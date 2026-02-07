package com.aegis.objectfilter.core.driverreadiness

import kotlin.math.sqrt

data class EarResult(
  val left: Float? = null,
  val right: Float? = null,
  val avg: Float? = null,
)

class EarCalculator {
  fun compute(points: List<Point3>?): EarResult {
    if (points == null || points.size < 468) return EarResult()

    val left = ear(
      p1 = points[33],
      p2 = points[160],
      p3 = points[158],
      p4 = points[133],
      p5 = points[153],
      p6 = points[144],
    )

    val right = ear(
      p1 = points[362],
      p2 = points[385],
      p3 = points[387],
      p4 = points[263],
      p5 = points[373],
      p6 = points[380],
    )

    val avg = if (left != null && right != null) (left + right) / 2f else null
    return EarResult(left = left, right = right, avg = avg)
  }

  private fun ear(
    p1: Point3,
    p2: Point3,
    p3: Point3,
    p4: Point3,
    p5: Point3,
    p6: Point3,
  ): Float? {
    val w = dist(p1, p4).takeIf { it > 0f } ?: return null
    val h1 = dist(p2, p6)
    val h2 = dist(p3, p5)
    return (h1 + h2) / (2f * w)
  }

  private fun dist(a: Point3, b: Point3): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
  }
}
