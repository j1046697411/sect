package cn.jzl.sect.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.jzl.sect.components.StatCard
import cn.jzl.sect.viewmodel.SectViewModel
import org.jetbrains.compose.resources.stringResource
import sect.client.generated.resources.Res
import sect.client.generated.resources.error_prefix
import sect.client.generated.resources.label_elder
import sect.client.generated.resources.label_foundation
import sect.client.generated.resources.label_inner
import sect.client.generated.resources.label_outer
import sect.client.generated.resources.label_qi_refining
import sect.client.generated.resources.label_total
import sect.client.generated.resources.page_overview_title
import sect.client.generated.resources.sect_contribution_points_label
import sect.client.generated.resources.sect_current_time_label
import sect.client.generated.resources.sect_disciple_stats_title
import sect.client.generated.resources.sect_name_label
import sect.client.generated.resources.sect_position_distribution
import sect.client.generated.resources.sect_realm_distribution
import sect.client.generated.resources.sect_spirit_stones_label

/**
 * 宗门总览页面
 */
@Composable
fun OverviewPage(viewModel: SectViewModel) {
    val sectInfo by viewModel.sectInfo.collectAsState()
    val discipleStats by viewModel.discipleStats.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(Res.string.page_overview_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 宗门信息卡片
        when (val state = sectInfo) {
            is SectViewModel.SectInfoUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is SectViewModel.SectInfoUiState.Success -> {
                val info = state.data
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(Res.string.sect_name_label) + " ${info.name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(Res.string.sect_spirit_stones_label) + " ${info.spiritStones}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            stringResource(Res.string.sect_contribution_points_label) + " ${info.contributionPoints}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            stringResource(Res.string.sect_current_time_label) + " ${info.currentYear}年${info.currentMonth}月${info.currentDay}日",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            is SectViewModel.SectInfoUiState.Error -> {
                Text(
                    stringResource(Res.string.error_prefix) + " ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 弟子统计
        when (val state = discipleStats) {
            is SectViewModel.DiscipleStatsUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is SectViewModel.DiscipleStatsUiState.Success -> {
                val stats = state.data
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(Res.string.sect_disciple_stats_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 职务分布
                        Text(
                            stringResource(Res.string.sect_position_distribution),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatCard(
                                value = "${stats.totalCount}",
                                label = stringResource(Res.string.label_total),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = "${stats.elderCount}",
                                label = stringResource(Res.string.label_elder),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = "${stats.innerCount}",
                                label = stringResource(Res.string.label_inner),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = "${stats.outerCount}",
                                label = stringResource(Res.string.label_outer),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 境界分布
                        Text(
                            stringResource(Res.string.sect_realm_distribution),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatCard(
                                value = "${stats.qiRefiningCount}",
                                label = stringResource(Res.string.label_qi_refining),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = "${stats.foundationCount}",
                                label = stringResource(Res.string.label_foundation),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            is SectViewModel.DiscipleStatsUiState.Error -> {
                Text(
                    stringResource(Res.string.error_prefix) + " ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
