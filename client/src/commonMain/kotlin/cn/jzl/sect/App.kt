package cn.jzl.sect

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.resource.components.ResourceType
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.engine.*
import cn.jzl.sect.engine.WorldProvider
import cn.jzl.sect.pages.SkillsPage
import cn.jzl.sect.viewmodel.*

/**
 * 页面类型枚举
 */
enum class PageType {
    OVERVIEW,      // 宗门总览
    DISCIPLES,     // 弟子管理
    BUILDINGS,     // 建筑管理
    QUESTS,        // 任务大厅
    POLICY,        // 政策配置
    SKILLS,        // 功法管理
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
        // 右侧面板显示/隐藏状态
        var isRightPanelVisible by remember { mutableStateOf(true) }

        // 创建ViewModel（此时World已初始化）
        val gameViewModel: GameViewModel = viewModel { GameViewModel() }
        val sectViewModel: SectViewModel = viewModel { SectViewModel() }
        val discipleViewModel: DiscipleViewModel = viewModel { DiscipleViewModel() }
        val skillViewModel: SkillViewModel = viewModel { SkillViewModel() }

        // 游戏状态
        val gameState by gameViewModel.gameState.collectAsState()
        val gameSpeed by gameViewModel.gameSpeed.collectAsState()
        val currentTime by gameViewModel.currentTime.collectAsState()

        // 响应式布局检测
        val windowSizeClass = rememberWindowSizeClass()

        // 根据窗口尺寸自动调整布局
        LaunchedEffect(windowSizeClass) {
            when (windowSizeClass) {
                WindowSizeClass.COMPACT -> {
                    // 超窄屏：隐藏导航和右侧面板
                    isNavExpanded = false
                    isRightPanelVisible = false
                }
                WindowSizeClass.MEDIUM -> {
                    // 中屏：折叠导航，显示右侧面板
                    isNavExpanded = false
                    isRightPanelVisible = true
                }
                WindowSizeClass.EXPANDED -> {
                    // 宽屏：展开导航，显示右侧面板
                    isNavExpanded = true
                    isRightPanelVisible = true
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("宗门修真录 - $currentTime") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        // 右侧面板切换按钮（中屏时显示）
                        if (windowSizeClass == WindowSizeClass.MEDIUM) {
                            IconButton(onClick = { isRightPanelVisible = !isRightPanelVisible }) {
                                Text(if (isRightPanelVisible) "◀" else "▶")
                            }
                        }

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
            },
            bottomBar = {
                // 超窄屏时显示底部导航
                if (windowSizeClass == WindowSizeClass.COMPACT) {
                    BottomNavigationBar(
                        currentPage = currentPage,
                        onPageSelected = { currentPage = it }
                    )
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 左侧可折叠导航栏（超窄屏时隐藏）
                if (windowSizeClass != WindowSizeClass.COMPACT) {
                    CollapsibleNavigationRail(
                        isExpanded = isNavExpanded,
                        onToggle = { isNavExpanded = !isNavExpanded },
                        currentPage = currentPage,
                        onPageSelected = { currentPage = it }
                    )
                }

                // 中间主内容区（带动画）
                AnimatedContent(
                    targetState = currentPage,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
                    }
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = if (windowSizeClass == WindowSizeClass.COMPACT) 8.dp else 16.dp,
                                end = if (isRightPanelVisible && windowSizeClass != WindowSizeClass.COMPACT) 8.dp else 16.dp,
                                top = 16.dp,
                                bottom = 16.dp
                            )
                    ) {
                        when (page) {
                            PageType.OVERVIEW -> OverviewPage(sectViewModel)
                            PageType.DISCIPLES -> DisciplesPage(discipleViewModel)
                            PageType.BUILDINGS -> FacilitiesPage()
                            PageType.QUESTS -> QuestsPage(gameViewModel)
                            PageType.POLICY -> PolicyPage(gameViewModel)
                            PageType.SKILLS -> SkillsPage(skillViewModel, discipleViewModel)
                        }
                    }
                }

                // 右侧信息面板（根据状态显示）
                if (isRightPanelVisible && windowSizeClass != WindowSizeClass.COMPACT) {
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
}

/**
 * 窗口尺寸分类
 */
enum class WindowSizeClass {
    COMPACT,    // < 600dp (手机)
    MEDIUM,     // 600-1200dp (平板/小窗口)
    EXPANDED    // > 1200dp (桌面/大窗口)
}

/**
 * 记住窗口尺寸分类
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val density = LocalDensity.current
    val windowSize = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize

    return remember(windowSize) {
        val widthDp = with(density) { windowSize.width.toDp() }
        when {
            widthDp < 600.dp -> WindowSizeClass.COMPACT
            widthDp < 1200.dp -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }
    }
}

/**
 * 底部导航栏（超窄屏使用）
 */
@Composable
fun BottomNavigationBar(
    currentPage: PageType,
    onPageSelected: (PageType) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Text("🏠") },
            label = { Text("总览") },
            selected = currentPage == PageType.OVERVIEW,
            onClick = { onPageSelected(PageType.OVERVIEW) }
        )
        NavigationBarItem(
            icon = { Text("👥") },
            label = { Text("弟子") },
            selected = currentPage == PageType.DISCIPLES,
            onClick = { onPageSelected(PageType.DISCIPLES) }
        )
        NavigationBarItem(
            icon = { Text("🏯") },
            label = { Text("建筑") },
            selected = currentPage == PageType.BUILDINGS,
            onClick = { onPageSelected(PageType.BUILDINGS) }
        )
        NavigationBarItem(
            icon = { Text("📜") },
            label = { Text("任务") },
            selected = currentPage == PageType.QUESTS,
            onClick = { onPageSelected(PageType.QUESTS) }
        )
        NavigationBarItem(
            icon = { Text("⚙️") },
            label = { Text("政策") },
            selected = currentPage == PageType.POLICY,
            onClick = { onPageSelected(PageType.POLICY) }
        )
        NavigationBarItem(
            icon = { Text("📚") },
            label = { Text("功法") },
            selected = currentPage == PageType.SKILLS,
            onClick = { onPageSelected(PageType.SKILLS) }
        )
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
        GameSpeed.entries.filter { it != GameSpeed.PAUSE }.forEach { speed ->
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
                icon = "🏯",
                label = "建筑",
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.BUILDINGS,
                onClick = { onPageSelected(PageType.BUILDINGS) }
            )

