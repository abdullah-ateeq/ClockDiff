package com.example.clockdiff

import com.example.clockdiff.domain.ClockDiffCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ClockDiffCalculatorTest {

    @Test
    fun testSameTimeZeroDifference() {
        val t = LocalTime.of(14, 30)
        val result = ClockDiffCalculator.calculate(startTime = t, endTime = t)

        assertEquals(0, result.hours)
        assertEquals(0, result.minutes)
        assertEquals(0, result.totalMinutes)
        assertEquals(0, result.totalSeconds)
        assertEquals("0 min", result.formattedDuration)
        assertFalse(result.isNegative)
        assertFalse(result.crossesMidnight)
        assertNull(result.warningMessage)
    }

    @Test
    fun testTimesWithinSameDay() {
        // 09:15 to 17:45 -> 8 hours 30 minutes
        val start = LocalTime.of(9, 15)
        val end = LocalTime.of(17, 45)
        val result = ClockDiffCalculator.calculate(startTime = start, endTime = end)

        assertEquals(8, result.hours)
        assertEquals(30, result.minutes)
        assertEquals(510, result.totalMinutes)
        assertEquals(510 * 60L, result.totalSeconds)
        assertEquals("8 hrs 30 mins", result.formattedDuration)
        assertEquals(8.5, result.decimalHours, 0.01)
        assertFalse(result.isNegative)
        assertFalse(result.crossesMidnight)
    }

    @Test
    fun testMidnightCrossingEnabled() {
        // 23:30 to 01:15 with crossMidnight = true
        // 23:30 -> 24:00 (30m) + 00:00 -> 01:15 (75m) = 105m = 1 hr 45 min
        val start = LocalTime.of(23, 30)
        val end = LocalTime.of(1, 15)
        val result = ClockDiffCalculator.calculate(
            startTime = start,
            endTime = end,
            crossMidnight = true
        )

        assertEquals(1, result.hours)
        assertEquals(45, result.minutes)
        assertEquals(105, result.totalMinutes)
        assertEquals(105 * 60L, result.totalSeconds)
        assertEquals("1 hr 45 mins", result.formattedDuration)
        assertEquals(1.75, result.decimalHours, 0.01)
        assertFalse(result.isNegative)
        assertTrue(result.crossesMidnight)
        assertNull(result.warningMessage)
    }

    @Test
    fun testMidnightCrossingDisabledDoesNotAssumeNextDay() {
        // 23:30 to 01:15 with crossMidnight = false
        // Should NOT assume next day, should indicate end is earlier and negative
        val start = LocalTime.of(23, 30)
        val end = LocalTime.of(1, 15)
        val result = ClockDiffCalculator.calculate(
            startTime = start,
            endTime = end,
            crossMidnight = false
        )

        assertTrue(result.isNegative)
        assertEquals(-1335, result.totalMinutes) // -22 hrs 15 min
        assertEquals(22, result.hours)
        assertEquals(15, result.minutes)
        assertNotNull(result.warningMessage)
        assertFalse(result.crossesMidnight)
    }

    @Test
    fun testExactHourBoundaries() {
        // 10:00 to 14:00 -> exact 4 hours, 0 minutes
        val start = LocalTime.of(10, 0)
        val end = LocalTime.of(14, 0)
        val result = ClockDiffCalculator.calculate(startTime = start, endTime = end)

        assertEquals(4, result.hours)
        assertEquals(0, result.minutes)
        assertEquals(240, result.totalMinutes)
        assertEquals("4 hrs", result.formattedDuration)
    }

    @Test
    fun testExactMinuteBoundaryOneMinute() {
        // 11:59 to 12:00 -> 1 minute
        val start = LocalTime.of(11, 59)
        val end = LocalTime.of(12, 0)
        val result = ClockDiffCalculator.calculate(startTime = start, endTime = end)

        assertEquals(0, result.hours)
        assertEquals(1, result.minutes)
        assertEquals(1, result.totalMinutes)
        assertEquals(60L, result.totalSeconds)
        assertEquals("1 min", result.formattedDuration)
    }

    @Test
    fun testDifferentDatesCalculation() {
        // Start: 2026-10-30 20:00, End: 2026-11-01 08:30
        // Duration: 1 day 12 hours 30 minutes (36 hrs 30 mins = 2190 mins)
        val start = LocalTime.of(20, 0)
        val startDate = LocalDate.of(2026, 10, 30)
        val end = LocalTime.of(8, 30)
        val endDate = LocalDate.of(2026, 11, 1)

        val result = ClockDiffCalculator.calculate(
            startTime = start,
            endTime = end,
            startDate = startDate,
            endDate = endDate
        )

        assertEquals(1, result.days)
        assertEquals(12, result.hours)
        assertEquals(30, result.minutes)
        assertEquals(2190, result.totalMinutes)
        assertEquals(2190 * 60L, result.totalSeconds)
        assertEquals("1 day 12 hrs 30 mins", result.formattedDuration)
        assertTrue(result.crossesMidnight)
        assertFalse(result.isNegative)
    }

    @Test
    fun testDifferentDatesEndDateEarlier() {
        // End date earlier than start date
        val start = LocalTime.of(10, 0)
        val startDate = LocalDate.of(2026, 11, 2)
        val end = LocalTime.of(10, 0)
        val endDate = LocalDate.of(2026, 11, 1)

        val result = ClockDiffCalculator.calculate(
            startTime = start,
            endTime = end,
            startDate = startDate,
            endDate = endDate
        )

        assertTrue(result.isNegative)
        assertEquals(1, result.days)
        assertEquals(0, result.hours)
        assertEquals(0, result.minutes)
        assertEquals(-1440, result.totalMinutes)
        assertNotNull(result.warningMessage)
    }

    @Test
    fun testTimeFormatting12HourAnd24Hour() {
        val morningTime = LocalTime.of(9, 5)
        val afternoonTime = LocalTime.of(14, 30)
        val midnight = LocalTime.of(0, 0)
        val noon = LocalTime.of(12, 0)

        // 24-hour format
        assertEquals("09:05", ClockDiffCalculator.formatTime(morningTime, is24Hour = true))
        assertEquals("14:30", ClockDiffCalculator.formatTime(afternoonTime, is24Hour = true))
        assertEquals("00:00", ClockDiffCalculator.formatTime(midnight, is24Hour = true))
        assertEquals("12:00", ClockDiffCalculator.formatTime(noon, is24Hour = true))

        // 12-hour format (case-insensitive check for AM/PM format)
        val morning12 = ClockDiffCalculator.formatTime(morningTime, is24Hour = false).uppercase()
        val afternoon12 = ClockDiffCalculator.formatTime(afternoonTime, is24Hour = false).uppercase()
        val midnight12 = ClockDiffCalculator.formatTime(midnight, is24Hour = false).uppercase()
        val noon12 = ClockDiffCalculator.formatTime(noon, is24Hour = false).uppercase()

        assertTrue(morning12.contains("9:05") && morning12.contains("AM"))
        assertTrue(afternoon12.contains("2:30") && afternoon12.contains("PM"))
        assertTrue(midnight12.contains("12:00") && midnight12.contains("AM"))
        assertTrue(noon12.contains("12:00") && noon12.contains("PM"))
    }
}
