package com.example.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Job

enum class SystemAppId {
  DIALER,       // Телефон
  MESSAGES,     // Сообщения
  BROWSER,      // Браузер
  CAMERA,       // Камера
  CALENDAR,     // Календарь
  CLOCK,        // Часы
  PHOTOS,       // Галерея / Фото
  SETTINGS,     // Настройки
  MUSIC,        // Музыка
  FILE_MANAGER, // Файлы
  CALCULATOR,   // Калькулятор
  COMPASS,      // Компас
  PLACEHOLDER   // Заполнитель для будущих приложений
}

data class DesktopAppItem(
  val id: SystemAppId,
  val title: String,
  val isLocked: Boolean = true
)

data class OSStatusState(
  val timeString: String = "09:41",
  val batteryLevel: Int = 92,
  val isCharging: Boolean = false,
  val wifiSignal: Int = 4,
  val cellularSignal: Int = 4,
  val carrierName: String = "5G"
)

/**
 * Geometric bounding rectangle for opening/closing app animations.
 */
data class AppRect(
  val x: Float = 0f,
  val y: Float = 0f,
  val width: Float = 0f,
  val height: Float = 0f
)

/**
 * Unified dark-gray placeholder theme for application interiors.
 */
object AppThemeColors {
  val AppInteriorDarkGray = Color(0xFF20232A)
  val AppInteriorCardGray = Color(0xFF2C303B)
  val AppInteriorHeaderGray = Color(0xFF383D4A)
}

/**
 * Per-app persistent animation session.
 * Tracks progress (0f..1f), position anchors, drag offsets, and active animation jobs.
 */
class AppWindowSession(
  val appId: SystemAppId,
  var originRect: AppRect,
  val progress: Animatable<Float, AnimationVector1D> = Animatable(0f),
  var animJob: Job? = null
)
