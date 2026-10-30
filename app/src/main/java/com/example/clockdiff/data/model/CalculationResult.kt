package com.example.clockdiff.data.model

import java.time.LocalDate
import java.time.LocalTime

data class CalculationResult(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val crossMidnight: Boolean = false,
    val isNegative: Boolean = false,
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val totalMinutes: Long = 0,
    val totalSeconds: Long = 0,
    val formattedDuration: String = "",
    val decimalHours: Double = 0.0,
    val crossesMidnight: Boolean = false,
    val warningMessage: String? = null
)