            NavItem(
                icon = "📜",
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

            NavItem(
                icon = "📚",
                label = "功法",
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.SKILLS,
                onClick = { onPageSelected(PageType.SKILLS) }
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
 * 事件项组件
 */
@Composable
fun EventItem(icon: String, text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
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
 * 设施管理页面（简化版）
 */
@Composable
fun FacilitiesPage() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "设施管理",
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
                Text(
                    text = "🏗️ 功能开发中",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "设施建设系统正在开发中，敬请期待...",
                    style = MaterialTheme.typography.bodyMedium
                )
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
                    val selectedDiscipleFromVM by viewModel.selectedDisciple.collectAsState()

                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 200.dp),
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
                Text("错误: ${state.message}", color = MaterialTheme.colorScheme.error)
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

// 占位符数据类 - 任务相关（简化版）
enum class TaskStatus { PENDING_APPROVAL, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED }
data class TaskInfo(val id: Long, val title: String, val description: String, val createdAt: String, val status: TaskStatus)

/**
 * 任务大厅页面（简化版）
 */
@Composable
fun QuestsPage(gameViewModel: GameViewModel) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "任务大厅",
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
                Text(
                    text = "📜 功能开发中",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "任务系统正在开发中，敬请期待...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * 政策配置页面（简化版）
 */
@Composable
fun PolicyPage(gameViewModel: GameViewModel) {
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
                Text(
                    text = "⚙️ 功能开发中",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "政策配置系统正在开发中，敬请期待...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
