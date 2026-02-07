package com.aegis.objectfilter.core.driverreadiness

class PoseEmaFilter(
  private val alpha: Float = 0.35f,
) {
  private var yawEma: Float? = null
  private var pitchEma: Float? = null

  fun update(pose: PoseDeg): PoseDeg {
    yawEma = ema(yawEma, pose.yaw)
    pitchEma = ema(pitchEma, pose.pitch)
    return PoseDeg(yaw = yawEma, pitch = pitchEma)
  }

  private fun ema(prev: Float?, next: Float?): Float? {
    if (next == null) return prev
    return prev?.let { (1 - alpha) * it + alpha * next } ?: next
  }
}
