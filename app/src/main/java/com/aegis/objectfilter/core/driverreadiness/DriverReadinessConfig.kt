package com.aegis.objectfilter.core.driverreadiness

data class DriverReadinessConfig(
  val calibrationWindowMs: Long = 30_000L,
  val drowsyHoldMs: Long = 2_000L,
  val notLookingHoldMs: Long = 1_500L,
  val staringHoldMs: Long = 500L,
  val notLookingYawThresholdDeg: Float = 15f,
  val notLookingPitchDownThresholdDeg: Float = -15f,
  val drowsyEarBaselineFactor: Float = 0.75f,
  val earClosedHardMin: Float = 0.15f,
)
