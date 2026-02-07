package com.aegis.objectfilter.core.driverreadiness

enum class DriverReadinessStatus {
  ALERT,
  DROWSY,
  NOT_LOOKING,
  STARING,
  NO_FACE,
}

data class DriverMetrics(
  val earLeft: Float? = null,
  val earRight: Float? = null,
  val earAvg: Float? = null,
  val yawDeg: Float? = null,
  val pitchDeg: Float? = null,
  val blinkRatePerMin: Float? = null,
  val stabilityScore: Float = 0f,
)

data class DriverReadinessState(
  val status: DriverReadinessStatus,
  val metrics: DriverMetrics,
  val confidence: Float,
  val timestampMs: Long,
  val systemTag: String,
  val isCalibrating: Boolean,
  val calibrationProgress: Float,
)

data class Point3(
  val x: Float,
  val y: Float,
  val z: Float,
)

data class FaceMeshFrame(
  val points: List<Point3>,
  val transformMatrix: FloatArray?,
)
