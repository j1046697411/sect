package cn.jzl.sect.i18n

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import sect.client.generated.resources.Res
import sect.client.generated.resources.language_switch

/**
 * 语言切换按钮组件
 *
 * 显示当前语言，点击后弹出语言选择菜单
 */
@Composable
fun LanguageSwitcher() {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage by I18nManager.currentLanguage.collectAsState()

    IconButton(
        onClick = { expanded = true }
    ) {
        Text(
            text = currentLanguage.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        Language.entries.forEach { language ->
            DropdownMenuItem(
                text = { Text(language.displayName) },
                onClick = {
                    I18nManager.switchLanguage(language)
                    expanded = false
                }
            )
        }
    }
}

/**
 * 简化的语言切换按钮
 *
 * 直接在中英文之间切换
 */
@Composable
fun LanguageToggleButton() {
    val currentLanguage by I18nManager.currentLanguage.collectAsState()

    IconButton(
        onClick = { I18nManager.toggleLanguage() }
    ) {
        Text(
            text = if (currentLanguage == Language.CHINESE) "EN" else "中",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
