package cn.jzl.sect.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.jzl.sect.viewmodel.*

/**
 * 功法页面
 */
@Composable
fun SkillsPage(
    skillViewModel: SkillViewModel,
    discipleViewModel: DiscipleViewModel
) {
    val skillList by skillViewModel.skillList.collectAsState()
    val selectedDisciple by discipleViewModel.selectedDisciple.collectAsState()
    val learningCondition by skillViewModel.learningCondition.collectAsState()
    val inheritanceCondition by skillViewModel.inheritanceCondition.collectAsState()
    val operationResult by skillViewModel.operationResult.collectAsState()

    // 对话框状态
    var showSkillDetailDialog by remember { mutableStateOf(false) }
    var showLearnDialog by remember { mutableStateOf(false) }
    var showInheritDialog by remember { mutableStateOf(false) }
    var selectedSkill by remember { mutableStateOf<SkillUiModel?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 标题
        Text(
            text = "功法管理",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 功法列表
        when (val state = skillList) {
            is SkillViewModel.SkillListUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is SkillViewModel.SkillListUiState.Success -> {
                val skills = state.skills
                if (skills.isEmpty()) {
                    Text("暂无可用功法", style = MaterialTheme.typography.bodyLarge)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(skills.size) { index ->
                            SkillCard(
                                skill = skills[index],
                                onClick = {
                                    selectedSkill = skills[index]
                                    showSkillDetailDialog = true
                                },
                                onLearn = {
                                    selectedSkill = skills[index]
                                    showLearnDialog = true
                                }
                            )
                        }
                    }
                }
            }
            is SkillViewModel.SkillListUiState.Error -> {
                Text("错误: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // 功法详情对话框
    if (showSkillDetailDialog && selectedSkill != null) {
        SkillDetailDialog(
            skill = selectedSkill!!,
            onDismiss = {
                showSkillDetailDialog = false
                selectedSkill = null
            },
            onLearn = {
                showSkillDetailDialog = false
                showLearnDialog = true
            }
        )
    }

    // 学习功法对话框
    if (showLearnDialog && selectedSkill != null) {
        LearnSkillDialog(
            skill = selectedSkill!!,
            selectedDisciple = selectedDisciple,
            learningCondition = learningCondition,
            onDismiss = {
                showLearnDialog = false
                skillViewModel.clearLearningCondition()
            },
            onCheckCondition = { discipleId, realm, talent, learnedSkillIds ->
                skillViewModel.checkLearningCondition(
                    selectedSkill!!,
                    discipleId,
                    realm,
                    talent,
                    learnedSkillIds
                )
            },
            onLearn = { discipleId ->
                skillViewModel.learnSkill(selectedSkill!!, discipleId)
            }
        )
    }

    // 操作结果提示
    operationResult?.let { result ->
        AlertDialog(
            onDismissRequest = { skillViewModel.clearOperationResult() },
            title = { Text(if (result.success) "成功" else "失败") },
            text = { Text(result.message) },
            confirmButton = {
                Button(onClick = { skillViewModel.clearOperationResult() }) {
                    Text("确定")
                }
            }
        )
    }
}

/**
 * 功法卡片
 */
@Composable
fun SkillCard(
    skill: SkillUiModel,
    onClick: () -> Unit,
    onLearn: () -> Unit
) {
    val rarityColor = when (skill.rarity) {
        cn.jzl.sect.skill.components.SkillRarity.COMMON -> Color(0xFF9E9E9E)
        cn.jzl.sect.skill.components.SkillRarity.UNCOMMON -> Color(0xFF4CAF50)
        cn.jzl.sect.skill.components.SkillRarity.RARE -> Color(0xFF2196F3)
        cn.jzl.sect.skill.components.SkillRarity.EPIC -> Color(0xFF9C27B0)
        cn.jzl.sect.skill.components.SkillRarity.LEGENDARY -> Color(0xFFFF9800)
        cn.jzl.sect.skill.components.SkillRarity.MYTHIC -> Color(0xFFF44336)
        cn.jzl.sect.skill.components.SkillRarity.DIVINE -> Color(0xFFFFD700)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 名称和品级
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = rarityColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = skill.rarityDisplay,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = rarityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 类型
            Text(
                text = "类型: ${skill.typeDisplay}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 所需境界
            Text(
                text = "所需境界: ${skill.requiredRealmDisplay}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 描述
            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 学习按钮
            Button(
                onClick = onLearn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("学习功法")
            }
        }
    }
}

/**
 * 功法详情对话框
 */
@Composable
fun SkillDetailDialog(
    skill: SkillUiModel,
    onDismiss: () -> Unit,
    onLearn: () -> Unit
) {
    val rarityColor = when (skill.rarity) {
        cn.jzl.sect.skill.components.SkillRarity.COMMON -> Color(0xFF9E9E9E)
        cn.jzl.sect.skill.components.SkillRarity.UNCOMMON -> Color(0xFF4CAF50)
        cn.jzl.sect.skill.components.SkillRarity.RARE -> Color(0xFF2196F3)
        cn.jzl.sect.skill.components.SkillRarity.EPIC -> Color(0xFF9C27B0)
        cn.jzl.sect.skill.components.SkillRarity.LEGENDARY -> Color(0xFFFF9800)
        cn.jzl.sect.skill.components.SkillRarity.MYTHIC -> Color(0xFFF44336)
        cn.jzl.sect.skill.components.SkillRarity.DIVINE -> Color(0xFFFFD700)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(skill.name, style = MaterialTheme.typography.headlineSmall)
                Surface(
                    color = rarityColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = skill.rarityDisplay,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = rarityColor
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 类型
                InfoRow("类型", skill.typeDisplay)

                // 所需境界
                InfoRow("所需境界", skill.requiredRealmDisplay)

                // 所需悟性
                InfoRow("所需悟性", "${skill.requiredComprehension}")

                // 前置功法
                if (skill.hasPrerequisites) {
                    InfoRow("前置功法", "有")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 描述
                Text(
                    text = "功法描述",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = skill.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onLearn) {
                Text("学习功法")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 学习功法对话框
 */
@Composable
fun LearnSkillDialog(
    skill: SkillUiModel,
    selectedDisciple: DiscipleUiModel?,
    learningCondition: SkillViewModel.SkillLearningConditionUiState,
    onDismiss: () -> Unit,
    onCheckCondition: (Long, cn.jzl.sect.core.cultivation.Realm, cn.jzl.sect.core.cultivation.Talent, List<Long>) -> Unit,
    onLearn: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("学习功法") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "功法: ${skill.displayName}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedDisciple == null) {
                    // 未选择弟子
                    Text(
                        text = "请先选择一名弟子",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // 已选择弟子，显示条件检查
                    Text(
                        text = "选择弟子: ${selectedDisciple.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 检查条件按钮
                    Button(
                        onClick = {
                            val talent = cn.jzl.sect.core.cultivation.Talent(
                                physique = selectedDisciple.talent?.physique ?: 50,
                                comprehension = selectedDisciple.talent?.comprehension ?: 50,
                                fortune = selectedDisciple.talent?.fortune ?: 50,
                                mental = selectedDisciple.talent?.mental ?: 50,
                                strength = selectedDisciple.talent?.strength ?: 20,
                                agility = selectedDisciple.talent?.agility ?: 20,
                                intelligence = selectedDisciple.talent?.intelligence ?: 20,
                                endurance = selectedDisciple.talent?.endurance ?: 20,
                                charm = selectedDisciple.talent?.charm ?: 50,
                                alchemyTalent = selectedDisciple.talent?.alchemyTalent ?: 0,
                                forgingTalent = selectedDisciple.talent?.forgingTalent ?: 0
                            )
                            val learnedSkillIds = selectedDisciple.learnedSkills.map { it.skillId }
                            onCheckCondition(selectedDisciple.id, selectedDisciple.realm, talent, learnedSkillIds)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("检查学习条件")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 显示条件检查结果
                    when (learningCondition) {
                        is SkillViewModel.SkillLearningConditionUiState.Idle -> {
                            Text(
                                text = "点击上方按钮检查学习条件",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is SkillViewModel.SkillLearningConditionUiState.Ready -> {
                            val condition = learningCondition.condition

                            // 条件列表
                            LearningConditionItem(
                                label = "境界要求",
                                met = condition.realmMet,
                                detail = "需要: ${skill.requiredRealmDisplay}"
                            )
                            LearningConditionItem(
                                label = "悟性要求",
                                met = condition.comprehensionMet,
                                detail = "需要: ${skill.requiredComprehension}"
                            )
                            if (skill.hasPrerequisites) {
                                LearningConditionItem(
                                    label = "前置功法",
                                    met = condition.prerequisitesMet,
                                    detail = if (condition.missingPrerequisites.isEmpty()) "已满足"
                                    else "缺少: ${condition.missingPrerequisites.joinToString(", ")}"
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 成功率
                            Text(
                                text = "学习成功率: ${(condition.successRate * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (condition.successRate > 0.7) Color(0xFF4CAF50)
                                else if (condition.successRate > 0.4) Color(0xFFFF9800)
                                else Color(0xFFF44336)
                            )

                            // 学习时间
                            Text(
                                text = "预计学习时间: ${condition.learningTime} 单位",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val canLearn = when (learningCondition) {
                is SkillViewModel.SkillLearningConditionUiState.Ready -> learningCondition.condition.canLearn
                else -> false
            }
            Button(
                onClick = {
                    selectedDisciple?.let { onLearn(it.id) }
                    onDismiss()
                },
                enabled = canLearn && selectedDisciple != null
            ) {
                Text("学习")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 学习条件项
 */
@Composable
fun LearningConditionItem(
    label: String,
    met: Boolean,
    detail: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (met) "✓" else "✗",
                color = if (met) Color(0xFF4CAF50) else Color(0xFFF44336),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 信息行组件
 */
@Composable
fun InfoRow(label: String, value: String) {
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
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
