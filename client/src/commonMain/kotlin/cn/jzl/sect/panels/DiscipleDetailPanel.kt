package cn.jzl.sect.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.jzl.sect.components.InfoRow
import cn.jzl.sect.components.bars.GameCultivationBar
import cn.jzl.sect.components.bars.GameEnergyBar
import cn.jzl.sect.components.bars.GameHealthBar
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.viewmodel.DiscipleUiModel

/**
 * 弟子详情面板组件
 */
@Composable
fun DiscipleDetailPanel(
    disciple: DiscipleUiModel,
    onClose: () -> Unit
) {
    val positionColor = when (disciple.position) {
        SectPositionType.LEADER -> MaterialTheme.colorScheme.primary
        SectPositionType.ELDER -> MaterialTheme.colorScheme.tertiary
        SectPositionType.DISCIPLE_INNER -> MaterialTheme.colorScheme.secondary
        SectPositionType.DISCIPLE_OUTER -> MaterialTheme.colorScheme.surfaceVariant
    }

    Column {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👤 弟子详情",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onClose) {
                Text("✕", style = MaterialTheme.typography.titleMedium)
            }
        }

        Divider()

        // 基本信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = disciple.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 境界和状态
        InfoRow("境界", disciple.realmDisplay)
        InfoRow("状态", disciple.currentBehavior)

        Divider()

        // 修为详情
        Text(
            text = "📈 修为详情",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        InfoRow("当前修为", "${disciple.cultivation}/${disciple.maxCultivation}")
        InfoRow("突破进度", "${(disciple.cultivationProgress * 100).toInt()}%")
        GameCultivationBar(
            progress = disciple.cultivationProgress,
            modifier = Modifier.fillMaxWidth()
        )

        Divider()

        // 生命和精力
        Text(
            text = "❤ 生命值",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFFE53935)
        )
        InfoRow("当前", "${disciple.health}/${disciple.maxHealth}")
        GameHealthBar(
            progress = disciple.health.toFloat() / disciple.maxHealth.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            isLow = disciple.health < disciple.maxHealth * 0.3f
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "⚡ 精力值",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF0288D1)
        )
        InfoRow("当前", "${disciple.spirit}/${disciple.maxSpirit}")
        GameEnergyBar(
            progress = disciple.spirit.toFloat() / disciple.maxSpirit.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

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
