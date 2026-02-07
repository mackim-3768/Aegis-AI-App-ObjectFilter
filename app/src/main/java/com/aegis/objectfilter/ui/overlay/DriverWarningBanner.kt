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
import com.aegis.objectfilter.core.driverreadiness.DriverReadinessStatus
import com.aegis.objectfilter.ui.CameraUiState

@Composable
fun DriverWarningBanner(uiState: CameraUiState) {
  val state = uiState.driverState ?: return

  val bgColor = when (state.status) {
    DriverReadinessStatus.NOT_LOOKING -> Color(0xFFFFCDD2)
    DriverReadinessStatus.DROWSY -> Color(0xFFFFF59D)
    DriverReadinessStatus.STARING -> Color(0xFFFFF59D)
    DriverReadinessStatus.NO_FACE -> Color(0xFFE1F5FE)
    DriverReadinessStatus.ALERT -> Color.Transparent
  }

  if (bgColor == Color.Transparent) return

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(bgColor)
      .padding(12.dp),
  ) {
    Text(
      text = state.systemTag,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Black,
    )

    if (state.isCalibrating) {
      val pct = (state.calibrationProgress * 100).toInt()
      Text(
        text = "Calibration: ${pct}%",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Black,
      )
    }

    val m = state.metrics
    val earText = m.earAvg?.let { String.format("%.3f", it) } ?: "-"
    val yawText = m.yawDeg?.let { String.format("%.1f", it) } ?: "-"
    val pitchText = m.pitchDeg?.let { String.format("%.1f", it) } ?: "-"

    Text(
      text = "EAR=$earText yaw=$yawText pitch=$pitchText",
      style = MaterialTheme.typography.bodySmall,
      color = Color.Black,
    )
  }
}
