package cn.jzl.sect.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import sect.client.generated.resources.Res
import sect.client.generated.resources.nav_buildings
import sect.client.generated.resources.nav_disciples
import sect.client.generated.resources.nav_overview
import sect.client.generated.resources.nav_policy
import sect.client.generated.resources.nav_quests
import sect.client.generated.resources.nav_skills

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
            label = { Text(stringResource(Res.string.nav_overview)) },
            selected = currentPage == PageType.OVERVIEW,
            onClick = { onPageSelected(PageType.OVERVIEW) }
        )
        NavigationBarItem(
            icon = { Text("👥") },
            label = { Text(stringResource(Res.string.nav_disciples)) },
            selected = currentPage == PageType.DISCIPLES,
            onClick = { onPageSelected(PageType.DISCIPLES) }
        )
        NavigationBarItem(
            icon = { Text("🏯") },
            label = { Text(stringResource(Res.string.nav_buildings)) },
            selected = currentPage == PageType.BUILDINGS,
            onClick = { onPageSelected(PageType.BUILDINGS) }
        )
        NavigationBarItem(
            icon = { Text("📜") },
            label = { Text(stringResource(Res.string.nav_quests)) },
            selected = currentPage == PageType.QUESTS,
            onClick = { onPageSelected(PageType.QUESTS) }
        )
        NavigationBarItem(
            icon = { Text("⚙️") },
            label = { Text(stringResource(Res.string.nav_policy)) },
            selected = currentPage == PageType.POLICY,
            onClick = { onPageSelected(PageType.POLICY) }
        )
        NavigationBarItem(
            icon = { Text("📚") },
            label = { Text(stringResource(Res.string.nav_skills)) },
            selected = currentPage == PageType.SKILLS,
            onClick = { onPageSelected(PageType.SKILLS) }
        )
    }
}
