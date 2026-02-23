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
import org.jetbrains.compose.resources.stringResource
import sect.client.generated.resources.Res
import sect.client.generated.resources.nav_buildings
import sect.client.generated.resources.nav_disciples
import sect.client.generated.resources.nav_overview
import sect.client.generated.resources.nav_policy
import sect.client.generated.resources.nav_quests
import sect.client.generated.resources.nav_skills

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
                label = stringResource(Res.string.nav_overview),
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.OVERVIEW,
                onClick = { onPageSelected(PageType.OVERVIEW) }
            )

            NavItem(
                icon = "👥",
                label = stringResource(Res.string.nav_disciples),
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.DISCIPLES,
                onClick = { onPageSelected(PageType.DISCIPLES) }
            )

            NavItem(
                icon = "🏯",
                label = stringResource(Res.string.nav_buildings),
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.BUILDINGS,
                onClick = { onPageSelected(PageType.BUILDINGS) }
            )

            NavItem(
                icon = "📜",
                label = stringResource(Res.string.nav_quests),
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.QUESTS,
                onClick = { onPageSelected(PageType.QUESTS) }
            )

            NavItem(
                icon = "⚙️",
                label = stringResource(Res.string.nav_policy),
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.POLICY,
                onClick = { onPageSelected(PageType.POLICY) }
            )

            NavItem(
                icon = "📚",
                label = stringResource(Res.string.nav_skills),
                isExpanded = isExpanded,
                isSelected = currentPage == PageType.SKILLS,
                onClick = { onPageSelected(PageType.SKILLS) }
            )
        }
    }
}
