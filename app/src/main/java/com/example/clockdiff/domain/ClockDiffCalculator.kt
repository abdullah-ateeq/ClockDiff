package com.example.clockdiff.domain

import com.example.clockdiff.data.model.CalculationResult
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ClockDiffCalculator {

    private val TIME_FORMAT_12_HOUR = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val TIME_FORMAT_24_HOUR = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
    private val DATE_FORMAT_SHORT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

    fun calculate(
        startTime: LocalTime,
        endTime: LocalTime,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        crossMidnight: Boolean = false
    ): CalculationResult {
        // Resolve dates if either or both are specified
        val resolvedStartDate: LocalDate?
        val resolvedEndDate: LocalDate?

        if (startDate != null && endDate != null) {
            resolvedStartDate = startDate
            resolvedEndDate = endDate
        } else if (startDate != null && endDate == null) {
            resolvedStartDate = startDate
            resolvedEndDate = if (crossMidnight && endTime.isBefore(startTime)) {
                startDate.plusDays(1)
            } else {
                startDate
            }
        } else if (startDate == null && endDate != null) {
            resolvedStartDate = if (crossMidnight && endTime.isBefore(startTime)) {
                endDate.minusDays(1)
            } else {
                endDate
            }
            resolvedEndDate = endDate
        } else {
            resolvedStartDate = null
            resolvedEndDate = null
        }

        if (resolvedStartDate != null && resolvedEndDate != null) {
            return calculateWithDates(startTime, endTime, resolvedStartDate, resolvedEndDate)
        } else {
            return calculateTimeOnly(startTime, endTime, crossMidnight)
        }
    }

    private fun calculateTimeOnly(
        startTime: LocalTime,
        endTime: LocalTime,
        crossMidnight: Boolean
    ): CalculationResult {
        if (startTime == endTime) {
            return CalculationResult(
                startTime = startTime,
                endTime = endTime,
                crossMidnight = crossMidnight,
                isNegative = false,
                days = 0,
                hours = 0,
                minutes = 0,
                totalMinutes = 0,
                totalSeconds = 0,
                formattedDuration = "0 min",
                decimalHours = 0.0,
                crossesMidnight = false,
                warningMessage = null
            )
        }

        if (!endTime.isBefore(startTime)) {
            // Same day, normal forward calculation
            val duration = Duration.between(startTime, endTime)
            val totalMinutes = duration.toMinutes()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            val seconds = duration.seconds
            val decimalHours = totalMinutes / 60.0

            return CalculationResult(
                startTime = startTime,
                endTime = endTime,
                crossMidnight = crossMidnight,
                isNegative = false,
                days = 0,
                hours = hours,
                minutes = minutes,
                totalMinutes = totalMinutes,
                totalSeconds = seconds,
                formattedDuration = formatDuration(0, hours, minutes, false),
                decimalHours = Math.round(decimalHours * 100.0) / 100.0,
                crossesMidnight = false,
                warningMessage = null
            )
        } else {
            // endTime is before startTime
            if (crossMidnight) {
                // Crosses midnight into next day
                val startDateTime = LocalDateTime.of(LocalDate.of(2000, 1, 1), startTime)
                val endDateTime = LocalDateTime.of(LocalDate.of(2000, 1, 2), endTime)
                val duration = Duration.between(startDateTime, endDateTime)

                val totalMinutes = duration.toMinutes()
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                val seconds = duration.seconds
                val decimalHours = totalMinutes / 60.0

                return CalculationResult(
                    startTime = startTime,
                    endTime = endTime,
                    crossMidnight = true,
                    isNegative = false,
                    days = 0,
                    hours = hours,
                    minutes = minutes,
                    totalMinutes = totalMinutes,
                    totalSeconds = seconds,
                    formattedDuration = formatDuration(0, hours, minutes, false),
                    decimalHours = Math.round(decimalHours * 100.0) / 100.0,
                    crossesMidnight = true,
                    warningMessage = null
                )
            } else {
                // crossMidnight is false: Do not assume next day
                val duration = Duration.between(endTime, startTime)
                val totalMinutes = duration.toMinutes()
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                val seconds = duration.seconds
                val decimalHours = totalMinutes / 60.0

                return CalculationResult(
                    startTime = startTime,
                    endTime = endTime,
                    crossMidnight = false,
                    isNegative = true,
                    days = 0,
                    hours = hours,
                    minutes = minutes,
                    totalMinutes = -totalMinutes,
                    totalSeconds = -seconds,
                    formattedDuration = "- " + formatDuration(0, hours, minutes, false),
                    decimalHours = -(Math.round(decimalHours * 100.0) / 100.0),
                    crossesMidnight = false,
                    warningMessage = "End time is earlier than start time. Enable 'Cross Midnight' if it concludes the next day."
                )
            }
        }
    }

    private fun calculateWithDates(
        startTime: LocalTime,
        endTime: LocalTime,
        startDate: LocalDate,
        endDate: LocalDate
    ): CalculationResult {
        val startDateTime = LocalDateTime.of(startDate, startTime)
        val endDateTime = LocalDateTime.of(endDate, endTime)
        val duration = Duration.between(startDateTime, endDateTime)

        val isNegative = duration.isNegative
        val absDuration = if (isNegative) duration.abs() else duration

        val totalMinutesSigned = duration.toMinutes()
        val totalSecondsSigned = duration.seconds

        val absTotalMinutes = absDuration.toMinutes()
        val days = absDuration.toDays()
        val remainingMinutesAfterDays = absTotalMinutes % (24 * 60)
        val hours = remainingMinutesAfterDays / 60
        val minutes = remainingMinutesAfterDays % 60

        val decimalHours = Math.round((absTotalMinutes / 60.0) * 100.0) / 100.0
        val signedDecimalHours = if (isNegative) -decimalHours else decimalHours

        val crossesMidnight = !startDate.isEqual(endDate) || (isNegative && duration.abs().toDays() > 0)
        val formatted = (if (isNegative) "- " else "") + formatDuration(days, hours, minutes, false)

        val warning = if (isNegative) {
            "End date and time is earlier than start date and time."
        } else {
            null
        }

        return CalculationResult(
            startTime = startTime,
            endTime = endTime,
            startDate = startDate,
            endDate = endDate,
            crossMidnight = false,
            isNegative = isNegative,
            days = days,
            hours = hours,
            minutes = minutes,
            totalMinutes = totalMinutesSigned,
            totalSeconds = totalSecondsSigned,
            formattedDuration = formatted,
            decimalHours = signedDecimalHours,
            crossesMidnight = crossesMidnight,
            warningMessage = warning
        )
    }

    fun formatDuration(days: Long, hours: Long, minutes: Long, isNegative: Boolean): String {
        val prefix = if (isNegative) "- " else ""
        val parts = mutableListOf<String>()

        if (days > 0) {
            parts.add("$days ${if (days == 1L) "day" else "days"}")
        }
        if (hours > 0) {
            parts.add("$hours ${if (hours == 1L) "hr" else "hrs"}")
        }
        if (minutes > 0) {
            parts.add("$minutes ${if (minutes == 1L) "min" else "mins"}")
        }

        if (parts.isEmpty()) {
            return "${prefix}0 min"
        }

        return prefix + parts.joinToString(" ")
    }

    fun formatTime(time: LocalTime, is24Hour: Boolean): String {
        return if (is24Hour) {
            time.format(TIME_FORMAT_24_HOUR)
        } else {
            time.format(TIME_FORMAT_12_HOUR)
        }
    }

    fun formatDate(date: LocalDate): String {
        return date.format(DATE_FORMAT)
    }

    fun formatDateShort(date: LocalDate): String {
        return date.format(DATE_FORMAT_SHORT)
    }
}
