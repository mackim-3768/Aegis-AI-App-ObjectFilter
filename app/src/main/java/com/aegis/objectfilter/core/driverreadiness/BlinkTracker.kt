package com.aegis.objectfilter.core.driverreadiness

class BlinkTracker {
  private var eyeClosed: Boolean = false
  private val blinkTimestamps = ArrayDeque<Long>()

  fun update(nowMs: Long, earAvg: Float?, drowsyThreshold: Float?) {
    if (earAvg == null || drowsyThreshold == null) return

    val closeThreshold = drowsyThreshold * 0.9f
    val openThreshold = drowsyThreshold * 1.05f

    if (!eyeClosed && earAvg <= closeThreshold) {
      eyeClosed = true
      return
    }

    if (eyeClosed && earAvg >= openThreshold) {
      eyeClosed = false
      blinkTimestamps.addLast(nowMs)
      trim(nowMs)
    }
  }

  fun ratePerMin(nowMs: Long): Float {
    trim(nowMs)
    return blinkTimestamps.size.toFloat()
  }

  private fun trim(nowMs: Long) {
    val windowStart = nowMs - 60_000L
    while (blinkTimestamps.isNotEmpty() && blinkTimestamps.first() < windowStart) {
      blinkTimestamps.removeFirst()
    }
  }
}
