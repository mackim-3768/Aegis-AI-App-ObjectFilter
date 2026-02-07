package com.aegis.objectfilter.core.ml

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class LiteRtFaceLandmarker(
  private val modelAssetPath: String,
) : AutoCloseable {

  private var landmarker: FaceLandmarker? = null

  @Synchronized
  fun init(context: Context) {
    if (landmarker != null) return

    val isExynos = Build.HARDWARE.contains("exynos", ignoreCase = true) ||
      Build.BOARD.contains("exynos", ignoreCase = true)

    val delegateOrder = if (isExynos) {
      listOf(Delegate.GPU, Delegate.CPU)
    } else {
      listOf(Delegate.GPU, Delegate.CPU)
    }

    var lastError: Throwable? = null
    for (delegate in delegateOrder) {
      try {
        val baseOptions = BaseOptions.builder()
          .setModelAssetPath(modelAssetPath)
          .setDelegate(delegate)
          .build()

        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
          .setBaseOptions(baseOptions)
          .setNumFaces(1)
          .setMinFaceDetectionConfidence(0.5f)
          .setMinFacePresenceConfidence(0.5f)
          .setMinTrackingConfidence(0.5f)
          .setOutputFaceBlendshapes(false)
          .setOutputFacialTransformationMatrixes(true)
          .setRunningMode(RunningMode.IMAGE)
          .build()

        landmarker = FaceLandmarker.createFromOptions(context, options)
        return
      } catch (t: Throwable) {
        lastError = t
      }
    }

    throw IllegalStateException("Failed to initialize FaceLandmarker", lastError)
  }

  fun detect(bitmap: Bitmap): FaceLandmarkerResult? {
    val faceLandmarker = landmarker ?: return null
    val argb8888Bitmap = if (bitmap.config == Bitmap.Config.ARGB_8888) {
      bitmap
    } else {
      bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    val mpImage = BitmapImageBuilder(argb8888Bitmap).build()
    return faceLandmarker.detect(mpImage)
  }

  override fun close() {
    landmarker?.close()
    landmarker = null
  }
}
