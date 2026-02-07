package com.aegis.objectfilter.core.driverreadiness

import kotlin.math.abs

class StatusAccumulator(
  private val config: DriverReadinessConfig,
) {
  private var drowsyAccumMs: Long = 0L
  private var notLookingAccumMs: Long = 0L
  private var staringAccumMs: Long = 0L

  fun updateNoFace(dtMs: Long): DriverReadinessStatus {
    reset(dtMs)
    return DriverReadinessStatus.NO_FACE
  }

  fun update(
    dtMs: Long,
    earAvg: Float?,
    drowsyThreshold: Float?,
    pose: PoseDeg,
    isLowMotion: Boolean,
  ): DriverReadinessStatus {
    drowsyAccumMs = if (earAvg != null && drowsyThreshold != null && earAvg <= drowsyThreshold) {
      drowsyAccumMs + dtMs
    } else {
      0L
    }

    val isNotLooking = isNotLooking(pose)
    notLookingAccumMs = if (isNotLooking) notLookingAccumMs + dtMs else 0L

    staringAccumMs = if (isLowMotion) staringAccumMs + dtMs else 0L

    return when {
      notLookingAccumMs >= config.notLookingHoldMs -> DriverReadinessStatus.NOT_LOOKING
      drowsyAccumMs >= config.drowsyHoldMs -> DriverReadinessStatus.DROWSY
      staringAccumMs >= config.staringHoldMs -> DriverReadinessStatus.STARING
      else -> DriverReadinessStatus.ALERT
    }
  }

  fun stabilityScore(isLowMotion: Boolean): Float {
    if (!isLowMotion) return 0f
    return (staringAccumMs.toFloat() / config.staringHoldMs.toFloat()).coerceIn(0f, 1f)
  }

  private fun isNotLooking(pose: PoseDeg): Boolean {
    val yaw = pose.yaw
    val pitch = pose.pitch

    val yawOff = yaw != null && abs(yaw) >= config.notLookingYawThresholdDeg
    val pitchDown = pitch != null && pitch <= config.notLookingPitchDownThresholdDeg
    return yawOff || pitchDown
  }

  private fun reset(dtMs: Long) {
    if (dtMs <= 0L) return
    drowsyAccumMs = 0L
    notLookingAccumMs = 0L
    staringAccumMs = 0L
  }
}
