package cn.jzl.sect.components.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.viewmodel.DiscipleUiModel

/**
 * 获取职务图标
 */
fun getPositionIcon(position: SectPositionType): String {
    return when (position) {
        SectPositionType.LEADER -> "👑"
        SectPositionType.ELDER -> "🎓"
        SectPositionType.DISCIPLE_INNER -> "⭐"
        SectPositionType.DISCIPLE_OUTER -> "○"
    }
}

/**
 * 弟子卡片组件
 */
@Composable
fun DiscipleCard(
    disciple: DiscipleUiModel,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val positionColor = when (disciple.position) {
        SectPositionType.LEADER -> MaterialTheme.colorScheme.primary
        SectPositionType.ELDER -> MaterialTheme.colorScheme.tertiary
        SectPositionType.DISCIPLE_INNER -> MaterialTheme.colorScheme.secondary
        SectPositionType.DISCIPLE_OUTER -> MaterialTheme.colorScheme.surfaceVariant
    }

    // 行为状态颜色
    val behaviorColor = when (disciple.currentBehavior) {
        "修炼中" -> MaterialTheme.colorScheme.primary
        "工作中" -> MaterialTheme.colorScheme.tertiary
        "休息中" -> MaterialTheme.colorScheme.secondary
        "社交中" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // 选中状态边框颜色
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 顶部：名字和境界
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 名字
                Text(
                    text = disciple.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 境界
                Text(
                    text = disciple.realmDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 第二行：职务图标、标签和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 职务图标和标签
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 职务图标
                    Text(
                        text = getPositionIcon(disciple.position),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Surface(
                        color = positionColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = disciple.positionDisplay,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = positionColor
                        )
                    }
                }

                // 当前状态（行为）
                Surface(
                    color = behaviorColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = disciple.currentBehavior,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = behaviorColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // 修为进度（游戏风格）
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📈 修为",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(disciple.cultivationProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAB47BC)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                GameCultivationBar(
                    progress = disciple.cultivationProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 生命和精力（游戏风格）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 生命值（游戏风格血条）
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "❤ ${disciple.health}/${disciple.maxHealth}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (disciple.health < disciple.maxHealth * 0.3f) {
                                Color(0xFFB71C1C)
                            } else {
                                Color(0xFFE53935)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    GameHealthBar(
                        progress = disciple.health.toFloat() / disciple.maxHealth.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        isLow = disciple.health < disciple.maxHealth * 0.3f
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 精力值（游戏风格能量条）
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ ${disciple.spirit}/${disciple.maxSpirit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0288D1)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    GameEnergyBar(
                        progress = disciple.spirit.toFloat() / disciple.maxSpirit.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

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

/**
 * 游戏风格能量条组件
 */
@Composable
fun GameEnergyBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "energy_progress"
    )

    Box(
        modifier = modifier
            .height(12.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFF333333))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF29B6F6).copy(alpha = 0.8f),
                            Color(0xFF0288D1),
                            Color(0xFF29B6F6).copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.2f))
        )
    }
}

/**
 * 游戏风格修为进度条组件
 */
@Composable
fun GameCultivationBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "cultivation_progress"
    )

    Box(
        modifier = modifier
            .height(12.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFF333333))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFAB47BC).copy(alpha = 0.8f),
                            Color(0xFF7B1FA2),
                            Color(0xFFAB47BC).copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.2f))
        )
    }
}
