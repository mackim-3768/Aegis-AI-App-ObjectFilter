package com.aegis.objectfilter.core.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.aegis.objectfilter.core.filter.CognitiveFilter
import com.aegis.objectfilter.core.filter.FilteredSummary
import com.aegis.objectfilter.core.ml.LiteRtObjectDetector
import com.aegis.objectfilter.core.util.ImageProxyToBitmapConverter
import kotlin.system.measureTimeMillis
import java.util.concurrent.atomic.AtomicBoolean

class CognitiveAnalyzer(
  private val detector: LiteRtObjectDetector,
  private val cognitiveFilter: CognitiveFilter,
  private val onSummary: (FilteredSummary) -> Unit,
) : ImageAnalysis.Analyzer {

  private val converter = ImageProxyToBitmapConverter()
  private val isProcessing = AtomicBoolean(false)

  override fun analyze(image: ImageProxy) {
    if (!isProcessing.compareAndSet(false, true)) {
      image.close()
      return
    }

    try {
      val bitmap = converter.toBitmap(image) ?: return

      lateinit var detections: List<LiteRtObjectDetector.DetectionResult>
      val inferenceMs = measureTimeMillis {
        detections = detector.detect(bitmap)
      }

      val summary = cognitiveFilter.summarize(
        detections = detections,
        frameWidth = bitmap.width,
        frameHeight = bitmap.height,
        inferenceMs = inferenceMs,
      )

      onSummary(summary)
    } finally {
      image.close()
      isProcessing.set(false)
    }
  }
}
