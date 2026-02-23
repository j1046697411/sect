package cn.jzl.sect.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.jzl.sect.components.FilterChip
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.viewmodel.DiscipleUiModel
import cn.jzl.sect.viewmodel.DiscipleViewModel
import org.jetbrains.compose.resources.stringResource
import sect.client.generated.resources.Res
import sect.client.generated.resources.error_prefix
import sect.client.generated.resources.filter_all
import sect.client.generated.resources.label_elder
import sect.client.generated.resources.label_inner
import sect.client.generated.resources.label_outer
import sect.client.generated.resources.no_disciples
import sect.client.generated.resources.page_disciples_title

/**
 * 弟子管理页面
 */
@Composable
fun DisciplesPage(viewModel: DiscipleViewModel) {
    val discipleList by viewModel.discipleList.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(Res.string.page_disciples_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 筛选标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = stringResource(Res.string.filter_all),
                selected = currentFilter is DiscipleViewModel.DiscipleFilter.All,
                onClick = { viewModel.filterByPosition(null) }
            )
            FilterChip(
                label = stringResource(Res.string.label_inner),
                selected = currentFilter is DiscipleViewModel.DiscipleFilter.ByPosition
                    && (currentFilter as? DiscipleViewModel.DiscipleFilter.ByPosition)?.position == SectPositionType.DISCIPLE_INNER,
                onClick = { viewModel.filterByPosition(SectPositionType.DISCIPLE_INNER) }
            )
            FilterChip(
                label = stringResource(Res.string.label_outer),
                selected = currentFilter is DiscipleViewModel.DiscipleFilter.ByPosition
                    && (currentFilter as? DiscipleViewModel.DiscipleFilter.ByPosition)?.position == SectPositionType.DISCIPLE_OUTER,
                onClick = { viewModel.filterByPosition(SectPositionType.DISCIPLE_OUTER) }
            )
            FilterChip(
                label = stringResource(Res.string.label_elder),
                selected = currentFilter is DiscipleViewModel.DiscipleFilter.ByPosition
                    && (currentFilter as? DiscipleViewModel.DiscipleFilter.ByPosition)?.position == SectPositionType.ELDER,
                onClick = { viewModel.filterByPosition(SectPositionType.ELDER) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 弟子卡片网格
        when (val state = discipleList) {
            is DiscipleViewModel.DiscipleListUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is DiscipleViewModel.DiscipleListUiState.Success -> {
                val disciples = state.data
                if (disciples.isEmpty()) {
                    Text(stringResource(Res.string.no_disciples), style = MaterialTheme.typography.bodyLarge)
                } else {
                    // 使用LazyVerticalGrid展示卡片
                    val selectedDiscipleFromVM by viewModel.selectedDisciple.collectAsState()

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(disciples.size) { index ->
                            val disciple = disciples[index]
                            val isSelected = selectedDiscipleFromVM?.id == disciple.id

                            DiscipleCard(
                                disciple = disciple,
                                isSelected = isSelected,
                                onClick = {
                                    viewModel.selectDisciple(disciple)
                                }
                            )
                        }
                    }
                }
            }
            is DiscipleViewModel.DiscipleListUiState.Error -> {
                Text(
                    stringResource(Res.string.error_prefix) + " ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
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
