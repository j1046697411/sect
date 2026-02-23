package cn.jzl.sect.components.bars

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 游戏风格血条组件
 */
@Composable
fun GameHealthBar(
    progress: Float,
    modifier: Modifier = Modifier,
    isLow: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "health_progress"
    )

    val color = if (isLow) {
        Color(0xFFB71C1C) // 深红色警告
    } else {
        Color(0xFFE53935) // 红色
    }

    Box(
        modifier = modifier
            .height(12.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFF333333))
    ) {
        // 血条填充
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.8f),
                            color,
                            color.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // 光泽效果
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.2f))
        )
    }
}
