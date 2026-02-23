package cn.jzl.sect.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.jzl.sect.viewmodel.*

/**
 * 功法页面（简化版）
 */
@Composable
fun SkillsPage(
    skillViewModel: SkillViewModel,
    discipleViewModel: DiscipleViewModel
) {
    val skillList by skillViewModel.skillList.collectAsState()
    val selectedDisciple by discipleViewModel.selectedDisciple.collectAsState()

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
                                onClick = { /* 查看详情 */ },
                                onLearn = { /* 学习功能开发中 */ }
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
        modifier = Modifier.fillMaxWidth(),
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

            // 学习按钮（禁用）
            Button(
                onClick = { /* 功能开发中 */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            ) {
                Text("学习功法(开发中)")
            }
        }
    }
}
