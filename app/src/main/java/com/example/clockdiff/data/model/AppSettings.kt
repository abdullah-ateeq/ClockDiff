package com.example.clockdiff.data.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val is24HourFormat: Boolean = false,
    val defaultCrossMidnight: Boolean = true
)
