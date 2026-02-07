package com.aegis.objectfilter.ui

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.aegis.objectfilter.core.analyzer.HybridAnalyzer
import com.aegis.objectfilter.core.driverreadiness.DriverReadinessEngine
import com.aegis.objectfilter.core.driverreadiness.DriverReadinessState
import com.aegis.objectfilter.core.filter.CognitiveFilter
import com.aegis.objectfilter.core.filter.FilteredSummary
import com.aegis.objectfilter.core.ml.LiteRtFaceLandmarker
import com.aegis.objectfilter.core.ml.LiteRtObjectDetector
import com.aegis.objectfilter.core.warning.ImmediateWarningDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(CameraUiState())
  val uiState: StateFlow<CameraUiState> = _uiState

  private val detector = LiteRtObjectDetector(
    modelAssetPath = "efficientdet-lite0.tflite",
  )

  private val faceLandmarker = LiteRtFaceLandmarker(
    modelAssetPath = "face_landmarker.task",
  )

  private val cognitiveFilter = CognitiveFilter()

  private val driverEngine = DriverReadinessEngine()

  private val warningDispatcher = ImmediateWarningDispatcher()

  private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

  fun bindCamera(context: Context, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener(
      {
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().apply {
          setSurfaceProvider(previewView.surfaceProvider)
        }

        val analysis = ImageAnalysis.Builder()
          .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
          .build()

        analysisExecutor.execute {
          try {
            detector.init(context)
            faceLandmarker.init(context)
            _uiState.update { it.copy(error = null) }
          } catch (t: Throwable) {
            val hint = "Ensure face_landmarker.task is placed in app/src/main/assets"
            val msg = t.message ?: "Model init failed"
            _uiState.update { it.copy(error = "$msg\n$hint") }
          }
        }

        val analyzer = HybridAnalyzer(
          detector = detector,
          faceLandmarker = faceLandmarker,
          cognitiveFilter = cognitiveFilter,
          driverEngine = driverEngine,
          onSummary = { summary ->
            _uiState.update { it.copy(lastSummary = summary) }
          },
          onDriverState = { driverState ->
            _uiState.update { it.copy(driverState = driverState) }
            warningDispatcher.dispatch(context, driverState)
          },
        )

        analysis.setAnalyzer(analysisExecutor, analyzer)

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
          lifecycleOwner,
          androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA,
          preview,
          analysis,
        )
      },
      ContextCompat.getMainExecutor(context),
    )
  }

  override fun onCleared() {
    super.onCleared()
    detector.close()
    faceLandmarker.close()
    warningDispatcher.close()
    analysisExecutor.shutdown()
  }
}

data class CameraUiState(
  val lastSummary: FilteredSummary? = null,
  val driverState: DriverReadinessState? = null,
  val error: String? = null,
)
