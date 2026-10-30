package com.example.clockdiff.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clockdiff.data.model.AppSettings
import com.example.clockdiff.data.model.ClockDiffHistoryItem
import com.example.clockdiff.data.model.ThemeMode
import com.example.clockdiff.data.repository.ClockDiffRepository
import com.example.clockdiff.domain.ClockDiffCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class ClockDiffViewModel(
    private val repository: ClockDiffRepository
) : ViewModel() {

    private val _userState = MutableStateFlow(
        ClockDiffUiState(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 30),
            includeDates = false,
            startDate = null,
            endDate = null,
            crossMidnight = true
        )
    )

    val uiState: StateFlow<ClockDiffUiState> = combine(
        _userState,
        repository.settingsFlow,
        repository.historyFlow
    ) { userState, settings, history ->
        userState.copy(
            settings = settings,
            history = history
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _userState.value
    )

    init {
        // Automatically perform initial calculation
        calculate()
    }

    fun setStartTime(time: LocalTime) {
        _userState.update { it.copy(startTime = time, isSavedToHistory = false) }
    }

    fun setEndTime(time: LocalTime) {
        _userState.update { it.copy(endTime = time, isSavedToHistory = false) }
    }

    fun setIncludeDates(include: Boolean) {
        _userState.update { current ->
            if (include) {
                val today = LocalDate.now()
                val endDate = if (current.crossMidnight && current.endTime.isBefore(current.startTime)) {
                    today.plusDays(1)
                } else {
                    today
                }
                current.copy(
                    includeDates = true,
                    startDate = current.startDate ?: today,
                    endDate = current.endDate ?: endDate,
                    isSavedToHistory = false
                )
            } else {
                current.copy(
                    includeDates = false,
                    startDate = null,
                    endDate = null,
                    isSavedToHistory = false
                )
            }
        }
    }

    fun setStartDate(date: LocalDate?) {
        _userState.update { it.copy(startDate = date, isSavedToHistory = false) }
    }

    fun setEndDate(date: LocalDate?) {
        _userState.update { it.copy(endDate = date, isSavedToHistory = false) }
    }

    fun setCrossMidnight(enabled: Boolean) {
        _userState.update { it.copy(crossMidnight = enabled, isSavedToHistory = false) }
    }

    fun swapTimes() {
        _userState.update { current ->
            val tempTime = current.startTime
            val tempDate = current.startDate
            current.copy(
                startTime = current.endTime,
                endTime = tempTime,
                startDate = current.endDate,
                endDate = tempDate,
                isSavedToHistory = false
            )
        }
    }

    fun setStartToNow() {
        val now = LocalTime.now()
        val roundedMinute = (now.minute / 5) * 5
        setStartTime(LocalTime.of(now.hour, roundedMinute))
    }

    fun setEndToNow() {
        val now = LocalTime.now()
        val roundedMinute = (now.minute / 5) * 5
        setEndTime(LocalTime.of(now.hour, roundedMinute))
    }

    fun calculate() {
        val current = _userState.value
        val result = ClockDiffCalculator.calculate(
            startTime = current.startTime,
            endTime = current.endTime,
            startDate = if (current.includeDates) current.startDate else null,
            endDate = if (current.includeDates) current.endDate else null,
            crossMidnight = current.crossMidnight
        )
        _userState.update {
            it.copy(
                calculationResult = result,
                isSavedToHistory = false,
                validationError = null
            )
        }
    }

    fun resetInputs() {
        _userState.update {
            it.copy(
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 30),
                includeDates = false,
                startDate = null,
                endDate = null,
                crossMidnight = true,
                calculationResult = null,
                isSavedToHistory = false,
                validationError = null
            )
        }
    }

    fun saveCurrentResultToHistory() {
        val current = _userState.value
        val result = current.calculationResult ?: return

        val historyItem = ClockDiffHistoryItem(
            startHour = result.startTime.hour,
            startMinute = result.startTime.minute,
            endHour = result.endTime.hour,
            endMinute = result.endTime.minute,
            startDateIso = result.startDate?.toString(),
            endDateIso = result.endDate?.toString(),
            crossMidnight = result.crossMidnight,
            days = result.days,
            hours = result.hours,
            minutes = result.minutes,
            totalMinutes = result.totalMinutes,
            totalSeconds = result.totalSeconds,
            formattedDuration = result.formattedDuration,
            isNegative = result.isNegative,
            crossesMidnight = result.crossesMidnight
        )

        viewModelScope.launch {
            repository.saveHistoryItem(historyItem)
            _userState.update {
                it.copy(
                    isSavedToHistory = true,
                    snackbarMessage = "Saved to calculation history"
                )
            }
        }
    }

    fun loadFromHistory(item: ClockDiffHistoryItem) {
        val start = LocalTime.of(item.startHour, item.startMinute)
        val end = LocalTime.of(item.endHour, item.endMinute)
        val startDate = item.startDateIso?.let { LocalDate.parse(it) }
        val endDate = item.endDateIso?.let { LocalDate.parse(it) }
        val hasDates = startDate != null && endDate != null

        _userState.update {
            it.copy(
                startTime = start,
                endTime = end,
                includeDates = hasDates,
                startDate = startDate,
                endDate = endDate,
                crossMidnight = item.crossMidnight,
                isSavedToHistory = true
            )
        }
        calculate()
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
            _userState.update { it.copy(snackbarMessage = "Calculation removed from history") }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _userState.update { it.copy(snackbarMessage = "All history cleared") }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(themeMode)
        }
    }

    fun setIs24HourFormat(is24Hour: Boolean) {
        viewModelScope.launch {
            repository.setIs24HourFormat(is24Hour)
        }
    }

    fun setDefaultCrossMidnight(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDefaultCrossMidnight(enabled)
            _userState.update { it.copy(crossMidnight = enabled) }
        }
    }

    fun clearSnackbarMessage() {
        _userState.update { it.copy(snackbarMessage = null) }
    }

    class Factory(private val repository: ClockDiffRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClockDiffViewModel::class.java)) {
                return ClockDiffViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
