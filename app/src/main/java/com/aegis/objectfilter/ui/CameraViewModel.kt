package com.aegis.objectfilter.ui

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.aegis.objectfilter.core.analyzer.CognitiveAnalyzer
import com.aegis.objectfilter.core.filter.CognitiveFilter
import com.aegis.objectfilter.core.filter.FilteredSummary
import com.aegis.objectfilter.core.ml.LiteRtObjectDetector
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

  private val cognitiveFilter = CognitiveFilter()

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
            _uiState.update { it.copy(error = null) }
          } catch (t: Throwable) {
            _uiState.update { it.copy(error = t.message ?: "Detector init failed") }
          }
        }

        val analyzer = CognitiveAnalyzer(
          detector = detector,
          cognitiveFilter = cognitiveFilter,
          onSummary = { summary ->
            _uiState.update { it.copy(lastSummary = summary) }
          },
        )

        analysis.setAnalyzer(analysisExecutor, analyzer)

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
          lifecycleOwner,
          androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
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
    analysisExecutor.shutdown()
  }
}

data class CameraUiState(
  val lastSummary: FilteredSummary? = null,
  val error: String? = null,
)
