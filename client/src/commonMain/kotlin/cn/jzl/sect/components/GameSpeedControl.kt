package cn.jzl.sect.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.jzl.sect.engine.GameSpeed
import cn.jzl.sect.engine.GameState
import org.jetbrains.compose.resources.stringResource
import sect.client.generated.resources.Res
import sect.client.generated.resources.game_pause
import sect.client.generated.resources.game_resume

/**
 * 游戏速度控制组件
 */
@Composable
fun GameSpeedControl(
    gameState: GameState,
    gameSpeed: GameSpeed,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSpeedChange: (GameSpeed) -> Unit
) {
    Row(
        modifier = Modifier.padding(end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 暂停/继续按钮
        Button(
            onClick = { if (gameState == GameState.Running) onPause() else onResume() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (gameState == GameState.Running)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                if (gameState == GameState.Running)
                    stringResource(Res.string.game_pause)
                else
                    stringResource(Res.string.game_resume)
            )
        }

        // 速度选择
        GameSpeed.entries.filter { it != GameSpeed.PAUSE }.forEach { speed ->
            val isSelected = gameSpeed == speed
            Button(
                onClick = { onSpeedChange(speed) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    speed.displayName,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
