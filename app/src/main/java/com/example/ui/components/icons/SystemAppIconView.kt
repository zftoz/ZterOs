package com.example.ui.components.icons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.SystemAppId

// Safe cache that does NOT throw NPE on null values
private val iconBitmapCache = HashMap<SystemAppId, ImageBitmap?>()

/**
 * Loads the user's PNG icon drawables directly from raw resource streams.
 */
@Composable
fun SystemAppIconGraphic(
  appId: SystemAppId,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val iconShape = RoundedCornerShape(percent = 23)

  val bitmap: ImageBitmap? = remember(appId) {
    synchronized(iconBitmapCache) {
      if (iconBitmapCache.containsKey(appId)) {
        iconBitmapCache[appId]
      } else {
        val loaded = loadRawAppBitmap(context, appId)
        iconBitmapCache[appId] = loaded
        loaded
      }
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
        contentScale = ContentScale.Crop
      )
    } else {
      VectorAppIconFallback(appId = appId)
    }
  }
}

private fun loadRawAppBitmap(context: Context, appId: SystemAppId): ImageBitmap? {
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
  } ?: return null

  // 1. Direct raw byte stream decoding (bypasses any resource density bugs)
  try {
    context.resources.openRawResource(resId).use { stream ->
      val bmp = BitmapFactory.decodeStream(stream)
      if (bmp != null) {
        return bmp.asImageBitmap()
      }
    }
  } catch (e: Throwable) {
    Log.w("SystemAppIcon", "Raw stream load failed for $appId: ${e.message}")
  }

  // 2. Standard BitmapFactory decode
  try {
    val bmp = BitmapFactory.decodeResource(context.resources, resId)
    if (bmp != null) {
      return bmp.asImageBitmap()
    }
  } catch (e: Throwable) {
    Log.w("SystemAppIcon", "Resource decode failed for $appId: ${e.message}")
  }

  // 3. ContextCompat drawable rendering
  try {
    val drawable = ContextCompat.getDrawable(context, resId)
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
      return drawable.bitmap.asImageBitmap()
    } else if (drawable != null) {
      val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 200
      val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 200
      val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bmp)
      drawable.setBounds(0, 0, width, height)
      drawable.draw(canvas)
      return bmp.asImageBitmap()
    }
  } catch (e: Throwable) {
    Log.w("SystemAppIcon", "Drawable draw failed for $appId: ${e.message}")
  }

  return null
}

@Composable
private fun VectorAppIconFallback(appId: SystemAppId) {
  val config = when (appId) {
    SystemAppId.DIALER -> IconConfig(
      bgGradient = listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669)),
      icon = Icons.Filled.Call,
      tint = Color.White
    )
    SystemAppId.MESSAGES -> IconConfig(
      bgGradient = listOf(Color(0xFF60A5FA), Color(0xFF3B82F6), Color(0xFF2563EB)),
      icon = Icons.Filled.Message,
      tint = Color.White
    )
    SystemAppId.BROWSER -> IconConfig(
      bgGradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1)),
      icon = Icons.Filled.Language,
      tint = Color.White
    )
    SystemAppId.CAMERA -> IconConfig(
      bgGradient = listOf(Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF475569)),
      icon = Icons.Filled.CameraAlt,
      tint = Color.White
    )
    SystemAppId.CALENDAR -> IconConfig(
      bgGradient = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFE2E8F0)),
      icon = Icons.Filled.CalendarMonth,
      tint = Color(0xFFEF4444)
    )
    SystemAppId.CLOCK -> IconConfig(
      bgGradient = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617)),
      icon = Icons.Filled.Schedule,
      tint = Color(0xFFF59E0B)
    )
    SystemAppId.PHOTOS -> IconConfig(
      bgGradient = listOf(Color(0xFFFDE047), Color(0xFFFB923C), Color(0xFFEC4899)),
      icon = Icons.Filled.Image,
      tint = Color.White
    )
    SystemAppId.SETTINGS -> IconConfig(
      bgGradient = listOf(Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF334155)),
      icon = Icons.Filled.Settings,
      tint = Color.White
    )
    SystemAppId.MUSIC -> IconConfig(
      bgGradient = listOf(Color(0xFFF43F5E), Color(0xFFE11D48), Color(0xFFBE123C)),
      icon = Icons.Filled.MusicNote,
      tint = Color.White
    )
    SystemAppId.FILE_MANAGER -> IconConfig(
      bgGradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0F172A)),
      icon = Icons.Filled.Folder,
      tint = Color(0xFFFBBF24)
    )
    SystemAppId.CALCULATOR -> IconConfig(
      bgGradient = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
      icon = Icons.Filled.Calculate,
      tint = Color(0xFFF97316)
    )
    SystemAppId.COMPASS -> IconConfig(
      bgGradient = listOf(Color(0xFF0F172A), Color(0xFF020617), Color(0xFF000000)),
      icon = Icons.Filled.Explore,
      tint = Color(0xFFEF4444)
    )
    SystemAppId.PLACEHOLDER -> IconConfig(
      bgGradient = listOf(Color(0xFF64748B), Color(0xFF475569), Color(0xFF334155)),
      icon = Icons.Filled.Folder,
      tint = Color.White.copy(alpha = 0.7f)
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.linearGradient(
          colors = config.bgGradient,
          start = Offset(0f, 0f),
          end = Offset(160f, 200f)
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
            endY = size.height * 0.5f
          ),
          size = size,
          cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )
        drawRoundRect(
          color = Color.White.copy(alpha = 0.2f),
          topLeft = Offset(0.5f, 0.5f),
          size = Size(size.width - 1f, size.height - 1f),
          cornerRadius = CornerRadius(cornerRadiusPx - 0.5f, cornerRadiusPx - 0.5f),
          style = Stroke(width = 1f)
        )
      },
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = config.icon,
      contentDescription = null,
      tint = config.tint,
      modifier = Modifier.fillMaxSize(0.55f)
    )
  }
}

private data class IconConfig(
  val bgGradient: List<Color>,
  val icon: ImageVector,
  val tint: Color
)
