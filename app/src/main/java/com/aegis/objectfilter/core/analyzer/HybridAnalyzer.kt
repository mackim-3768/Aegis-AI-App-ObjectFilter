package com.aegis.objectfilter.core.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.aegis.objectfilter.core.driverreadiness.DriverReadinessEngine
import com.aegis.objectfilter.core.driverreadiness.DriverReadinessState
import com.aegis.objectfilter.core.driverreadiness.FaceMeshFrame
import com.aegis.objectfilter.core.driverreadiness.Point3
import com.aegis.objectfilter.core.filter.CognitiveFilter
import com.aegis.objectfilter.core.filter.FilteredSummary
import com.aegis.objectfilter.core.ml.LiteRtFaceLandmarker
import com.aegis.objectfilter.core.ml.LiteRtObjectDetector
import com.aegis.objectfilter.core.util.ImageProxyToBitmapConverter
import com.aegis.objectfilter.core.util.rotate
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

class HybridAnalyzer(
  private val detector: LiteRtObjectDetector,
  private val faceLandmarker: LiteRtFaceLandmarker,
  private val cognitiveFilter: CognitiveFilter,
  private val driverEngine: DriverReadinessEngine,
  private val onSummary: (FilteredSummary) -> Unit,
  private val onDriverState: (DriverReadinessState) -> Unit,
) : ImageAnalysis.Analyzer {

  private val converter = ImageProxyToBitmapConverter()
  private val isProcessing = AtomicBoolean(false)

  override fun analyze(image: ImageProxy) {
    if (!isProcessing.compareAndSet(false, true)) {
      image.close()
      return
    }

    try {
      val nowMs = image.imageInfo.timestamp / 1_000_000L
      val bitmap = converter.toBitmap(image) ?: return
      val rotated = bitmap.rotate(image.imageInfo.rotationDegrees)

      lateinit var detections: List<LiteRtObjectDetector.DetectionResult>
      val objectInferenceMs = measureTimeMillis {
        detections = detector.detect(rotated)
      }

      val summary = cognitiveFilter.summarize(
        detections = detections,
        frameWidth = rotated.width,
        frameHeight = rotated.height,
        inferenceMs = objectInferenceMs,
      )
      onSummary(summary)

      val faceResult: FaceLandmarkerResult? = try {
        faceLandmarker.detect(rotated)
      } catch (_: Throwable) {
        null
      }

      val frame = faceResult?.toFaceMeshFrame()
      val driverState = driverEngine.process(nowMs, frame)
      onDriverState(driverState)
    } finally {
      image.close()
      isProcessing.set(false)
    }
  }
}

private fun FaceLandmarkerResult.toFaceMeshFrame(): FaceMeshFrame? {
  val faces = faceLandmarks()
  if (faces.isEmpty()) return null

  val points = faces.first().map { lm ->
    Point3(
      x = lm.x(),
      y = lm.y(),
      z = lm.z(),
    )
  }

  val matrix = facialTransformationMatrixes().orElse(null)?.firstOrNull()

  return FaceMeshFrame(
    points = points,
    transformMatrix = matrix,
  )
}
