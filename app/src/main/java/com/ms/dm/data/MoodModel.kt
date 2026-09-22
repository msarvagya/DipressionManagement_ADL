package com.ms.dm.data

enum class MoodLevel(
    val score: Int,
    val emoji: String,
    val label: String
) {
    VERY_LOW(1, "☁️", "Very Low"),
    LOW(2, "🌥️", "Low"),
    OKAY(3, "⛅", "Okay"),
    GOOD(4, "☀️", "Good")
}

data class DailyMood(
    val dayLabel: String,
    val mood: MoodLevel? = null
)

fun getInitial7DaysMood(): List<DailyMood> {
    return listOf(
        DailyMood("Mon", MoodLevel.OKAY),
        DailyMood("Tue", MoodLevel.LOW),
        DailyMood("Wed", MoodLevel.GOOD),
        DailyMood("Thu", MoodLevel.LOW),
        DailyMood("Fri", MoodLevel.VERY_LOW),
        DailyMood("Sat", MoodLevel.OKAY),
        DailyMood("Sun", MoodLevel.GOOD)
    )
}
