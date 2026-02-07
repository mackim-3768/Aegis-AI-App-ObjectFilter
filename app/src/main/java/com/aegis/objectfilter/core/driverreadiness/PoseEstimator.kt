package com.aegis.objectfilter.core.driverreadiness

import kotlin.math.atan2
import kotlin.math.sqrt

data class PoseDeg(
  val yaw: Float? = null,
  val pitch: Float? = null,
)

class PoseEstimator {
  fun estimate(transform: FloatArray?): PoseDeg {
    if (transform == null || transform.size < 16) return PoseDeg()

    val r02 = transform[8]
    val r12 = transform[9]
    val r22 = transform[10]

    val yaw = atan2(r02.toDouble(), r22.toDouble()).toFloat() * RAD_TO_DEG
    val pitch = atan2((-r12).toDouble(), sqrt((r02 * r02 + r22 * r22).toDouble())).toFloat() * RAD_TO_DEG

    return PoseDeg(yaw = yaw, pitch = pitch)
  }

  private companion object {
    private const val RAD_TO_DEG = (180.0 / Math.PI).toFloat()
  }
}
