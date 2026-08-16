package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.model.SystemAppId
import java.util.concurrent.ConcurrentHashMap

/**
 * Dynamically samples edge/corner pixels of system icon drawables to determine the exact background color.
 */
object IconEdgeColorExtractor {
  private val cache = ConcurrentHashMap<SystemAppId, Color>()

  fun getEdgeColor(context: Context, appId: SystemAppId): Color {
    return cache.getOrPut(appId) {
      extractColorFromDrawable(context, appId)
    }
  }

  private fun extractColorFromDrawable(context: Context, appId: SystemAppId): Color {
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
    } ?: return Color(0xFF20232A)

    return try {
      val options = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.ARGB_8888
      }
      val bitmap = BitmapFactory.decodeResource(context.resources, resId, options) ?: return Color(0xFF20232A)

      val width = bitmap.width
      val height = bitmap.height
      if (width <= 0 || height <= 0) return Color(0xFF20232A)

      // Sample along the 4 borders inside the squircle perimeter (at ~10% from edges)
      val samplePoints = listOf(
        Pair(width / 2, (height * 0.08f).toInt().coerceIn(0, height - 1)),
        Pair(width / 2, (height * 0.92f).toInt().coerceIn(0, height - 1)),
        Pair((width * 0.08f).toInt().coerceIn(0, width - 1), height / 2),
        Pair((width * 0.92f).toInt().coerceIn(0, width - 1), height / 2),
        Pair((width * 0.15f).toInt().coerceIn(0, width - 1), (height * 0.15f).toInt().coerceIn(0, height - 1)),
        Pair((width * 0.85f).toInt().coerceIn(0, width - 1), (height * 0.85f).toInt().coerceIn(0, height - 1))
      )

      var totalR = 0L
      var totalG = 0L
      var totalB = 0L
      var count = 0

      for (pt in samplePoints) {
        val pixel = bitmap.getPixel(pt.first, pt.second)
        val a = (pixel shr 24) and 0xff
        if (a > 100) { // Ignore transparent pixels
          val r = (pixel shr 16) and 0xff
          val g = (pixel shr 8) and 0xff
          val b = pixel and 0xff
          totalR += r
          totalG += g
          totalB += b
          count++
        }
      }

      if (count > 0) {
        Color(
          red = (totalR / count) / 255f,
          green = (totalG / count) / 255f,
          blue = (totalB / count) / 255f,
          alpha = 1f
        )
      } else {
        Color(0xFF20232A)
      }
    } catch (_: Exception) {
      Color(0xFF20232A)
    }
  }
}
