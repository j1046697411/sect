package cn.jzl.sect.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * 记住窗口尺寸分类
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize

    return remember(windowSize) {
        val widthDp = with(density) { windowSize.width.toDp() }
        when {
            widthDp < 600.dp -> WindowSizeClass.COMPACT
            widthDp < 1200.dp -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }
    }
}
