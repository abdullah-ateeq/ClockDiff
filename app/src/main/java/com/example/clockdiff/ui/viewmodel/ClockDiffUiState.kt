package com.example.clockdiff.ui.viewmodel

import com.example.clockdiff.data.model.AppSettings
import com.example.clockdiff.data.model.CalculationResult
import com.example.clockdiff.data.model.ClockDiffHistoryItem
import java.time.LocalDate
import java.time.LocalTime

data class ClockDiffUiState(
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(17, 0),
    val includeDates: Boolean = false,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val crossMidnight: Boolean = true,
    val calculationResult: CalculationResult? = null,
    val isSavedToHistory: Boolean = false,
    val validationError: String? = null,
    val history: List<ClockDiffHistoryItem> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val snackbarMessage: String? = null
)
