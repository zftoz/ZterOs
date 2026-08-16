package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.AppRect
import com.example.model.AppThemeColors
import com.example.model.AppWindowSession
import com.example.model.OSStatusState
import com.example.model.SystemAppId
import com.example.ui.components.DesktopDock
import com.example.ui.components.DesktopGrid
import com.example.ui.components.DeviceFrame
import com.example.ui.components.OSNavigationBar
import com.example.ui.components.OSStatusBar
import com.example.ui.components.icons.SystemAppIconGraphic
import com.example.util.IconEdgeColorExtractor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

// Precision continuous OS physics curves:
// Open: Continuous deceleration from icon into full screen frame
private val SmoothAppLaunchEasing = CubicBezierEasing(0.24f, 0.96f, 0.22f, 1.0f)
// Return: Decelerates directly and cleanly into the anchor icon
private val SmoothAppReturnEasing = CubicBezierEasing(0.25f, 0.90f, 0.24f, 1.0f)

// Calibrated smooth duration scale (buttery smooth, rapid and zero glitch)
private const val BASE_LAUNCH_DURATION_MS = 560
private const val BASE_RETURN_DURATION_MS = 480

@Composable
fun OSSimulatorScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val coroutineScope = rememberCoroutineScope()

  // Dynamic system time & battery state
  var currentTimeString by remember { mutableStateOf("09:41") }
  val batteryLevel by remember { mutableIntStateOf(92) }

  // System clock ticker
  LaunchedEffect(Unit) {
    while (true) {
      val now = Date()
      val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
      currentTimeString = timeFormat.format(now)
      delay(30_000)
    }
  }

  val statusState = remember(currentTimeString, batteryLevel) {
    OSStatusState(
      timeString = currentTimeString,
      batteryLevel = batteryLevel,
      carrierName = "5G"
    )
  }

  // Registered icon screen positions in root coordinate space
  val iconRectMap = remember { mutableStateMapOf<SystemAppId, AppRect>() }

  // Screen bounds
  var screenBounds by remember { mutableStateOf(AppRect(0f, 0f, 0f, 0f)) }

  // Persistent session instances per app ID (prevents duplicate windows)
  val sessionMap = remember { mutableStateMapOf<SystemAppId, AppWindowSession>() }

  // Ordered stack of active animating/opened apps (bottom to top)
  val activeAppOrder = remember { mutableStateListOf<SystemAppId>() }

  // Live gesture pull offsets (dynamic physical upward translation)
  val gestureOffsetY = remember { Animatable(0f) }
  val gestureOffsetX = remember { Animatable(0f) }

  // Helper to obtain or initialize a stable session for any app
  fun getOrCreateSession(appId: SystemAppId, originRect: AppRect): AppWindowSession {
    return sessionMap.getOrPut(appId) {
      AppWindowSession(
        appId = appId,
        originRect = originRect,
        progress = Animatable(0f)
      )
    }.apply {
      this.originRect = originRect
    }
  }

  // Helper to check if an app is currently animating or expanded
  fun isAppActive(appId: SystemAppId): Boolean {
    val session = sessionMap[appId] ?: return false
    return session.progress.value > 0.001f
  }

  // Close the top active application smoothly back to its own icon anchor
  fun closeTopApp() {
    val topAppId = activeAppOrder.lastOrNull() ?: return
    val topSession = sessionMap[topAppId] ?: return

    topSession.animJob?.cancel()
    val currentP = topSession.progress.value
    val closeDuration = (BASE_RETURN_DURATION_MS * currentP).roundToInt().coerceIn(160, BASE_RETURN_DURATION_MS)

    // Gracefully animate gesture translation back to 0 as the window shrinks into the icon
    coroutineScope.launch {
      gestureOffsetY.animateTo(0f, tween(closeDuration, easing = SmoothAppReturnEasing))
    }
    coroutineScope.launch {
      gestureOffsetX.animateTo(0f, tween(closeDuration, easing = SmoothAppReturnEasing))
    }

    topSession.animJob = coroutineScope.launch {
      topSession.progress.animateTo(
        targetValue = 0f,
        animationSpec = tween(
          durationMillis = closeDuration,
          easing = SmoothAppReturnEasing
        )
      )
      activeAppOrder.remove(topAppId)
    }
  }

  // Clean, spam-safe, race-condition-free AB app switcher and launcher
  fun launchApp(appId: SystemAppId) {
    val originRect = iconRectMap[appId] ?: AppRect(
      x = screenBounds.x + (screenBounds.width / 2f) - (27f * density.density),
      y = screenBounds.y + (screenBounds.height / 2f) - (27f * density.density),
      width = 54f * density.density,
      height = 54f * density.density
    )

    val targetSession = getOrCreateSession(appId, originRect)

    // If it's already completely open and is top of stack, do nothing
    if (targetSession.progress.value >= 0.999f && activeAppOrder.lastOrNull() == appId) {
      return
    }

    // Reset gesture offsets on launch
    coroutineScope.launch {
      gestureOffsetY.snapTo(0f)
      gestureOffsetX.snapTo(0f)
    }

    // Bring clicked app to the top of the render stack
    activeAppOrder.remove(appId)
    activeAppOrder.add(appId)

    // Smoothly close any OTHER open or opening apps in parallel into their own icon anchors
    activeAppOrder.toList().forEach { otherAppId ->
      if (otherAppId != appId) {
        val otherSession = sessionMap[otherAppId]
        if (otherSession != null && otherSession.progress.value > 0f) {
          otherSession.animJob?.cancel()
          val currentP = otherSession.progress.value
          val closeDuration = (BASE_RETURN_DURATION_MS * currentP).roundToInt().coerceIn(160, BASE_RETURN_DURATION_MS)

          otherSession.animJob = coroutineScope.launch {
            otherSession.progress.animateTo(
              targetValue = 0f,
              animationSpec = tween(durationMillis = closeDuration, easing = SmoothAppReturnEasing)
            )
            // Clean up from active render order when fully snapped back into icon
            if (otherSession.progress.value <= 0.001f && activeAppOrder.lastOrNull() != otherAppId) {
              activeAppOrder.remove(otherAppId)
            }
          }
        }
      }
    }

    // Animate target app towards 1.0f starting seamlessly from its CURRENT progress (smooth reversal if already closing)
    targetSession.animJob?.cancel()
    val currentP = targetSession.progress.value
    val remainingDist = 1f - currentP
    val openDuration = (BASE_LAUNCH_DURATION_MS * remainingDist).roundToInt().coerceIn(160, BASE_LAUNCH_DURATION_MS)

    targetSession.animJob = coroutineScope.launch {
      targetSession.progress.animateTo(
        targetValue = 1f,
        animationSpec = tween(
          durationMillis = openDuration,
          easing = SmoothAppLaunchEasing
        )
      )
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("os_simulation_root")
  ) {
    // Smartphone device frame
    DeviceFrame(
      modifier = Modifier.fillMaxSize()
    ) {
      BoxWithConstraints(
        modifier = Modifier
          .fillMaxSize()
          .clipToBounds() // Strictly clamp all animations inside the phone display perimeter
          .testTag("desktop_screen_canvas")
          .onGloballyPositioned { coords: LayoutCoordinates ->
            val pos = coords.positionInRoot()
            val s = coords.size
            if (s.width > 0 && s.height > 0) {
              screenBounds = AppRect(pos.x, pos.y, s.width.toFloat(), s.height.toFloat())
            }
          }
      ) {
        val topAppId = activeAppOrder.lastOrNull()
        val maxProgress = activeAppOrder.mapNotNull { sessionMap[it]?.progress?.value }.maxOrNull() ?: 0f

        // Gesture lift drag calculation
        val fullH = screenBounds.height.coerceAtLeast(1f)
        val fullW = screenBounds.width.coerceAtLeast(1f)
        val currentDragY = gestureOffsetY.value
        val dragFraction = (abs(currentDragY) / (fullH * 0.45f)).coerceIn(0f, 1f)

        // Desktop wallpaper & icons blur + subtle scale down (reveals desktop during gesture drag)
        val effectiveProgress = (maxProgress - (dragFraction * 0.4f)).coerceIn(0f, 1f)
        val desktopScale = 1f - (effectiveProgress * 0.05f)
        val desktopBlur = (effectiveProgress * 12f).dp

        // Desktop Workspace (Wallpaper, Status Bar, Grid, Dock)
        Box(
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
              scaleX = desktopScale
              scaleY = desktopScale
            }
            .blur(desktopBlur)
        ) {
          SelfContainedBlueWallpaper()

          Column(
            modifier = Modifier.fillMaxSize()
          ) {
            // Status Bar snug at top
            OSStatusBar(
              statusState = statusState,
              modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 2.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Desktop Grid
            DesktopGrid(
              pageCount = 2,
              rowsPerPage = 4,
              columnsPerRow = 4,
              modifier = Modifier
                .weight(1f)
                .padding(horizontal = 2.dp),
              isAppActiveOrAnimating = { appId -> isAppActive(appId) },
              onAppPositioned = { appId, rect ->
                iconRectMap[appId] = rect
              },
              onAppClick = { appId ->
                launchApp(appId)
              }
            )

            // Pinned Bottom Dock
            DesktopDock(
              apps = listOf(
                SystemAppId.DIALER,
                SystemAppId.MESSAGES,
                SystemAppId.BROWSER,
                SystemAppId.CAMERA
              ),
              isAppActiveOrAnimating = { appId -> isAppActive(appId) },
              onAppPositioned = { appId, rect ->
                iconRectMap[appId] = rect
              },
              onAppClick = { appId ->
                launchApp(appId)
              }
            )

            // Bottom spacer (gesture bar lives in universal top overlay)
            Spacer(modifier = Modifier.height(30.dp))
          }
        }

        // =========================================================================
        // PARALLEL MULTI-LAYER APP WINDOWS (Stretches directly from original icon)
        // Stays strictly within phone borders with square-to-rectangular expansion
        // =========================================================================
        activeAppOrder.forEach { appId ->
          val session = sessionMap[appId]
          if (session != null) {
            val p = session.progress.value
            if (p > 0.0005f) {
              val origin = session.originRect

              // Coordinates in local screen canvas
              val originX = if (screenBounds.width > 0f) origin.x - screenBounds.x else 0f
              val originY = if (screenBounds.height > 0f) origin.y - screenBounds.y else 0f
              val originW = origin.width.coerceAtLeast(36f * density.density)
              val originH = origin.height.coerceAtLeast(36f * density.density)

              val isTop = (appId == topAppId)

              // Dynamic aspect ratio expansion physics:
              // Maintains a balanced, slightly square-rounded card aspect during opening/closing
              val widthProgress = p.toDouble().pow(0.88).toFloat()
              val heightProgress = p.toDouble().pow(0.96).toFloat()

              val currentW = min(fullW, originW + (fullW - originW) * widthProgress)
              val currentH = min(fullH, originH + (fullH - originH) * heightProgress)

              // Dynamic center calculation: smoothly travels from icon center to screen center
              val originCenterX = originX + (originW / 2f)
              val originCenterY = originY + (originH / 2f)
              val screenCenterX = fullW / 2f
              val screenCenterY = fullH / 2f

              val currentCenterX = originCenterX + (screenCenterX - originCenterX) * p
              val currentCenterY = originCenterY + (screenCenterY - originCenterY) * p

              val rawX = currentCenterX - (currentW / 2f)
              val rawY = currentCenterY - (currentH / 2f)

              // Strict border containment horizontally, with gesture lift vertically
              val clampedX = rawX.coerceIn(0f, max(0f, fullW - currentW))
              val baseClampedY = rawY.coerceIn(0f, max(0f, fullH - currentH))

              // Gesture translation and scale applied strictly to the top window when dragged
              val topGestureY = if (isTop) gestureOffsetY.value * 0.90f else 0f
              val topGestureX = if (isTop) gestureOffsetX.value * 0.35f else 0f
              val topWindowScale = if (isTop) (1f - (dragFraction * 0.12f)) else 1f

              // Dynamically sample the EXACT edge pixels of the actual icon drawable
              val iconEdgeColor = IconEdgeColorExtractor.getEdgeColor(context, session.appId)

              // Smooth color morph: starts at 100% exact icon border pixel color, morphs smoothly into unified dark gray body
              val windowBgColor = lerp(
                start = iconEdgeColor,
                stop = AppThemeColors.AppInteriorDarkGray,
                fraction = ((p - 0.10f) / 0.70f).coerceIn(0f, 1f)
              )

              // Smooth corner radius morphing from icon squircle (23%) to phone screen inner curvature (26dp)
              // During gesture lift, corner radius increases slightly for a refined floating card effect
              val iconRadiusPx = originW * 0.23f
              val screenRadiusPx = with(density) { (26.dp + 6.dp * (if (isTop) dragFraction else 0f)).toPx() }
              val currentRadiusPx = iconRadiusPx + (screenRadiusPx - iconRadiusPx) * p
              val currentRadiusDp = with(density) { currentRadiusPx.toDp() }

              // Fast, natural icon graphic fade-out and unified dark-gray interior fade-in
              val iconOpacity = (1f - (p * 2.8f)).coerceIn(0f, 1f)
              val appBodyOpacity = ((p - 0.22f) * 1.35f).coerceIn(0f, 1f)

              val curWdp = with(density) { currentW.toDp() }
              val curHdp = with(density) { currentH.toDp() }

              Box(
                modifier = Modifier
                  .offset {
                    IntOffset(
                      (clampedX + topGestureX).roundToInt(),
                      (baseClampedY + topGestureY).roundToInt()
                    )
                  }
                  .size(width = curWdp, height = curHdp)
                  .graphicsLayer {
                    scaleX = topWindowScale
                    scaleY = topWindowScale
                    transformOrigin = TransformOrigin(0.5f, 0.85f)
                  }
                  .shadow(
                    elevation = (14f * p + 8f * (if (isTop) dragFraction else 0f)).dp,
                    shape = RoundedCornerShape(currentRadiusDp),
                    ambientColor = Color.Black.copy(alpha = 0.40f),
                    spotColor = Color.Black.copy(alpha = 0.55f)
                  )
                  .clip(RoundedCornerShape(currentRadiusDp))
                  .background(windowBgColor)
                  .testTag("app_window_${session.appId.name.lowercase()}")
              ) {
                // Unified Dark-Gray App Body Surface
                if (appBodyOpacity > 0.001f) {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .alpha(appBodyOpacity)
                  ) {
                    DarkGrayApplicationMockBody(
                      statusState = statusState
                    )
                  }
                }

                // Morphing Icon Graphic Layer (The icon itself stretching from grid)
                if (iconOpacity > 0.001f) {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .alpha(iconOpacity),
                    contentAlignment = Alignment.Center
                  ) {
                    SystemAppIconGraphic(
                      appId = session.appId,
                      modifier = Modifier.fillMaxSize()
                    )
                  }
                }
              }
            }
          }
        }

        // =========================================================================
        // UNIVERSAL TOP-LEVEL GESTURE BAR
        // Always mounted at the highest Z-index over the entire OS (Desktop & Apps)
        // =========================================================================
        Box(
          modifier = Modifier
            .fillMaxSize(),
          contentAlignment = Alignment.BottomCenter
        ) {
          OSNavigationBar(
            pillColor = Color.White.copy(alpha = 0.92f),
            onDragStart = {
              // User touched gesture bar
            },
            onDragDelta = { deltaY, deltaX ->
              val currentTopId = activeAppOrder.lastOrNull()
              if (currentTopId != null) {
                coroutineScope.launch {
                  // Only translate the entire window upward with rubberband damping
                  val newY = (gestureOffsetY.value + deltaY).coerceAtMost(0f)
                  val newX = gestureOffsetX.value + deltaX * 0.35f
                  gestureOffsetY.snapTo(newY)
                  gestureOffsetX.snapTo(newX)
                }
              }
            },
            onDragReleased = { totalDragY, _ ->
              val currentTopId = activeAppOrder.lastOrNull()
              if (currentTopId != null) {
                // If flicked or dragged up past threshold (-35px), trigger clean close animation to icon
                if (totalDragY < -35f || gestureOffsetY.value < -35f) {
                  closeTopApp()
                } else {
                  // Snap window translation smoothly back to resting position (0) without closing
                  coroutineScope.launch {
                    gestureOffsetY.animateTo(0f, tween(240, easing = SmoothAppLaunchEasing))
                  }
                  coroutineScope.launch {
                    gestureOffsetX.animateTo(0f, tween(240, easing = SmoothAppLaunchEasing))
                  }
                }
              }
            },
            onTapGesture = {
              closeTopApp()
            }
          )
        }
      }
    }
  }
}

