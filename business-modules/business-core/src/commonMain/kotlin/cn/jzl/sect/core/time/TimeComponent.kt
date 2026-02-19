package cn.jzl.sect.core.time

data class TimeComponent(
    val year: Int = 1,
    val month: Int = 1,
    val day: Int = 1,
    val hour: Int = 0
) {
    fun toDisplayString(): String = "${year}年${month}月${day}日 ${hour}时"
    
    fun addHours(hours: Int): TimeComponent {
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
        
        return TimeComponent(newYear, newMonth, newDay, newHour)
    }
}

enum class Season {
    SPRING, SUMMER, AUTUMN, WINTER
}
