package cn.jzl.sect.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

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
