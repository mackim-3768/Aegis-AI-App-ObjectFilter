package com.aegis.objectfilter.core.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import com.aegis.objectfilter.core.filter.DetectionLike
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class LiteRtObjectDetector(
  private val modelAssetPath: String,
) : AutoCloseable {

  private var objectDetector: ObjectDetector? = null

  fun detect(bitmap: Bitmap): List<DetectionResult> {
    val detector = objectDetector ?: return emptyList()

    val image = TensorImage.fromBitmap(bitmap)
    val results = detector.detect(image)

    return results.flatMap { detection ->
      val bbox = detection.boundingBox
      detection.categories.map { category ->
        DetectionResult(
          categoryName = category.label,
          score = category.score,
          boundingBox = bbox,
        )
      }
    }
  }

  @Synchronized
  fun init(context: Context) {
    if (objectDetector != null) return

    val isExynos = Build.HARDWARE.contains("exynos", ignoreCase = true) ||
      Build.BOARD.contains("exynos", ignoreCase = true)

    val delegateOrder: List<(BaseOptions.Builder) -> BaseOptions.Builder> =
      if (isExynos) {
        listOf(
          { it.useNnapi() },
          { it.useGpu() },
          { it },
        )
      } else {
        listOf(
          { it.useGpu() },
          { it.useNnapi() },
          { it },
        )
      }

    var lastError: Throwable? = null
    for (configureDelegate in delegateOrder) {
      try {
        val baseOptions = configureDelegate(
          BaseOptions.builder().setNumThreads(2),
        ).build()

        val options = ObjectDetector.ObjectDetectorOptions.builder()
          .setBaseOptions(baseOptions)
          .setMaxResults(5)
          .build()

        objectDetector = ObjectDetector.createFromFileAndOptions(
          context,
          modelAssetPath,
          options,
        )
        return
      } catch (t: Throwable) {
        lastError = t
      }
    }

    throw IllegalStateException("Failed to initialize ObjectDetector", lastError)
  }

  override fun close() {
    objectDetector?.close()
    objectDetector = null
  }

  data class DetectionResult(
    override val categoryName: String,
    val score: Float,
    override val boundingBox: RectF,
  ) : DetectionLike
}
