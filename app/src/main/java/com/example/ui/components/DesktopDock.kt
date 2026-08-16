package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.model.AppRect
import com.example.model.SystemAppId

@Composable
fun DesktopDock(
  modifier: Modifier = Modifier,
  apps: List<SystemAppId> = listOf(
    SystemAppId.DIALER,
    SystemAppId.MESSAGES,
    SystemAppId.BROWSER,
    SystemAppId.CAMERA
  ),
  isAppActiveOrAnimating: (SystemAppId) -> Boolean = { false },
  onAppPositioned: (SystemAppId, AppRect) -> Unit = { _, _ -> },
  onAppClick: (SystemAppId) -> Unit = {}
) {
  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp)
      .testTag("desktop_dock"),
    contentAlignment = Alignment.Center
  ) {
    val computedIconSize = min((maxWidth / 5.2f), 56.dp).coerceAtLeast(36.dp)
    val dockHeight = (computedIconSize + 20.dp).coerceIn(60.dp, 80.dp)
    val cornerRadius = min(dockHeight / 2.8f, 26.dp)

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(dockHeight)
        .shadow(
          elevation = 10.dp,
          shape = RoundedCornerShape(cornerRadius),
          ambientColor = Color.Black.copy(alpha = 0.25f),
          spotColor = Color.Black.copy(alpha = 0.35f)
        )
        .clip(RoundedCornerShape(cornerRadius))
        .background(Color(0x352A3B54))
        .border(
          width = 1.dp,
          color = Color(0x35FFFFFF),
          shape = RoundedCornerShape(cornerRadius)
        )
        .padding(horizontal = 10.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        apps.forEachIndexed { _, appId ->
          DesktopAppIcon(
            appId = appId,
            size = computedIconSize,
            testTagId = "dock_app_${appId.name.lowercase()}",
            isVisibleOnGrid = !isAppActiveOrAnimating(appId),
            onPositioned = { rect -> onAppPositioned(appId, rect) },
            onClick = { onAppClick(appId) }
          )
        }
      }
    }
  }
}
