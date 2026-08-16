package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OSStatusState

@Composable
fun OSStatusBar(
  statusState: OSStatusState,
  modifier: Modifier = Modifier,
  contentColor: Color = Color.White
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(28.dp)
      .padding(horizontal = 14.dp) // Aligns with the higher rounded corner curvature
      .testTag("os_status_bar"),
    contentAlignment = Alignment.Center
  ) {
    // Left Section: Time
    Row(
      modifier = Modifier.align(Alignment.CenterStart),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = statusState.timeString,
        color = contentColor,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp
      )
    }

    // Right Section: 5G, Cellular, Wi-Fi, and Battery
    Row(
      modifier = Modifier.align(Alignment.CenterEnd),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = statusState.carrierName,
        color = contentColor.copy(alpha = 0.9f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
      )

      Icon(
        imageVector = Icons.Default.SignalCellular4Bar,
        contentDescription = "Cellular Signal",
        tint = contentColor,
        modifier = Modifier.size(13.dp)
      )

      Icon(
        imageVector = Icons.Default.Wifi,
        contentDescription = "Wi-Fi Connected",
        tint = contentColor,
        modifier = Modifier.size(13.dp)
      )

      Spacer(modifier = Modifier.width(1.dp))

      Text(
        text = "${statusState.batteryLevel}%",
        color = contentColor,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Medium
      )

      BatteryIndicator(
        batteryLevel = statusState.batteryLevel,
        isCharging = statusState.isCharging,
        color = contentColor,
        modifier = Modifier.size(width = 19.dp, height = 9.5.dp)
      )
    }
  }
}

@Composable
fun BatteryIndicator(
  batteryLevel: Int,
  isCharging: Boolean,
  color: Color,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val strokeWidth = 1.1.dp.toPx()
      val bodyWidth = size.width - 2.dp.toPx()
      val bodyHeight = size.height
      val corner = 2.dp.toPx()

      drawRoundRect(
        color = color.copy(alpha = 0.6f),
        topLeft = Offset(0f, 0f),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = strokeWidth)
      )

      drawRoundRect(
        color = color.copy(alpha = 0.6f),
        topLeft = Offset(bodyWidth + 0.6.dp.toPx(), (bodyHeight - 3.2.dp.toPx()) / 2),
        size = Size(1.3.dp.toPx(), 3.2.dp.toPx()),
        cornerRadius = CornerRadius(0.8.dp.toPx(), 0.8.dp.toPx())
      )

      val fillRatio = (batteryLevel.coerceIn(0, 100) / 100f)
      val innerPadding = 1.3.dp.toPx()
      val fillWidth = (bodyWidth - innerPadding * 2) * fillRatio
      val fillHeight = bodyHeight - innerPadding * 2

      val fillColor = when {
        batteryLevel <= 20 -> Color(0xFFFF5252)
        else -> color
      }

      if (fillWidth > 0) {
        drawRoundRect(
          color = fillColor,
          topLeft = Offset(innerPadding, innerPadding),
          size = Size(fillWidth, fillHeight),
          cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
      }
    }

    if (isCharging) {
      Icon(
        imageVector = Icons.Default.Bolt,
        contentDescription = "Charging",
        tint = Color(0xFFFFD700),
        modifier = Modifier.size(8.5.dp)
      )
    }
  }
}
