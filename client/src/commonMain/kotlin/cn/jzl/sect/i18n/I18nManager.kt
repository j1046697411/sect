package cn.jzl.sect.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 国际化语言管理器
 * 单例对象，负责管理应用的语言状态
 */
object I18nManager {

    /**
     * 内部使用的可变状态流，存储当前语言
     */
    private val _currentLanguage = MutableStateFlow(Language.CHINESE)

    /**
     * 对外暴露的只读状态流
     * 订阅此属性以监听语言变化
     */
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    /**
     * 切换到指定语言
     * @param language 目标语言
     */
    fun switchLanguage(language: Language) {
        if (_currentLanguage.value != language) {
            _currentLanguage.value = language
        }
    }

    /**
     * 在中文和英文之间切换
     * 当前为中文则切换到英文，反之亦然
     */
    fun toggleLanguage() {
        val newLanguage = when (_currentLanguage.value) {
            Language.CHINESE -> Language.ENGLISH
            Language.ENGLISH -> Language.CHINESE
        }
        _currentLanguage.value = newLanguage
    }
}
