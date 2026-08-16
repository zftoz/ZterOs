package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Universal System Gesture Bar.
 * - Sits at the bottom bezel.
 * - Drags dynamically lift and pull the application interface directly upwards from its position.
 */
@Composable
fun OSNavigationBar(
  modifier: Modifier = Modifier,
  pillColor: Color = Color.White.copy(alpha = 0.92f),
  onDragStart: () -> Unit = {},
  onDragDelta: (dragY: Float, dragX: Float) -> Unit = { _, _ -> },
  onDragReleased: (totalDragY: Float, totalDragX: Float) -> Unit = { _, _ -> },
  onTapGesture: () -> Unit = {}
) {
  val interactionSource = remember { MutableInteractionSource() }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(44.dp)
      .testTag("os_universal_gesture_bar")
      .pointerInput(Unit) {
        var cumY = 0f
        var cumX = 0f
        detectDragGestures(
          onDragStart = {
            cumY = 0f
            cumX = 0f
            onDragStart()
          },
          onDrag = { change, dragAmount ->
            change.consume()
            cumY += dragAmount.y
            cumX += dragAmount.x
            onDragDelta(dragAmount.y, dragAmount.x)
          },
          onDragEnd = {
            onDragReleased(cumY, cumX)
          },
          onDragCancel = {
            onDragReleased(cumY, cumX)
          }
        )
      }
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onTapGesture
      ),
    contentAlignment = Alignment.BottomCenter
  ) {
    Box(
      modifier = Modifier
        .padding(bottom = 6.dp)
        .width(115.dp)
        .height(4.4.dp)
        .clip(RoundedCornerShape(2.2.dp))
        .background(pillColor)
    )
  }
}
