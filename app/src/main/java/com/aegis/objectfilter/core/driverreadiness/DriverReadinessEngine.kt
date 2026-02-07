package com.aegis.objectfilter.core.driverreadiness

class DriverReadinessEngine(
  private val config: DriverReadinessConfig = DriverReadinessConfig(),
) {

  private val timeTracker = TimeTracker()
  private val earCalculator = EarCalculator()
  private val calibrationTracker = CalibrationTracker(config)
  private val poseEstimator = PoseEstimator()
  private val poseEmaFilter = PoseEmaFilter()
  private val blinkTracker = BlinkTracker()
  private val motionTracker = MotionTracker()
  private val statusAccumulator = StatusAccumulator(config)

  fun process(nowMs: Long, frame: FaceMeshFrame?): DriverReadinessState {
    val dtMs = timeTracker.update(nowMs)

    val ear = earCalculator.compute(frame?.points)
    calibrationTracker.update(nowMs, ear.avg)
    val drowsyThreshold = calibrationTracker.drowsyThreshold(ear.avg)

    val rawPose = poseEstimator.estimate(frame?.transformMatrix)
    val pose = poseEmaFilter.update(rawPose)

    blinkTracker.update(nowMs, ear.avg, drowsyThreshold)
    val isLowMotion = motionTracker.update(dtMs, pose)

    val status = if (frame == null) {
      statusAccumulator.updateNoFace(dtMs)
    } else {
      statusAccumulator.update(
        dtMs = dtMs,
        earAvg = ear.avg,
        drowsyThreshold = drowsyThreshold,
        pose = pose,
        isLowMotion = isLowMotion,
      )
    }

    val metrics = DriverMetrics(
      earLeft = ear.left,
      earRight = ear.right,
      earAvg = ear.avg,
      yawDeg = pose.yaw,
      pitchDeg = pose.pitch,
      blinkRatePerMin = blinkTracker.ratePerMin(nowMs),
      stabilityScore = statusAccumulator.stabilityScore(isLowMotion),
    )

    val confidence = confidenceFor(frame, ear.avg, pose)
    val tag = DriverReadinessTagGenerator.tagFor(status)

    return DriverReadinessState(
      status = status,
      metrics = metrics,
      confidence = confidence,
      timestampMs = nowMs,
      systemTag = tag,
      isCalibrating = calibrationTracker.isCalibrating(nowMs),
      calibrationProgress = calibrationTracker.progress(nowMs),
    )
  }

  private fun confidenceFor(frame: FaceMeshFrame?, earAvg: Float?, pose: PoseDeg): Float {
    if (frame == null) return 0f
    var c = 0.5f
    if (earAvg != null) c += 0.25f
    if (pose.yaw != null && pose.pitch != null) c += 0.25f
    return c.coerceIn(0f, 1f)
  }
}
