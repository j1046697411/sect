package cn.jzl.sect

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.engine.*
import cn.jzl.sect.engine.WorldProvider
import cn.jzl.sect.viewmodel.*

/**
 * 页面类型枚举
 */
enum class PageType {
    OVERVIEW,      // 宗门总览
    DISCIPLES,     // 弟子管理
    QUESTS,        // 任务大厅
    POLICY,        // 政策配置
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    // 初始化World（同步执行，确保在创建ViewModel之前完成）
    remember {
        if (!WorldProvider.isInitialized) {
            WorldProvider.initialize("青云宗")
        }
        true
    }

    MaterialTheme {
        var currentPage by remember { mutableStateOf(PageType.OVERVIEW) }
        // 导航栏展开/折叠状态
        var isNavExpanded by remember { mutableStateOf(true) }

        // 创建ViewModel（此时World已初始化）
        val gameViewModel: GameViewModel = viewModel { GameViewModel() }
        val sectViewModel: SectViewModel = viewModel { SectViewModel() }
        val discipleViewModel: DiscipleViewModel = viewModel { DiscipleViewModel() }

        // 游戏状态
        val gameState by gameViewModel.gameState.collectAsState()
        val gameSpeed by gameViewModel.gameSpeed.collectAsState()
        val currentTime by gameViewModel.currentTime.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("宗门修真录 - $currentTime") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        // 游戏速度控制
                        GameSpeedControl(
                            gameState = gameState,
                            gameSpeed = gameSpeed,
                            onPause = { gameViewModel.pauseGame() },
                            onResume = { gameViewModel.resumeGame() },
                            onSpeedChange = { gameViewModel.setGameSpeed(it) }
                        )
                    }
                )
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 左侧可折叠导航栏
                CollapsibleNavigationRail(
                    isExpanded = isNavExpanded,
                    onToggle = { isNavExpanded = !isNavExpanded },
                    currentPage = currentPage,
                    onPageSelected = { currentPage = it }
                )

                // 中间主内容区
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    when (currentPage) {
                        PageType.OVERVIEW -> OverviewPage(sectViewModel)
                        PageType.DISCIPLES -> DisciplesPage(discipleViewModel)
                        PageType.QUESTS -> QuestsPage(gameViewModel)
                        PageType.POLICY -> PolicyPage(gameViewModel)
                    }
                }

                // 右侧信息面板
                RightPanel(
                    sectViewModel = sectViewModel,
                    discipleViewModel = discipleViewModel,
                    gameViewModel = gameViewModel,
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

/**
 * 游戏速度控制组件
 */
@Composable
fun GameSpeedControl(
    gameState: GameState,
    gameSpeed: GameSpeed,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSpeedChange: (GameSpeed) -> Unit
) {
    Row(
        modifier = Modifier.padding(end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 暂停/继续按钮
        Button(
            onClick = { if (gameState == GameState.Running) onPause() else onResume() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (gameState == GameState.Running) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (gameState == GameState.Running) "暂停" else "继续")
        }

        // 速度选择
        GameSpeed.values().filter { it != GameSpeed.PAUSE }.forEach { speed ->
            val isSelected = gameSpeed == speed
            Button(
                onClick = { onSpeedChange(speed) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    speed.displayName,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 可折叠导航栏组件
 */
@Composable
fun CollapsibleNavigationRail(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    currentPage: PageType,
    onPageSelected: (PageType) -> Unit
) {
    val width by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 80.dp,
        label = "nav_width"
    )

    Card(
        modifier = Modifier
            .width(width)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 切换按钮
            IconButton(
                onClick = onToggle,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = if (isExpanded) "◀" else "▶",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Divider(modifier = Modifier.padding(horizontal = 8.dp))

            // 导航项
            NavItem(
                icon = "🏠",
                label = "总览",
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.OVERVIEW,
                onClick = { onPageSelected(PageType.OVERVIEW) }
            )

            NavItem(
                icon = "👥",
                label = "弟子",
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.DISCIPLES,
                onClick = { onPageSelected(PageType.DISCIPLES) }
            )

            NavItem(
                icon = "📋",
                label = "任务",
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.QUESTS,
                onClick = { onPageSelected(PageType.QUESTS) }
            )

            NavItem(
                icon = "⚙️",
                label = "政策",
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.POLICY,
                onClick = { onPageSelected(PageType.POLICY) }
            )
        }
    }
}

/**
 * 导航项组件
 */
@Composable
fun NavItem(
    icon: String,
    label: String,
    isExpanded: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "nav_item_bg"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            if (isExpanded) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 游戏状态
            Text(
                text = "游戏状态",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            InfoRow("状态", if (gameState == GameState.Running) "运行中" else "已暂停")
            InfoRow("速度", gameSpeed.displayName)

            Divider()

            // 宗门信息
            Text(
                text = "宗门信息",
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
                text = "弟子统计",
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
                text = "快速操作",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = { gameViewModel.publishSelectionTask() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("发布选拔任务")
            }
        }
    }
}

/**
 * 信息行组件
 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

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

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * 弟子管理页面
 */
@Composable
fun DisciplesPage(viewModel: DiscipleViewModel) {
    val discipleList by viewModel.discipleList.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()

    // 详情对话框状态
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedDisciple by remember { mutableStateOf<DiscipleUiModel?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "弟子管理",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 筛选标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "全部",
                selected = currentFilter is DiscipleViewModel.DiscipleFilter.All,
                onClick = { viewModel.filterByPosition(null) }
            )
            FilterChip(
                label = "内门",
                selected = currentFilter is DiscipleViewModel.DiscipleFilter.ByPosition
                    && (currentFilter as? DiscipleViewModel.DiscipleFilter.ByPosition)?.position == SectPositionType.DISCIPLE_INNER,
                onClick = { viewModel.filterByPosition(SectPositionType.DISCIPLE_INNER) }
            )
            FilterChip(
                label = "外门",
                selected = currentFilter is DiscipleViewModel.DiscipleFilter.ByPosition
                    && (currentFilter as? DiscipleViewModel.DiscipleFilter.ByPosition)?.position == SectPositionType.DISCIPLE_OUTER,
                onClick = { viewModel.filterByPosition(SectPositionType.DISCIPLE_OUTER) }
            )
            FilterChip(
                label = "长老",
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
                    Text("暂无弟子", style = MaterialTheme.typography.bodyLarge)
                } else {
                    // 使用LazyVerticalGrid展示卡片
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 200.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(disciples.size) { index ->
                            DiscipleCard(
                                disciple = disciples[index],
                                onClick = {
                                    selectedDisciple = disciples[index]
                                    showDetailDialog = true
                                }
                            )
                        }
                    }
                }
            }
            is DiscipleViewModel.DiscipleListUiState.Error -> {
                Text("错误: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // 弟子详情对话框
    if (showDetailDialog && selectedDisciple != null) {
        DiscipleDetailDialog(
            disciple = selectedDisciple!!,
            onDismiss = { showDetailDialog = false }
        )
    }
}

/**
 * 弟子卡片组件
 */
@Composable
fun DiscipleCard(disciple: DiscipleUiModel, onClick: () -> Unit = {}) {
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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

            // 年龄
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "年龄: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${disciple.age}岁",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 修为进度（游戏风格）
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📿 修为",
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
 * 弟子详情对话框
 */
@Composable
fun DiscipleDetailDialog(disciple: DiscipleUiModel, onDismiss: () -> Unit) {
    val positionColor = when (disciple.position) {
        SectPositionType.LEADER -> MaterialTheme.colorScheme.primary
        SectPositionType.ELDER -> MaterialTheme.colorScheme.tertiary
        SectPositionType.DISCIPLE_INNER -> MaterialTheme.colorScheme.secondary
        SectPositionType.DISCIPLE_OUTER -> MaterialTheme.colorScheme.surfaceVariant
    }

    val behaviorColor = when (disciple.currentBehavior) {
        "修炼中" -> MaterialTheme.colorScheme.primary
        "工作中" -> MaterialTheme.colorScheme.tertiary
        "休息中" -> MaterialTheme.colorScheme.secondary
        "社交中" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(disciple.name, style = MaterialTheme.typography.headlineSmall)
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
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 境界和状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "境界: ${disciple.realmDisplay}",
                        style = MaterialTheme.typography.bodyLarge
                    )
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

                Spacer(modifier = Modifier.height(16.dp))

                // 基本信息
                DetailItem(label = "年龄", value = "${disciple.age}岁")
                DetailItem(label = "职务", value = disciple.positionDisplay)

                Spacer(modifier = Modifier.height(16.dp))

                // 修为详情
                Text(
                    text = "修为详情",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DetailItem(label = "当前修为", value = "${disciple.cultivation}/${disciple.maxCultivation}")
                DetailItem(label = "突破进度", value = "${(disciple.cultivationProgress * 100).toInt()}%")

                Spacer(modifier = Modifier.height(16.dp))

                // 生命和精力
                Text(
                    text = "状态",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DetailItem(label = "生命值", value = "${disciple.health}/${disciple.maxHealth}")
                DetailItem(label = "精力值", value = "${disciple.spirit}/${disciple.maxSpirit}")

                Spacer(modifier = Modifier.height(16.dp))

                // 进度条
                Text("修为进度", style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = { disciple.cultivationProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("生命值", style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = { disciple.health.toFloat() / disciple.maxHealth.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (disciple.health < disciple.maxHealth * 0.3f) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("精力值", style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = { disciple.spirit.toFloat() / disciple.maxSpirit.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 详情项组件
 */
@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (selected) {
        androidx.compose.ui.graphics.Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor
        )
    }
}

@Composable
fun DiscipleRow(position: String, realm: String, age: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(position, modifier = Modifier.weight(1f))
        Text(realm, modifier = Modifier.weight(1f))
        Text(age, modifier = Modifier.weight(1f))
        Text(status, modifier = Modifier.weight(1f))
    }
}

/**
 * 任务大厅页面
 */
@Composable
fun QuestsPage(gameViewModel: GameViewModel) {
    val pendingTasks by gameViewModel.pendingTasks.collectAsState()
    val completedTasks by gameViewModel.completedTasks.collectAsState()
    val candidates by gameViewModel.candidates.collectAsState()

    var showPublishDialog by remember { mutableStateOf(false) }
    var showCandidatesDialog by remember { mutableStateOf(false) }
    var selectedTaskId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "任务大厅",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { showPublishDialog = true }) {
                Text("发布选拔任务")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 任务统计
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                value = pendingTasks.count { it.status == TaskStatus.IN_PROGRESS }.toString(),
                label = "进行中",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = pendingTasks.count { it.status == TaskStatus.PENDING_APPROVAL }.toString(),
                label = "待审批",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = completedTasks.size.toString(),
                label = "已完成",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 任务列表
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "任务列表",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (pendingTasks.isEmpty()) {
                    Text("暂无待处理任务", style = MaterialTheme.typography.bodyMedium)
                } else {
                    pendingTasks.forEach { task ->
                        TaskItem(
                            task = task,
                            onApprove = {
                                gameViewModel.approveTask(task.id, true)
                                // 执行任务
                                gameViewModel.executeTask(task.id)
                                // 加载候选人
                                gameViewModel.loadCandidates(task.id)
                                selectedTaskId = task.id
                                showCandidatesDialog = true
                            },
                            onReject = { gameViewModel.approveTask(task.id, false) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // 发布任务对话框
    if (showPublishDialog) {
        AlertDialog(
            onDismissRequest = { showPublishDialog = false },
            title = { Text("发布选拔任务") },
            text = { Text("确定要发布外门弟子选拔任务吗？") },
            confirmButton = {
                Button(onClick = {
                    gameViewModel.publishSelectionTask()
                    showPublishDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(onClick = { showPublishDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 候选人对话框
    if (showCandidatesDialog) {
        AlertDialog(
            onDismissRequest = { showCandidatesDialog = false },
            title = { Text("晋升候选人") },
            text = {
                Column {
                    if (candidates.isEmpty()) {
                        Text("暂无候选人")
                    } else {
                        Text("请选择要晋升的弟子：", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        candidates.forEach { candidate ->
                            CandidateItem(candidate = candidate)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedIds = candidates.map { it.id }
                        if (selectedIds.isNotEmpty()) {
                            gameViewModel.promoteDisciples(selectedIds)
                        }
                        showCandidatesDialog = false
                    }
                ) {
                    Text("确认晋升")
                }
            },
            dismissButton = {
                Button(onClick = { showCandidatesDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
fun TaskItem(task: TaskInfo, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.titleSmall)
                    Text(task.description, style = MaterialTheme.typography.bodySmall)
                    Text("创建时间: ${task.createdAt}", style = MaterialTheme.typography.bodySmall)
                }
                TaskStatusBadge(status = task.status)
            }

            if (task.status == TaskStatus.PENDING_APPROVAL) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onApprove, modifier = Modifier.weight(1f)) {
                        Text("批准")
                    }
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("拒绝")
                    }
                }
            }
        }
    }
}

@Composable
fun TaskStatusBadge(status: TaskStatus) {
    val (text, color) = when (status) {
        TaskStatus.PENDING_APPROVAL -> "待审批" to MaterialTheme.colorScheme.error
        TaskStatus.APPROVED -> "已批准" to MaterialTheme.colorScheme.primary
        TaskStatus.IN_PROGRESS -> "进行中" to MaterialTheme.colorScheme.tertiary
        TaskStatus.COMPLETED -> "已完成" to MaterialTheme.colorScheme.secondary
        TaskStatus.CANCELLED -> "已取消" to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun CandidateItem(candidate: CandidateInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(candidate.name, style = MaterialTheme.typography.titleSmall)
            Text("评分: ${String.format("%.2f", candidate.score)}", style = MaterialTheme.typography.bodySmall)
            Text(
                "完成度: ${(candidate.completionRate * 100).toInt()}% | " +
                "效率: ${(candidate.efficiency * 100).toInt()}% | " +
                "质量: ${(candidate.quality * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * 政策配置页面
 */
@Composable
fun PolicyPage(gameViewModel: GameViewModel) {
    val currentPolicy by gameViewModel.currentPolicy.collectAsState()

    // 根据当前政策初始化状态
    var selectionCycle by remember(currentPolicy) {
        mutableStateOf(
            when (currentPolicy?.selectionCycle) {
                3 -> 0
                5 -> 1
                10 -> 2
                else -> 1
            }
        )
    }
    var selectionRatio by remember(currentPolicy) {
        mutableStateOf(currentPolicy?.selectionRatio ?: 0.05f)
    }
    var cultivationRatio by remember(currentPolicy) {
        mutableStateOf((currentPolicy?.cultivationRatio ?: 40).toFloat())
    }
    var facilityRatio by remember(currentPolicy) {
        mutableStateOf((currentPolicy?.facilityRatio ?: 30).toFloat())
    }
    var reserveRatio by remember(currentPolicy) {
        mutableStateOf((currentPolicy?.reserveRatio ?: 30).toFloat())
    }

    // 计算总和
    val totalRatio = cultivationRatio + facilityRatio + reserveRatio
    val isValid = kotlin.math.abs(totalRatio - 100f) < 0.1f

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "政策配置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 选拔周期
                Text(
                    text = "选拔周期",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row {
                    RadioButton(
                        selected = selectionCycle == 0,
                        onClick = { selectionCycle = 0 }
                    )
                    Text("3年", modifier = Modifier.padding(end = 16.dp, top = 12.dp))

                    RadioButton(
                        selected = selectionCycle == 1,
                        onClick = { selectionCycle = 1 }
                    )
                    Text("5年", modifier = Modifier.padding(end = 16.dp, top = 12.dp))

                    RadioButton(
                        selected = selectionCycle == 2,
                        onClick = { selectionCycle = 2 }
                    )
                    Text("10年", modifier = Modifier.padding(top = 12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 选拔比例
                Text(
                    text = "选拔比例: ${(selectionRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = selectionRatio,
                    onValueChange = { selectionRatio = it },
                    valueRange = 0.03f..0.10f,
                    steps = 6
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 资源分配
                Text(
                    text = "资源分配比例",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text("修炼: ${cultivationRatio.toInt()}%")
                Slider(
                    value = cultivationRatio,
                    onValueChange = { cultivationRatio = it },
                    valueRange = 0f..100f
                )

                Text("设施: ${facilityRatio.toInt()}%")
                Slider(
                    value = facilityRatio,
                    onValueChange = { facilityRatio = it },
                    valueRange = 0f..100f
                )

                Text("储备: ${reserveRatio.toInt()}%")
                Slider(
                    value = reserveRatio,
                    onValueChange = { reserveRatio = it },
                    valueRange = 0f..100f
                )

                // 验证总和
                if (!isValid) {
                    Text(
                        text = "警告: 资源分配总和必须为100% (当前: ${totalRatio.toInt()}%)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 保存按钮
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = {
                            val cycleYears = when (selectionCycle) {
                                0 -> 3
                                1 -> 5
                                else -> 10
                            }
                            val policyInfo = PolicyInfo(
                                selectionCycle = cycleYears,
                                selectionRatio = selectionRatio,
                                cultivationRatio = cultivationRatio.toInt(),
                                facilityRatio = facilityRatio.toInt(),
                                reserveRatio = reserveRatio.toInt()
                            )
                            gameViewModel.savePolicy(policyInfo)
                        },
                        enabled = isValid
                    ) {
                        Text("保存配置")
                    }
                }
            }
        }
    }
}
