package cn.jzl.sect.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.jzl.sect.components.InfoRow
import cn.jzl.sect.viewmodel.DiscipleViewModel
import cn.jzl.sect.viewmodel.GameViewModel
import cn.jzl.sect.viewmodel.SectViewModel

/**
 * 右侧面板组件
 */
@Composable
fun RightPanel(
    sectViewModel: SectViewModel,
    discipleViewModel: DiscipleViewModel,
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val sectInfo by sectViewModel.sectInfo.collectAsState()
    val discipleStats by sectViewModel.discipleStats.collectAsState()
    val gameState by gameViewModel.gameState.collectAsState()
    val gameSpeed by gameViewModel.gameSpeed.collectAsState()
    val detailedTime by gameViewModel.detailedGameTime.collectAsState()
    val resourceProduction by gameViewModel.resourceProduction.collectAsState()
    val selectedDisciple by discipleViewModel.selectedDisciple.collectAsState()

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 如果有选中的弟子，显示详情；否则显示默认信息
            if (selectedDisciple != null) {
                DiscipleDetailPanel(
                    disciple = selectedDisciple!!,
                    onClose = { discipleViewModel.clearSelection() }
                )
            } else {
                // 实时游戏时间
                Text(
                    text = "⏰ 游戏时间",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                detailedTime?.let { time ->
                    Text(
                        text = "第${time.year}年 ${time.month}月 ${time.day}日",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${time.timeOfDay} ⚡ ${gameSpeed.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: Text("加载中...", style = MaterialTheme.typography.bodySmall)

                Divider()

                // 资源产量
                Text(
                    text = "💰 资源产量",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                resourceProduction?.let { production ->
                    InfoRow("灵石", "+${production.spiritStonesPerHour}/小时")
                    InfoRow("贡献点", "+${production.contributionPointsPerHour}/小时")
                } ?: Text("计算中...", style = MaterialTheme.typography.bodySmall)

                Divider()

                // 宗门信息
                Text(
                    text = "🏯 宗门信息",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                when (val state = sectInfo) {
                    is SectViewModel.SectInfoUiState.Success -> {
                        InfoRow("名称", state.data.name)
                        InfoRow("灵石", "${state.data.spiritStones}")
                        InfoRow("贡献点", "${state.data.contributionPoints}")
                    }
                    else -> {
                        Text("加载中...", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Divider()

                // 弟子统计
                Text(
                    text = "👥 弟子统计",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                when (val state = discipleStats) {
                    is SectViewModel.DiscipleStatsUiState.Success -> {
                        InfoRow("总数", "${state.data.totalCount}")
                        InfoRow("内门", "${state.data.innerCount}")
                        InfoRow("外门", "${state.data.outerCount}")
                        InfoRow("长老", "${state.data.elderCount}")
                    }
                    else -> {
                        Text("加载中...", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Divider()

                // 快速操作
                Text(
                    text = "⚡ 快速操作",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { /* 功能开发中 */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                ) {
                    Text("发布选拔任务(开发中)")
                }
            }
        }
    }
}
