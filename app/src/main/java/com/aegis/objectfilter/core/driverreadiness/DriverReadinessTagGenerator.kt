package com.aegis.objectfilter.core.driverreadiness

object DriverReadinessTagGenerator {
  fun tagFor(status: DriverReadinessStatus): String {
    return when (status) {
      DriverReadinessStatus.ALERT -> "[Status: Alert]"
      DriverReadinessStatus.DROWSY -> "[Status: Drowsy / URGENT]"
      DriverReadinessStatus.NOT_LOOKING -> "[Status: Not_Looking / CRITICAL]"
      DriverReadinessStatus.STARING -> "[Status: Staring / WARNING]"
      DriverReadinessStatus.NO_FACE -> "[Status: No_Face / WARNING]"
    }
  }
}