/**
 * Unified Dark-Gray App Interior Mock:
 * Clean, consistent dark-slate (#20232A) interior across all applications.
 */
@Composable
private fun DarkGrayApplicationMockBody(
  statusState: OSStatusState,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppThemeColors.AppInteriorDarkGray)
  ) {
    // Status Bar inside application
    OSStatusBar(
      statusState = statusState,
      modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 2.dp)
    )

    // Clean Dark-Gray Application Content Area
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Top header banner placeholder
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(AppThemeColors.AppInteriorHeaderGray)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Content item placeholders
      repeat(4) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppThemeColors.AppInteriorCardGray)
        )
      }
    }

    // Bottom space reserved for universal gesture bar
    Spacer(modifier = Modifier.height(30.dp))
  }
}

/**
 * Pure self-contained blue wallpaper with smooth ambient lighting curves.
 */
@Composable
private fun SelfContainedBlueWallpaper(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.linearGradient(
          colors = listOf(
            Color(0xFF0F1E33),
            Color(0xFF162D4A),
            Color(0xFF1E3E64),
            Color(0xFF183150),
            Color(0xFF0B1626)
          ),
          start = Offset(0f, 0f),
          end = Offset(600f, 1200f)
        )
      )
      .drawBehind {
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0x3538BDF8),
              Color(0x180284C7),
              Color.Transparent
            ),
            center = Offset(size.width * 0.75f, size.height * 0.35f),
            radius = size.width * 0.95f
          )
        )

        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0x2860A5FA),
              Color(0x101D4ED8),
              Color.Transparent
            ),
            center = Offset(size.width * 0.25f, size.height * 0.7f),
            radius = size.width * 0.85f
          )
        )
      }
  )
}
