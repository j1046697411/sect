package cn.jzl.sect.core.time

/**
 * 时间组件 - 存储游戏世界时间
 */
data class GameTime(
    val year: Int = 1,
    val month: Int = 1,
    val day: Int = 1,
    val hour: Int = 0
)

enum class Season {
    SPRING, SUMMER, AUTUMN, WINTER
}

/**
 * 格式化为显示字符串
 */
fun GameTime.toDisplayString(): String = "${year}年${month}月${day}日 ${hour}时"

/**
 * 添加小时数并返回新的时间
 */
fun GameTime.addHours(hours: Int): GameTime {
    var newHour = hour + hours
    var newDay = day
    var newMonth = month
    var newYear = year

    while (newHour >= 24) {
        newHour -= 24
        newDay += 1
    }

    while (newDay > 30) {
        newDay -= 30
        newMonth += 1
    }

    while (newMonth > 12) {
        newMonth -= 12
        newYear += 1
    }

    return GameTime(newYear, newMonth, newDay, newHour)
}
