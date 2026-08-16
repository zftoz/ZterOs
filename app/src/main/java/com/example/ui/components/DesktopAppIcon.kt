package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AppRect
import com.example.model.SystemAppId
import com.example.ui.components.icons.SystemAppIconGraphic

/**
 * Desktop app icon with precise root-coordinate tracking and visibility sync.
 * When the app is animating/open, the grid icon hides its graphic so the expanding
 * window acts as the exact single continuous entity.
 */
@Composable
fun DesktopAppIcon(
  modifier: Modifier = Modifier,
  appId: SystemAppId = SystemAppId.PLACEHOLDER,
  size: Dp = 54.dp,
  testTagId: String = "app_icon",
  isVisibleOnGrid: Boolean = true,
  onPositioned: (AppRect) -> Unit = {},
  onClick: () -> Unit = {}
) {
  val interactionSource = remember { MutableInteractionSource() }
  val iconShape = RoundedCornerShape(percent = 23)

  Box(
    modifier = modifier
      .size(size)
      .aspectRatio(1f)
      .testTag(testTagId)
      .onGloballyPositioned { coordinates: LayoutCoordinates ->
        val pos = coordinates.positionInRoot()
        val s = coordinates.size
        if (s.width > 0 && s.height > 0) {
          onPositioned(
            AppRect(
              x = pos.x,
              y = pos.y,
              width = s.width.toFloat(),
              height = s.height.toFloat()
            )
          )
        }
      }
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    if (isVisibleOnGrid) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .shadow(
            elevation = 4.dp,
            shape = iconShape,
            ambientColor = Color.Black.copy(alpha = 0.3f),
            spotColor = Color(0xFF0F172A).copy(alpha = 0.4f)
          )
      ) {
        SystemAppIconGraphic(
          appId = appId,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
