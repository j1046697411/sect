package cn.jzl.sect.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 筛选标签组件
 * 用于筛选条件选择，如"全部"、"外门"、"内门"等
 *
 * @param label 标签文本
 * @param selected 是否选中
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param selectedColor 选中时的颜色
 */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary
) {
    val backgroundColor = if (selected) {
        selectedColor
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val textColor = if (selected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val borderModifier = if (selected) {
        Modifier
    } else {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

/**
 * 筛选标签组
 * 用于显示一组筛选标签
 *
 * @param options 选项列表，每个选项包含标签和是否选中
 * @param onOptionSelected 选项选中回调，返回选中的索引
 * @param modifier 修饰符
 */
@Composable
fun FilterChipGroup(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                label = label,
                selected = index == selectedIndex,
                onClick = { onOptionSelected(index) }
            )
        }
    }
}

/**
 * 多选筛选标签组
 * 用于支持多选的筛选场景
 *
 * @param options 选项列表
 * @param selectedIndices 已选中的索引集合
 * @param onOptionToggle 选项切换回调
 * @param modifier 修饰符
 */
@Composable
fun MultiSelectFilterChipGroup(
    options: List<String>,
    selectedIndices: Set<Int>,
    onOptionToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                label = label,
                selected = index in selectedIndices,
                onClick = { onOptionToggle(index) }
            )
        }
    }
}
