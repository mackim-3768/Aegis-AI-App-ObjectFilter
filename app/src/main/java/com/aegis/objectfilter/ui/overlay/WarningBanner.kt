package com.aegis.objectfilter.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aegis.objectfilter.core.filter.RiskLevel
import com.aegis.objectfilter.ui.CameraUiState

@Composable
fun WarningBanner(uiState: CameraUiState) {
  val error = uiState.error
  if (error != null) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFFFFCDD2))
        .padding(12.dp),
    ) {
      Text(
        text = error,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Black,
      )
    }
    return
  }

  val summary = uiState.lastSummary
  if (summary == null) return

  val bgColor = when (summary.highestRisk) {
    RiskLevel.CRITICAL -> Color(0xFFFFCDD2)
    RiskLevel.WARNING -> Color(0xFFFFF59D)
    RiskLevel.NONE -> Color.Transparent
  }

  if (bgColor == Color.Transparent) return

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(bgColor)
      .padding(12.dp),
  ) {
    Text(
      text = summary.summaryText,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Black,
    )
  }
}
