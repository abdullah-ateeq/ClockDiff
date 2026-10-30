package com.example.clockdiff

import com.example.clockdiff.data.model.ClockDiffHistoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockDiffHistoryTest {

    @Test
    fun testHistoryItemJsonSerializationAndDeserialization() {
        val item = ClockDiffHistoryItem(
            id = "test-id-123",
            timestamp = 1700000000000L,
            startHour = 23,
            startMinute = 30,
            endHour = 1,
            endMinute = 15,
            startDateIso = "2026-10-30",
            endDateIso = "2026-10-31",
            crossMidnight = true,
            days = 0,
            hours = 1,
            minutes = 45,
            totalMinutes = 105,
            totalSeconds = 6300,
            formattedDuration = "1 hr 45 mins",
            isNegative = false,
            crossesMidnight = true
        )

        val jsonString = item.toJsonString()
        val deserialized = ClockDiffHistoryItem.fromJsonString(jsonString)

        assertEquals(item.id, deserialized.id)
        assertEquals(item.timestamp, deserialized.timestamp)
        assertEquals(item.startHour, deserialized.startHour)
        assertEquals(item.startMinute, deserialized.startMinute)
        assertEquals(item.endHour, deserialized.endHour)
        assertEquals(item.endMinute, deserialized.endMinute)
        assertEquals(item.startDateIso, deserialized.startDateIso)
        assertEquals(item.endDateIso, deserialized.endDateIso)
        assertTrue(deserialized.crossMidnight)
        assertEquals(1, deserialized.hours)
        assertEquals(45, deserialized.minutes)
        assertEquals(105, deserialized.totalMinutes)
        assertEquals(6300, deserialized.totalSeconds)
        assertEquals("1 hr 45 mins", deserialized.formattedDuration)
        assertFalse(deserialized.isNegative)
        assertTrue(deserialized.crossesMidnight)
    }

    @Test
    fun testHistoryListSerializationAndEmptyHandling() {
        val list = listOf(
            ClockDiffHistoryItem(
                id = "1",
                startHour = 9,
                startMinute = 0,
                endHour = 17,
                endMinute = 0,
                hours = 8,
                minutes = 0,
                totalMinutes = 480,
                totalSeconds = 28800,
                formattedDuration = "8 hrs"
            ),
            ClockDiffHistoryItem(
                id = "2",
                startHour = 12,
                startMinute = 0,
                endHour = 12,
                endMinute = 30,
                hours = 0,
                minutes = 30,
                totalMinutes = 30,
                totalSeconds = 1800,
                formattedDuration = "30 mins"
            )
        )

        val jsonString = ClockDiffHistoryItem.listToJsonString(list)
        val deserializedList = ClockDiffHistoryItem.listFromJsonString(jsonString)

        assertEquals(2, deserializedList.size)
        assertEquals("1", deserializedList[0].id)
        assertEquals(480, deserializedList[0].totalMinutes)
        assertEquals("2", deserializedList[1].id)
        assertEquals(30, deserializedList[1].totalMinutes)

        // Empty / null test
        assertTrue(ClockDiffHistoryItem.listFromJsonString(null).isEmpty())
        assertTrue(ClockDiffHistoryItem.listFromJsonString("").isEmpty())
        assertTrue(ClockDiffHistoryItem.listFromJsonString("[]").isEmpty())
    }
}
