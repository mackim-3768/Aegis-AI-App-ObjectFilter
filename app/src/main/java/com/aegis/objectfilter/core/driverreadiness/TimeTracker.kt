package com.aegis.objectfilter.core.driverreadiness

class TimeTracker {
  private var lastAtMs: Long? = null

  fun update(nowMs: Long): Long {
    val prev = lastAtMs
    lastAtMs = nowMs
    return if (prev == null) 0L else (nowMs - prev).coerceAtLeast(0L)
  }
}
