package com.aegis.objectfilter.core.util

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.rotate(rotationDegrees: Int): Bitmap {
  if (rotationDegrees % 360 == 0) return this

  val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
  return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
