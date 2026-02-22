package cn.jzl.sect.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 状态类型枚举
 */
enum class StatusType {
    SUCCESS,    // 成功/正常
    WARNING,    // 警告
    ERROR,      // 错误/危险
    INFO,       // 信息
    PENDING,    // 待处理
    PROCESSING  // 进行中
}

/**
 * 状态徽章组件
 * 用于显示状态标签，如"修炼中"、"已完成"等
 *
 * @param text 状态文本
 * @param type 状态类型
 * @param modifier 修饰符
 */
@Composable
fun StatusBadge(
    text: String,
    type: StatusType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (type) {
        StatusType.SUCCESS -> Pair(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary
        )
        StatusType.WARNING -> Pair(
            Color(0xFFFFA000).copy(alpha = 0.15f),
            Color(0xFFFF6F00)
        )
        StatusType.ERROR -> Pair(
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error
        )
        StatusType.INFO -> Pair(
            Color(0xFF2196F3).copy(alpha = 0.15f),
            Color(0xFF1976D2)
        )
        StatusType.PENDING -> Pair(
            Color(0xFF9E9E9E).copy(alpha = 0.15f),
            Color(0xFF616161)
        )
        StatusType.PROCESSING -> Pair(
            Color(0xFF9C27B0).copy(alpha = 0.15f),
            Color(0xFF7B1FA2)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * 带图标的状态徽章
 *
 * @param text 状态文本
 * @param type 状态类型
 * @param icon 图标Composable
 * @param modifier 修饰符
 */
@Composable
fun StatusBadgeWithIcon(
    text: String,
    type: StatusType,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (type) {
        StatusType.SUCCESS -> Pair(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary
        )
        StatusType.WARNING -> Pair(
            Color(0xFFFFA000).copy(alpha = 0.15f),
            Color(0xFFFF6F00)
        )
        StatusType.ERROR -> Pair(
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error
        )
        StatusType.INFO -> Pair(
            Color(0xFF2196F3).copy(alpha = 0.15f),
            Color(0xFF1976D2)
        )
        StatusType.PENDING -> Pair(
            Color(0xFF9E9E9E).copy(alpha = 0.15f),
            Color(0xFF616161)
        )
        StatusType.PROCESSING -> Pair(
            Color(0xFF9C27B0).copy(alpha = 0.15f),
            Color(0xFF7B1FA2)
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
