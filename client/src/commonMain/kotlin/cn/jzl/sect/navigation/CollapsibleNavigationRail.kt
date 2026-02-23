package cn.jzl.sect.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.jzl.sect.components.NavItem

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
