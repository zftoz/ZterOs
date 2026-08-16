package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.model.AppRect
import com.example.model.SystemAppId
import kotlinx.coroutines.launch

@Composable
fun DesktopGrid(
  modifier: Modifier = Modifier,
  pageCount: Int = 2,
  rowsPerPage: Int = 4,
  columnsPerRow: Int = 4,
  isAppActiveOrAnimating: (SystemAppId) -> Boolean = { false },
  onAppPositioned: (SystemAppId, AppRect) -> Unit = { _, _ -> },
  onAppClick: (SystemAppId) -> Unit = {}
) {
  val pagerState = rememberPagerState(pageCount = { pageCount })
  val coroutineScope = rememberCoroutineScope()

  val page1Grid: List<List<SystemAppId?>> = listOf(
    listOf(SystemAppId.CALENDAR, SystemAppId.CLOCK, SystemAppId.PHOTOS, SystemAppId.SETTINGS),
    listOf(SystemAppId.MUSIC, SystemAppId.FILE_MANAGER, SystemAppId.CALCULATOR, SystemAppId.COMPASS),
    listOf(null, null, null, null),
    listOf(null, null, null, null)
  )

  val page2Grid: List<List<SystemAppId?>> = listOf(
    listOf(SystemAppId.PLACEHOLDER, SystemAppId.PLACEHOLDER, SystemAppId.PLACEHOLDER, SystemAppId.PLACEHOLDER),
    listOf(SystemAppId.PLACEHOLDER, SystemAppId.PLACEHOLDER, null, null),
    listOf(null, null, null, null),
    listOf(null, null, null, null)
  )

  val pages = listOf(page1Grid, page2Grid)

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .testTag("desktop_grid_container")
  ) {
    val computedIconSize = min((maxWidth / 5.2f), 56.dp).coerceAtLeast(36.dp)

    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      HorizontalPager(
        state = pagerState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) { pageIndex ->
        val currentGrid = pages.getOrElse(pageIndex) { page1Grid }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 4.dp),
          verticalArrangement = Arrangement.SpaceEvenly,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          repeat(rowsPerPage) { rowIndex ->
            val rowItems = currentGrid.getOrNull(rowIndex) ?: List(columnsPerRow) { null }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              repeat(columnsPerRow) { colIndex ->
                val appId = rowItems.getOrNull(colIndex)

                if (appId != null) {
                  DesktopAppIcon(
                    appId = appId,
                    size = computedIconSize,
                    testTagId = "desktop_p${pageIndex}_r${rowIndex}_c${colIndex}_${appId.name.lowercase()}",
                    isVisibleOnGrid = !isAppActiveOrAnimating(appId),
                    onPositioned = { rect -> onAppPositioned(appId, rect) },
                    onClick = { onAppClick(appId) }
                  )
                } else {
                  Spacer(modifier = Modifier.size(computedIconSize))
                }
              }
            }
          }
        }
      }

      // Page indicator dots
      Row(
        modifier = Modifier
          .padding(top = 2.dp, bottom = 4.dp)
          .testTag("desktop_page_indicator"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        repeat(pageCount) { index ->
          val isSelected = pagerState.currentPage == index
          val width by animateDpAsState(
            targetValue = if (isSelected) 16.dp else 5.dp,
            label = "indicator_width"
          )
          val alpha = if (isSelected) 0.9f else 0.35f

          Box(
            modifier = Modifier
              .padding(horizontal = 2.5.dp)
              .size(width = width, height = 5.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = alpha))
              .clickable {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(index)
                }
              }
          )
        }
      }
    }
  }
}
