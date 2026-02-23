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
import org.jetbrains.compose.resources.stringResource
import sect.client.generated.resources.Res
import sect.client.generated.resources.unit_contribution_points
import sect.client.generated.resources.label_elder
import sect.client.generated.resources.label_inner
import sect.client.generated.resources.label_name
import sect.client.generated.resources.label_outer
import sect.client.generated.resources.label_total
import sect.client.generated.resources.panel_button_recruitment
import sect.client.generated.resources.panel_calculating
import sect.client.generated.resources.panel_disciple_stats
import sect.client.generated.resources.panel_game_time
import sect.client.generated.resources.panel_loading
import sect.client.generated.resources.panel_quick_actions
import sect.client.generated.resources.panel_resource_production
import sect.client.generated.resources.panel_sect_info
import sect.client.generated.resources.unit_per_hour
import sect.client.generated.resources.unit_spirit_stones

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
                    text = stringResource(Res.string.panel_game_time),
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
                } ?: Text(stringResource(Res.string.panel_loading), style = MaterialTheme.typography.bodySmall)

                Divider()

                // 资源产量
                Text(
                    text = stringResource(Res.string.panel_resource_production),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                resourceProduction?.let { production ->
                    InfoRow(
                        stringResource(Res.string.unit_spirit_stones),
                        "+${production.spiritStonesPerHour}" + stringResource(Res.string.unit_per_hour)
                    )
                    InfoRow(
                        stringResource(Res.string.unit_contribution_points),
                        "+${production.contributionPointsPerHour}" + stringResource(Res.string.unit_per_hour)
                    )
                } ?: Text(stringResource(Res.string.panel_calculating), style = MaterialTheme.typography.bodySmall)

                Divider()

                // 宗门信息
                Text(
                    text = stringResource(Res.string.panel_sect_info),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                when (val state = sectInfo) {
                    is SectViewModel.SectInfoUiState.Success -> {
                        InfoRow(stringResource(Res.string.label_name), state.data.name)
                        InfoRow(
                            stringResource(Res.string.unit_spirit_stones),
                            "${state.data.spiritStones}"
                        )
                        InfoRow(
                            stringResource(Res.string.unit_contribution_points),
                            "${state.data.contributionPoints}"
                        )
                    }
                    else -> {
                        Text(stringResource(Res.string.panel_loading), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Divider()

                // 弟子统计
                Text(
                    text = stringResource(Res.string.panel_disciple_stats),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                when (val state = discipleStats) {
                    is SectViewModel.DiscipleStatsUiState.Success -> {
                        InfoRow(stringResource(Res.string.label_total), "${state.data.totalCount}")
                        InfoRow(stringResource(Res.string.label_inner), "${state.data.innerCount}")
                        InfoRow(stringResource(Res.string.label_outer), "${state.data.outerCount}")
                        InfoRow(stringResource(Res.string.label_elder), "${state.data.elderCount}")
                    }
                    else -> {
                        Text(stringResource(Res.string.panel_loading), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Divider()

                // 快速操作
                Text(
                    text = stringResource(Res.string.panel_quick_actions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { /* 功能开发中 */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                ) {
                    Text(stringResource(Res.string.panel_button_recruitment))
                }
            }
        }
    }
}
