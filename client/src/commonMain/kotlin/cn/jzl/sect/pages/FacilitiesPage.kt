package cn.jzl.sect.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 设施管理页面（简化版）
 */
@Composable
fun FacilitiesPage() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "设施管理",
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
                Text(
                    text = "🏗️ 功能开发中",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "设施建设系统正在开发中，敬请期待...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
