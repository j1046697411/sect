package cn.jzl.sect.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.jzl.sect.viewmodel.GameViewModel
import org.jetbrains.compose.resources.stringResource
import sect.client.generated.resources.Res
import sect.client.generated.resources.page_quests_title
import sect.client.generated.resources.quests_coming_soon
import sect.client.generated.resources.quests_in_development

/**
 * 任务大厅页面（简化版）
 */
@Composable
fun QuestsPage(gameViewModel: GameViewModel) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(Res.string.page_quests_title),
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
                    text = stringResource(Res.string.quests_in_development),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.quests_coming_soon),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
