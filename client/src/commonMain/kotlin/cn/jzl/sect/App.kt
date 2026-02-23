package cn.jzl.sect

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.jzl.sect.components.GameSpeedControl
import cn.jzl.sect.engine.WorldProvider
import cn.jzl.sect.navigation.BottomNavigationBar
import cn.jzl.sect.navigation.CollapsibleNavigationRail
import cn.jzl.sect.navigation.PageType
import cn.jzl.sect.navigation.WindowSizeClass
import cn.jzl.sect.navigation.rememberWindowSizeClass
import cn.jzl.sect.pages.DisciplesPage
import cn.jzl.sect.pages.FacilitiesPage
import cn.jzl.sect.pages.OverviewPage
import cn.jzl.sect.pages.PolicyPage
import cn.jzl.sect.pages.QuestsPage
import cn.jzl.sect.pages.SkillsPage
import cn.jzl.sect.panels.RightPanel
import cn.jzl.sect.viewmodel.DiscipleViewModel
import cn.jzl.sect.viewmodel.GameViewModel
import cn.jzl.sect.viewmodel.SectViewModel
import cn.jzl.sect.viewmodel.SkillViewModel
import cn.jzl.sect.i18n.LanguageSwitcher
import org.jetbrains.compose.resources.stringResource
import sect.client.generated.resources.Res
import sect.client.generated.resources.app_title

/**
 * 主应用组件
 *
 * 负责：
 * - World 初始化
 * - ViewModel 创建
 * - 响应式布局管理
 * - 页面路由
 */
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
        var isNavExpanded by remember { mutableStateOf(true) }
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
                    isNavExpanded = false
                    isRightPanelVisible = false
                }
                WindowSizeClass.MEDIUM -> {
                    isNavExpanded = false
                    isRightPanelVisible = true
                }
                WindowSizeClass.EXPANDED -> {
                    isNavExpanded = true
                    isRightPanelVisible = true
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_title) + " - $currentTime") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        if (windowSizeClass == WindowSizeClass.MEDIUM) {
                            IconButton(onClick = { isRightPanelVisible = !isRightPanelVisible }) {
                                Text(if (isRightPanelVisible) "◀" else "▶")
                            }
                        }

                        LanguageSwitcher()

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
                if (windowSizeClass != WindowSizeClass.COMPACT) {
                    CollapsibleNavigationRail(
                        isExpanded = isNavExpanded,
                        onToggle = { isNavExpanded = !isNavExpanded },
                        currentPage = currentPage,
                        onPageSelected = { currentPage = it }
                    )
                }

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
