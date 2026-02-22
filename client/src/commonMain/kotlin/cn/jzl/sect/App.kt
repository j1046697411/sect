package cn.jzl.sect

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    MaterialTheme {
        var currentPage by remember { mutableStateOf(PageType.OVERVIEW) }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("宗门修真录") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 左侧导航栏
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    NavigationRailItem(
                        icon = { Text("🏠") },
                        label = { Text("总览") },
                        selected = currentPage == PageType.OVERVIEW,
                        onClick = { currentPage = PageType.OVERVIEW }
                    )
                    NavigationRailItem(
                        icon = { Text("👥") },
                        label = { Text("弟子") },
                        selected = currentPage == PageType.DISCIPLES,
                        onClick = { currentPage = PageType.DISCIPLES }
                    )
                    NavigationRailItem(
                        icon = { Text("📋") },
                        label = { Text("任务") },
                        selected = currentPage == PageType.QUESTS,
                        onClick = { currentPage = PageType.QUESTS }
                    )
                    NavigationRailItem(
                        icon = { Text("⚙️") },
                        label = { Text("政策") },
                        selected = currentPage == PageType.POLICY,
                        onClick = { currentPage = PageType.POLICY }
                    )
                }
                
                // 主内容区
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (currentPage) {
                        PageType.OVERVIEW -> OverviewPage()
                        PageType.DISCIPLES -> DisciplesPage()
                        PageType.QUESTS -> QuestsPage()
                        PageType.POLICY -> PolicyPage()
                    }
                }
            }
        }
    }
}

/**
 * 宗门总览页面
 */
@Composable
fun OverviewPage() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "宗门总览",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 统计卡片行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(value = "128", label = "弟子总数", modifier = Modifier.weight(1f))
            StatCard(value = "25,000", label = "灵石储备", modifier = Modifier.weight(1f))
            StatCard(value = "12", label = "设施数量", modifier = Modifier.weight(1f))
            StatCard(value = "92%", label = "宗门稳定度", modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 境界分布
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "弟子境界分布",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatCard(value = "85", label = "炼气期", modifier = Modifier.weight(1f))
                    StatCard(value = "38", label = "筑基期", modifier = Modifier.weight(1f))
                    StatCard(value = "4", label = "金丹期", modifier = Modifier.weight(1f))
                    StatCard(value = "1", label = "元婴期", modifier = Modifier.weight(1f))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 近期动态
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "近期动态",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text("• 弟子张无忌突破至筑基中期")
                Text("• 千绝谷灵草产量增加15%")
                Text("• 新弟子报名：12人")
                Text("• 玄水阁使者来访")
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
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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
fun DisciplesPage() {
    var selectedFilter by remember { mutableStateOf(0) }
    val filterOptions = listOf("全部", "外门", "内门", "亲传", "杂役")
    
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
            filterOptions.forEachIndexed { index, label ->
                FilterChip(
                    label = label,
                    selected = index == selectedFilter,
                    onClick = { selectedFilter = index }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 弟子列表
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 表头
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("姓名", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    Text("职务", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    Text("境界", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    Text("状态", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // 示例数据
                DiscipleRow("张无忌", "内门", "筑基中期", "修炼中")
                DiscipleRow("赵敏", "外门", "炼气后期", "巡逻中")
                DiscipleRow("周芷若", "内门", "筑基初期", "炼丹中")
                DiscipleRow("张三丰", "亲传", "金丹中期", "闭关")
                DiscipleRow("杨过", "外门", "炼气中期", "任务中")
            }
        }
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
fun DiscipleRow(name: String, position: String, realm: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, modifier = Modifier.weight(1f))
        Text(position, modifier = Modifier.weight(1f))
        Text(realm, modifier = Modifier.weight(1f))
        StatusBadge(status)
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "修炼中" -> Pair(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), MaterialTheme.colorScheme.primary)
        "巡逻中" -> Pair(androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.15f), androidx.compose.ui.graphics.Color(0xFF2E7D32))
        "炼丹中" -> Pair(androidx.compose.ui.graphics.Color(0xFFFF9800).copy(alpha = 0.15f), androidx.compose.ui.graphics.Color(0xFFE65100))
        "闭关" -> Pair(androidx.compose.ui.graphics.Color(0xFF9E9E9E).copy(alpha = 0.15f), androidx.compose.ui.graphics.Color(0xFF616161))
        "任务中" -> Pair(androidx.compose.ui.graphics.Color(0xFF2196F3).copy(alpha = 0.15f), androidx.compose.ui.graphics.Color(0xFF1565C0))
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * 任务大厅页面
 */
@Composable
fun QuestsPage() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "任务大厅",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 任务统计
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(value = "3/10", label = "已接任务", modifier = Modifier.weight(1f))
            StatCard(value = "12/20", label = "可接任务", modifier = Modifier.weight(1f))
            StatCard(value = "45", label = "已完成", modifier = Modifier.weight(1f))
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
                    text = "待审批选拔任务",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // 示例任务
                QuestItem(
                    title = "外门弟子选拔",
                    description = "选拔外门弟子晋升为内门弟子",
                    status = "待审批"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                QuestItem(
                    title = "千绝谷灵草采集",
                    description = "采集灵草用于炼丹",
                    status = "进行中"
                )
            }
        }
    }
}

@Composable
fun QuestItem(title: String, description: String, status: String) {
    val statusColor = when (status) {
        "待审批" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        "进行中" -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        "已完成" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * 政策配置页面
 */
@Composable
fun PolicyPage() {
    var selectionCycle by remember { mutableStateOf(1) } // 0: 3年, 1: 5年, 2: 10年
    var selectionRatio by remember { mutableStateOf(0.05f) }
    
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
                
                // 保存按钮
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = androidx.compose.ui.Alignment.CenterEnd
                ) {
                    Button(
                        onClick = { }
                    ) {
                        Text("保存配置")
                    }
                }
            }
        }
    }
}
