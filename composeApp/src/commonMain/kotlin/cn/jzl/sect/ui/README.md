# UI 界面层 (UI Layer)

本模块包含所有游戏界面，使用 Compose Multiplatform 实现。

## 设计原则

1. **响应式设计**：界面自动适应不同屏幕尺寸
2. **状态驱动**：UI 状态由 ViewModel 管理
3. **与 ECS 分离**：UI 层通过 Service 查询数据，不直接操作 ECS

## 目录结构

```
ui/
├── screens/           # 游戏界面
│   ├── OverviewScreen.kt      # 宗门总览
│   ├── DiscipleScreen.kt      # 弟子管理
│   ├── CultivationScreen.kt   # 修炼界面
│   ├── FacilityScreen.kt      # 设施建设
│   ├── ResourceScreen.kt      # 资源管理
│   ├── MissionScreen.kt       # 任务大厅
│   ├── MarketScreen.kt        # 交易市场
│   ├── EventScreen.kt         # 事件处理
│   └── SettingsScreen.kt      # 游戏设置
│
└── viewmodels/        # 状态管理
    ├── OverviewViewModel.kt
    ├── DiscipleViewModel.kt
    └── EventViewModel.kt
```

## 界面总览

### 1. OverviewScreen (宗门总览)

```kotlin
@Composable
fun OverviewScreen(viewModel: OverviewViewModel) {
    val sectInfo by viewModel.sectInfo.collectAsState()
    val stats by viewModel.statistics.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部信息栏
        TopInfoBar(
            sectName = sectInfo.name,
            gameTime = sectInfo.currentTime,
            funds = sectInfo.funds
        )
        
        // 核心数据统计
        StatisticsPanel(stats)
        
        // 近期事件
        RecentEventsList(events = viewModel.recentEvents)
        
        // 快捷操作
        QuickActionPanel(
            onRecruitClick = { viewModel.startRecruitment() },
            onConstructionClick = { viewModel.openConstruction() },
            onPolicyClick = { viewModel.openPolicy() }
        )
    }
}
```

**显示内容**：
- 宗门基本信息（名称、等级、创建时间）
- 资源概览（灵石、贡献点、各类材料）
- 弟子统计（总数、各境界分布、各职位分布）
- 设施概况（数量、运行状态）
- 近期事件（最近发生的重大事件）

### 2. DiscipleScreen (弟子管理)

```kotlin
@Composable
fun DiscipleScreen(viewModel: DiscipleViewModel) {
    val disciples by viewModel.disciples.collectAsState()
    val selectedDisciple by viewModel.selectedDisciple.collectAsState()
    
    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧：弟子列表
        DiscipleListPanel(
            disciples = disciples,
            selectedId = selectedDisciple?.id,
            onSelect = { viewModel.selectDisciple(it) },
            filterOptions = viewModel.filterOptions
        )
        
        // 右侧：弟子详情
        selectedDisciple?.let { disciple ->
            DiscipleDetailPanel(
                disciple = disciple,
                onPromote = { viewModel.promote(disciple) },
                onExpel = { viewModel.expel(disciple) },
                onAssignTask = { viewModel.assignTask(disciple) }
            )
        }
    }
}
```

**显示内容**：
- 弟子列表（可筛选：境界、职位、状态）
- 弟子详情（基本信息、资质、性格、当前状态）
- 操作按钮（晋升、逐出、分配任务、查看历史）

### 3. CultivationScreen (修炼界面)

```kotlin
@Composable
fun CultivationScreen(viewModel: CultivationViewModel) {
    val cultivatingDisciples by viewModel.cultivatingDisciples.collectAsState()
    val breakthroughCandidates by viewModel.breakthroughCandidates.collectAsState()
    
    Column {
        // 修炼中弟子
        SectionTitle("正在修炼")
        CultivationProgressList(cultivatingDisciples)
        
        // 可突破弟子
        SectionTitle("可尝试突破")
        BreakthroughList(
            disciples = breakthroughCandidates,
            onAssist = { viewModel.assistBreakthrough(it) },
            onProvidePills = { viewModel.provideBreakthroughPills(it) }
        )
        
        // 功法管理
        SectionTitle("功法管理")
        TechniqueManagementPanel()
    }
}
```

**显示内容**：
- 正在修炼的弟子及进度
- 即将突破的弟子（进度>90%）
- 可突破弟子及突破成功率
- 功法管理（学习、切换主修功法）

### 4. FacilityScreen (设施建设)

```kotlin
@Composable
fun FacilityScreen(viewModel: FacilityViewModel) {
    val facilities by viewModel.facilities.collectAsState()
    val availableBlueprints by viewModel.availableBlueprints.collectAsState()
    
    Column {
        // 现有设施
        SectionTitle("现有设施")
        FacilityGrid(
            facilities = facilities,
            onUpgrade = { viewModel.upgrade(it) },
            onRepair = { viewModel.repair(it) }
        )
        
        // 可建造设施
        SectionTitle("可建造")
        BlueprintList(
            blueprints = availableBlueprints,
            onBuild = { viewModel.build(it) }
        )
    }
}
```

**显示内容**：
- 现有设施列表（类型、等级、状态、效果）
- 设施建设/升级界面
- 维护状态监控

