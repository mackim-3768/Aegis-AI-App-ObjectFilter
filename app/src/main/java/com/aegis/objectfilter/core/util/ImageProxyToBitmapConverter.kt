package com.aegis.objectfilter.core.util

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

class ImageProxyToBitmapConverter {

  fun toBitmap(image: ImageProxy): Bitmap? {
    if (image.image == null) return null
    if (image.format != ImageFormat.YUV_420_888) return null

    val nv21 = yuv420888ToNv21(image)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 80, out)
    val jpegBytes = out.toByteArray()

    return android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
  }

  private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)

    val chromaRowStride = image.planes[1].rowStride
    val chromaPixelStride = image.planes[1].pixelStride

    var outputOffset = ySize

    val width = image.width
    val height = image.height
    val chromaHeight = height / 2
    val chromaWidth = width / 2

    val uBytes = ByteArray(uBuffer.remaining())
    val vBytes = ByteArray(vBuffer.remaining())
    uBuffer.get(uBytes)
    vBuffer.get(vBytes)

    for (row in 0 until chromaHeight) {
      val rowStart = row * chromaRowStride
      for (col in 0 until chromaWidth) {
        val uvIndex = rowStart + col * chromaPixelStride
        nv21[outputOffset++] = vBytes[uvIndex]
        nv21[outputOffset++] = uBytes[uvIndex]
      }
    }

    return nv21
  }
}
