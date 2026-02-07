package com.aegis.objectfilter.core.driverreadiness

class CalibrationTracker(
  private val config: DriverReadinessConfig,
) {

  private val earSamples = ArrayList<Float>(512)
  private var startedAtMs: Long? = null
  private var baselineEar: Float? = null

  fun update(nowMs: Long, earAvg: Float?) {
    if (baselineEar != null) return
    if (startedAtMs == null) startedAtMs = nowMs

    if (!isCalibrating(nowMs)) {
      baselineEar = median(earSamples)
      return
    }

    if (earAvg != null && earAvg >= config.earClosedHardMin) {
      earSamples.add(earAvg)
    }
  }

  fun isCalibrating(nowMs: Long): Boolean {
    if (baselineEar != null) return false
    val started = startedAtMs ?: return true
    return (nowMs - started) < config.calibrationWindowMs
  }

  fun progress(nowMs: Long): Float {
    if (baselineEar != null) return 1f
    val started = startedAtMs ?: return 0f
    val elapsed = (nowMs - started).coerceAtLeast(0L)
    return (elapsed.toFloat() / config.calibrationWindowMs.toFloat()).coerceIn(0f, 1f)
  }

  fun drowsyThreshold(earAvg: Float?): Float? {
    val base = baselineEar ?: earAvg ?: return null
    return (base * config.drowsyEarBaselineFactor).coerceIn(0.12f, 0.30f)
  }

  private fun median(values: List<Float>): Float? {
    if (values.isEmpty()) return null
    val sorted = values.sorted()
    val mid = sorted.size / 2
    return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2f else sorted[mid]
  }
}