### 5. ResourceScreen (资源管理)

```kotlin
@Composable
fun ResourceScreen(viewModel: ResourceViewModel) {
    val resources by viewModel.resources.collectAsState()
    val productionStats by viewModel.productionStats.collectAsState()
    
    Column {
        // 资源概览
        ResourceOverviewPanel(resources)
        
        // 产量统计
        ProductionChart(productionStats)
        
        // 资源调配
        ResourceAllocationPanel(
            onAllocate = { type, amount -> viewModel.allocate(type, amount) }
        )
    }
}
```

**显示内容**：
- 资源库存（各类资源数量）
- 产量统计（每小时/天产量）
- 消耗统计
- 资源调配（分配给不同部门）

### 6. EventScreen (事件处理)

```kotlin
@Composable
fun EventScreen(viewModel: EventViewModel) {
    val pendingEvents by viewModel.pendingEvents.collectAsState()
    val criticalEvent by viewModel.criticalEvent.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 事件列表
        EventList(
            events = pendingEvents,
            onEventClick = { viewModel.showEventDetail(it) }
        )
        
        // 重大事件弹窗（覆盖层）
        criticalEvent?.let { event ->
            CriticalEventDialog(
                event = event,
                options = viewModel.getDecisionOptions(event),
                onDecide = { decision -> viewModel.makeDecision(event, decision) }
            )
        }
    }
}
```

**显示内容**：
- 待处理事件列表
- 事件详情
- 决策选项
- 历史事件回顾

## ViewModel 设计

### OverviewViewModel

```kotlin
class OverviewViewModel(
    private val sectQueryService: SectQueryService,
    private val eventService: EventService
) : ViewModel() {
    
    private val _sectInfo = MutableStateFlow<SectInfo?>(null)
    val sectInfo: StateFlow<SectInfo?> = _sectInfo.asStateFlow()
    
    private val _statistics = MutableStateFlow<SectStatistics>(SectStatistics())
    val statistics: StateFlow<SectStatistics> = _statistics.asStateFlow()
    
    private val _recentEvents = MutableStateFlow<List<GameEvent>>(emptyList())
    val recentEvents: StateFlow<List<GameEvent>> = _recentEvents.asStateFlow()
    
    init {
        // 定时刷新数据
        viewModelScope.launch {
            while (isActive) {
                refreshData()
                delay(1000)  // 每秒刷新
            }
        }
    }
    
    private fun refreshData() {
        _sectInfo.value = sectQueryService.getSectInfo()
        _statistics.value = sectQueryService.getStatistics()
        _recentEvents.value = eventService.getRecentEvents(10)
    }
    
    fun startRecruitment() { /* 导航到招募界面 */ }
    fun openConstruction() { /* 导航到建设界面 */ }
    fun openPolicy() { /* 打开政策制定界面 */ }
}
```

### DiscipleViewModel

```kotlin
class DiscipleViewModel(
    private val discipleQueryService: DiscipleQueryService,
    private val discipleManagementService: DiscipleManagementService
) : ViewModel() {
    
    private val _disciples = MutableStateFlow<List<DiscipleInfo>>(emptyList())
    val disciples: StateFlow<List<DiscipleInfo>> = _disciples.asStateFlow()
    
    private val _selectedDisciple = MutableStateFlow<DiscipleDetail?>(null)
    val selectedDisciple: StateFlow<DiscipleDetail?> = _selectedDisciple.asStateFlow()
    
    val filterOptions = mutableStateOf(FilterOptions())
    
    fun loadDisciples(sectId: Long) {
        val sect = world.getEntity(sectId)
        _disciples.value = discipleQueryService.getSectMembers(sect)
    }
    
    fun selectDisciple(entity: Entity) {
        _selectedDisciple.value = discipleQueryService.getDiscipleDetail(entity)
    }
    
    fun promote(disciple: DiscipleDetail) {
        // 显示晋升对话框，选择新职位
    }
    
    fun expel(disciple: DiscipleDetail) {
        // 显示确认对话框
        discipleManagementService.expel(disciple.entity, "玩家决定")
        refreshList()
    }
    
    fun applyFilter(options: FilterOptions) {
        filterOptions.value = options
        // 重新查询并过滤
    }
}
```

## 导航设计

```kotlin
@Composable
fun SectApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        topBar = { TimeDisplayBar() },
        bottomBar = { BottomNavigation(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "overview",
            modifier = Modifier.padding(padding)
        ) {
            composable("overview") { OverviewScreen(viewModel.overviewViewModel) }
            composable("disciples") { DiscipleScreen(viewModel.discipleViewModel) }
            composable("cultivation") { CultivationScreen(viewModel.cultivationViewModel) }
            composable("facilities") { FacilityScreen(viewModel.facilityViewModel) }
            composable("resources") { ResourceScreen(viewModel.resourceViewModel) }
            composable("events") { EventScreen(viewModel.eventViewModel) }
            composable("settings") { SettingsScreen(viewModel.settingsViewModel) }
        }
    }
}
```

## 依赖关系

- **依赖**：所有 Service 模块
- **无被依赖**：UI 是最顶层模块
