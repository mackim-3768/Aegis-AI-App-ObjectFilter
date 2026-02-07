package com.aegis.objectfilter.core.driverreadiness

import kotlin.math.abs

class MotionTracker {
  private var lastYaw: Float? = null
  private var lastPitch: Float? = null

  fun update(dtMs: Long, pose: PoseDeg): Boolean {
    if (dtMs <= 0L) return false

    val yaw = pose.yaw
    val pitch = pose.pitch
    val yawPrev = lastYaw
    val pitchPrev = lastPitch

    lastYaw = yaw
    lastPitch = pitch

    if (yaw == null || pitch == null || yawPrev == null || pitchPrev == null) return false

    val dtSec = dtMs.toFloat() / 1000f
    val yawSpeed = abs(yaw - yawPrev) / dtSec
    val pitchSpeed = abs(pitch - pitchPrev) / dtSec
    return yawSpeed < 2f && pitchSpeed < 2f
  }
}
