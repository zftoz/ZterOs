package com.example.ui.components.icons

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.model.SystemAppId

/**
 * High-performance, bulletproof native PNG icon loader with zero crash possibility.
 * Safely decodes directly using Android BitmapFactory to avoid any Compose painter resolution NPEs on Android 16.
 */
@Composable
fun SystemAppIconGraphic(
  appId: SystemAppId,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val iconShape = RoundedCornerShape(percent = 23)

  val bitmap = remember(appId) {
    val resId = when (appId) {
      SystemAppId.DIALER -> R.drawable.system_dialer
      SystemAppId.MESSAGES -> R.drawable.system_messages
      SystemAppId.BROWSER -> R.drawable.system_browser
      SystemAppId.CAMERA -> R.drawable.system_camera
      SystemAppId.CALENDAR -> R.drawable.system_calendar
      SystemAppId.CLOCK -> R.drawable.system_clock
      SystemAppId.PHOTOS -> R.drawable.system_photos
      SystemAppId.SETTINGS -> R.drawable.system_settings
      SystemAppId.MUSIC -> R.drawable.system_music
      SystemAppId.FILE_MANAGER -> R.drawable.system_filemanager
      SystemAppId.CALCULATOR -> R.drawable.system_calculator
      SystemAppId.COMPASS -> R.drawable.system_compass
      SystemAppId.PLACEHOLDER -> null
    }

    if (resId != null) {
      try {
        BitmapFactory.decodeResource(context.resources, resId)?.asImageBitmap()
      } catch (_: Throwable) {
        null
      }
    } else {
      null
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .aspectRatio(1f)
      .clip(iconShape),
    contentAlignment = Alignment.Center
  ) {
    if (bitmap != null) {
      Image(
        bitmap = bitmap,
        contentDescription = appId.name,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
      )
    } else {
      PlaceholderIconContent()
    }
  }
}

@Composable
private fun PlaceholderIconContent() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.linearGradient(
          colors = listOf(
            Color(0xFF8692A6),
            Color(0xFF677387),
            Color(0xFF4D5666)
          ),
          start = Offset(0f, 0f),
          end = Offset(180f, 220f)
        )
      )
      .drawBehind {
        val cornerRadiusPx = size.minDimension * 0.23f
        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.35f),
              Color.White.copy(alpha = 0.05f),
              Color.Transparent
            ),
            startY = 0f,
            endY = size.height * 0.6f
          ),
          size = size,
          cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )
        drawRoundRect(
          color = Color.White.copy(alpha = 0.25f),
          topLeft = Offset(0.5f, 0.5f),
          size = Size(size.width - 1f, size.height - 1f),
          cornerRadius = CornerRadius(cornerRadiusPx - 0.5f, cornerRadiusPx - 0.5f),
          style = Stroke(width = 1f)
        )
      }
  )
}
