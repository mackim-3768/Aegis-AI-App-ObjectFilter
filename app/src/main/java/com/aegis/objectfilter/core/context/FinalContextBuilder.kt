package com.aegis.objectfilter.core.context

import com.aegis.objectfilter.core.filter.FilteredSummary

object FinalContextBuilder {

  fun build(vision: FilteredSummary?, distilledIntent: String?): String {
    val visionText = vision?.summaryText ?: "{}"
    val intentText = distilledIntent ?: ""

    return """
[Vision]
$visionText

[DriverIntent]
$intentText
""".trimIndent()
  }
}
