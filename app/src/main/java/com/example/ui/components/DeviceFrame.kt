package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

/**
 * Premium Multi-Layered Smartphone Frame with Ultra-Slim Symmetric Bezels & Deep Rounded Corners.
 * - Scaled significantly further back (zoomed out) to provide generous room around the chassis.
 * - Multi-layered titanium/metallic aerospace outer chassis.
 * - Inner polished symmetric black display perimeter.
 */
@Composable
fun DeviceFrame(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.radialGradient(
          colors = listOf(
            Color(0xFF1E2638),
            Color(0xFF111724),
            Color(0xFF090D15)
          )
        )
      )
      .testTag("device_frame_container"),
    contentAlignment = Alignment.Center
  ) {
    val screenRatio = 9f / 19.5f // Standard flagship aspect ratio

    // Generous outer margins to zoom the whole device noticeably further away
    val outerMarginHorizontal = 56.dp
    val outerMarginVertical = 64.dp
    val availableWidth = (maxWidth - (outerMarginHorizontal * 2)).coerceAtLeast(80.dp)
    val availableHeight = (maxHeight - (outerMarginVertical * 2)).coerceAtLeast(160.dp)

    // Fit phone frame within available space while preserving aspect ratio
    val (frameWidth, frameHeight) = if (availableWidth / availableHeight > screenRatio) {
      val height = availableHeight
      val width = height * screenRatio
      Pair(width, height)
    } else {
      val width = availableWidth
      val height = width / screenRatio
      Pair(width, height)
    }

    // High-radius rounded flagship curves (proportional to dimensions)
    val outerCornerRadius = min(frameWidth * 0.165f, 44.dp)
    val middleChassisRadius = (outerCornerRadius - 2.dp).coerceAtLeast(10.dp)
    val innerScreenCornerRadius = (middleChassisRadius - 3.5.dp).coerceAtLeast(8.dp)

    // Layer 1: Phone Body Outer Chassis (Deep Ambient Shadow + Titanium Outer Rim)
    Box(
      modifier = Modifier
        .size(width = frameWidth, height = frameHeight)
        .shadow(
          elevation = 36.dp,
          shape = RoundedCornerShape(outerCornerRadius),
          ambientColor = Color.Black.copy(alpha = 0.8f),
          spotColor = Color(0xFF060B14).copy(alpha = 0.95f)
        )
        .clip(RoundedCornerShape(outerCornerRadius))
        // Titanium Outer Body Gradient
        .background(
          Brush.linearGradient(
            colors = listOf(
              Color(0xFF8B95A8),
              Color(0xFFB8C2D4),
              Color(0xFF6E7787),
              Color(0xFF9CA7B8),
              Color(0xFF555D6C)
            ),
            start = Offset(0f, 0f),
            end = Offset(frameWidth.value * 2f, frameHeight.value * 2f)
          )
        )
        // Outer Specular Chamfer Highlight
        .border(
          width = 1.2.dp,
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.65f),
              Color.White.copy(alpha = 0.2f),
              Color.White.copy(alpha = 0.45f)
            )
          ),
          shape = RoundedCornerShape(outerCornerRadius)
        )
        .padding(2.5.dp)
    ) {
      // Layer 2: Mid-Chassis Anodized Metal Band
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(middleChassisRadius))
          .background(
            Brush.linearGradient(
              colors = listOf(
                Color(0xFF38404E),
                Color(0xFF262C36),
                Color(0xFF1B2028)
              ),
              start = Offset(0f, 0f),
              end = Offset(0f, frameHeight.value * 1.5f)
            )
          )
          .padding(2.5.dp) // Ultra-slim symmetric bezel spacing
      ) {
        // Layer 3: Ultra-Black Symmetric Screen Bezel
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(innerScreenCornerRadius))
            .background(Color(0xFF05070A))
            .border(
              width = 0.75.dp,
              color = Color.Black,
              shape = RoundedCornerShape(innerScreenCornerRadius)
            )
            .padding(1.5.dp)
        ) {
          // Layer 4: Active Display Surface with Smooth Glass Curvature
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(RoundedCornerShape(innerScreenCornerRadius - 2.dp))
              .testTag("device_active_screen")
          ) {
            // Simulated OS Desktop Content
            content()

            // Screen Glass Edge Vignette
            Box(
              modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                  val cornerPx = (innerScreenCornerRadius - 2.dp).toPx()

                  // Top-left subtle diagonal glare reflection
                  drawRoundRect(
                    brush = Brush.linearGradient(
                      colors = listOf(
                        Color.White.copy(alpha = 0.04f),
                        Color.White.copy(alpha = 0.01f),
                        Color.Transparent
                      ),
                      start = Offset(0f, 0f),
                      end = Offset(size.width * 0.8f, size.height * 0.45f)
                    ),
                    size = size,
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                  )

                  // Subtle edge vignette
                  drawRoundRect(
                    color = Color.Black.copy(alpha = 0.06f),
                    size = size,
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = 1.5.dp.toPx())
                  )
                }
            )
          }
        }
      }
    }
  }
}
