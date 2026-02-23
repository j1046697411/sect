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
            text = "宗门总览",
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
                        Text("宗门名称: ${info.name}", style = MaterialTheme.typography.titleMedium)
                        Text("灵石储备: ${info.spiritStones}", style = MaterialTheme.typography.bodyLarge)
                        Text("贡献点: ${info.contributionPoints}", style = MaterialTheme.typography.bodyLarge)
                        Text("当前时间: ${info.currentYear}年${info.currentMonth}月${info.currentDay}日", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            is SectViewModel.SectInfoUiState.Error -> {
                Text("错误: ${state.message}", color = MaterialTheme.colorScheme.error)
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
                            text = "弟子统计",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 职务分布
                        Text("职务分布:", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatCard(value = "${stats.totalCount}", label = "总数", modifier = Modifier.weight(1f))
                            StatCard(value = "${stats.elderCount}", label = "长老", modifier = Modifier.weight(1f))
                            StatCard(value = "${stats.innerCount}", label = "内门", modifier = Modifier.weight(1f))
                            StatCard(value = "${stats.outerCount}", label = "外门", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 境界分布
                        Text("境界分布:", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatCard(value = "${stats.qiRefiningCount}", label = "炼气", modifier = Modifier.weight(1f))
                            StatCard(value = "${stats.foundationCount}", label = "筑基", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            is SectViewModel.DiscipleStatsUiState.Error -> {
                Text("错误: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
