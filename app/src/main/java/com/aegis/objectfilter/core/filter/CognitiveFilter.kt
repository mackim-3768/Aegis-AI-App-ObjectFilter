package com.aegis.objectfilter.core.filter

import android.graphics.RectF
import com.google.gson.Gson
import kotlin.math.abs

class CognitiveFilter {

  private val gson = Gson()

  fun summarize(
    detections: List<DetectionLike>,
    frameWidth: Int,
    frameHeight: Int,
    inferenceMs: Long,
  ): FilteredSummary {
    val filtered = detections
      .mapNotNull { detection ->
        val category = detection.categoryName
        val target = TargetObject.fromLabel(category) ?: return@mapNotNull null

        val bbox = detection.boundingBox
        val centerDistance = normalizedCenterDistance(
          bbox = bbox,
          frameWidth = frameWidth,
          frameHeight = frameHeight,
        )
        val areaRatio = areaRatio(bbox, frameWidth, frameHeight)

        val risk = when {
          centerDistance <= 0.2f || areaRatio >= 0.30f -> RiskLevel.CRITICAL
          centerDistance <= 0.35f || areaRatio >= 0.15f -> RiskLevel.WARNING
          else -> RiskLevel.NONE
        }

        FilteredObject(
          label = target.label,
          distance = if (areaRatio >= 0.30f) Distance.CLOSE else Distance.MID,
          risk = risk,
          bbox = bbox,
        )
      }

    val highestRisk = filtered.maxOfOrNull { it.risk } ?: RiskLevel.NONE

    val payload = FilteredPayload(
      objects = filtered.map {
        FilteredPayloadObject(
          objectLabel = it.label,
          distance = it.distance.name,
          status = it.risk.name,
        )
      },
      inferenceMs = inferenceMs,
    )

    val summaryText = gson.toJson(payload)

    return FilteredSummary(
      highestRisk = highestRisk,
      summaryText = summaryText,
      objects = filtered,
    )
  }

  private fun normalizedCenterDistance(
    bbox: RectF,
    frameWidth: Int,
    frameHeight: Int,
  ): Float {
    val cx = (bbox.left + bbox.right) / 2f
    val cy = (bbox.top + bbox.bottom) / 2f
    val nx = cx / frameWidth
    val ny = cy / frameHeight
    val dx = abs(nx - 0.5f)
    val dy = abs(ny - 0.5f)
    return (dx + dy) / 2f
  }

  private fun areaRatio(bbox: RectF, frameWidth: Int, frameHeight: Int): Float {
    val bw = (bbox.right - bbox.left).coerceAtLeast(0f)
    val bh = (bbox.bottom - bbox.top).coerceAtLeast(0f)
    val area = bw * bh
    val frameArea = (frameWidth.toFloat() * frameHeight.toFloat()).coerceAtLeast(1f)
    return area / frameArea
  }
}

interface DetectionLike {
  val categoryName: String
  val boundingBox: RectF
}

enum class RiskLevel {
  NONE,
  WARNING,
  CRITICAL,
}

enum class Distance {
  CLOSE,
  MID,
  FAR,
}

enum class TargetObject(val label: String) {
  PEDESTRIAN("Pedestrian"),
  CAR("Car"),
  TRAFFIC_LIGHT("Traffic Light"),
  STOP_SIGN("Stop Sign"),
  ;

  companion object {
    fun fromLabel(label: String): TargetObject? {
      return entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
    }
  }
}

data class FilteredObject(
  val label: String,
  val distance: Distance,
  val risk: RiskLevel,
  val bbox: RectF,
)

data class FilteredPayload(
  val objects: List<FilteredPayloadObject>,
  val inferenceMs: Long,
)

data class FilteredPayloadObject(
  val objectLabel: String,
  val distance: String,
  val status: String,
)

data class FilteredSummary(
  val highestRisk: RiskLevel,
  val summaryText: String,
  val objects: List<FilteredObject>,
)
