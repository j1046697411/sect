package cn.jzl.sect.i18n

/**
 * 语言枚举
 * 定义支持的语言类型
 */
enum class Language(
    /**
     * 语言代码
     */
    val languageCode: String,
    /**
     * 显示名称
     */
    val displayName: String
) {
    /**
     * 中文
     */
    CHINESE("zh", "中文"),

    /**
     * 英文
     */
    ENGLISH("en", "English");
}
