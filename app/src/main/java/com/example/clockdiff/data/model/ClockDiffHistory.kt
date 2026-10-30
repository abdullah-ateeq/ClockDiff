package com.example.clockdiff.data.model

import java.util.UUID

data class ClockDiffHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val startDateIso: String? = null,
    val endDateIso: String? = null,
    val crossMidnight: Boolean = false,
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val totalMinutes: Long = 0,
    val totalSeconds: Long = 0,
    val formattedDuration: String = "",
    val isNegative: Boolean = false,
    val crossesMidnight: Boolean = false
) {
    fun toJsonString(): String = buildString {
        append("{")
        append("\"id\":\"").append(id).append("\",")
        append("\"timestamp\":").append(timestamp).append(",")
        append("\"startHour\":").append(startHour).append(",")
        append("\"startMinute\":").append(startMinute).append(",")
        append("\"endHour\":").append(endHour).append(",")
        append("\"endMinute\":").append(endMinute).append(",")
        if (startDateIso != null) append("\"startDateIso\":\"").append(startDateIso).append("\",")
        if (endDateIso != null) append("\"endDateIso\":\"").append(endDateIso).append("\",")
        append("\"crossMidnight\":").append(crossMidnight).append(",")
        append("\"days\":").append(days).append(",")
        append("\"hours\":").append(hours).append(",")
        append("\"minutes\":").append(minutes).append(",")
        append("\"totalMinutes\":").append(totalMinutes).append(",")
        append("\"totalSeconds\":").append(totalSeconds).append(",")
        append("\"formattedDuration\":\"").append(escape(formattedDuration)).append("\",")
        append("\"isNegative\":").append(isNegative).append(",")
        append("\"crossesMidnight\":").append(crossesMidnight)
        append("}")
    }

    companion object {
        private fun escape(text: String): String {
            return text.replace("\"", "\\\"")
        }

        private fun extractString(json: String, key: String): String? {
            val regex = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            return regex.find(json)?.groupValues?.get(1)
        }

        private fun extractLong(json: String, key: String, default: Long = 0L): Long {
            val regex = "\"$key\"\\s*:\\s*(-?\\d+)".toRegex()
            return regex.find(json)?.groupValues?.get(1)?.toLongOrNull() ?: default
        }

        private fun extractInt(json: String, key: String, default: Int = 0): Int {
            val regex = "\"$key\"\\s*:\\s*(-?\\d+)".toRegex()
            return regex.find(json)?.groupValues?.get(1)?.toIntOrNull() ?: default
        }

        private fun extractBoolean(json: String, key: String, default: Boolean = false): Boolean {
            val regex = "\"$key\"\\s*:\\s*(true|false)".toRegex()
            return regex.find(json)?.groupValues?.get(1)?.toBooleanStrictOrNull() ?: default
        }

        fun fromJsonString(json: String): ClockDiffHistoryItem {
            return ClockDiffHistoryItem(
                id = extractString(json, "id") ?: UUID.randomUUID().toString(),
                timestamp = extractLong(json, "timestamp", System.currentTimeMillis()),
                startHour = extractInt(json, "startHour"),
                startMinute = extractInt(json, "startMinute"),
                endHour = extractInt(json, "endHour"),
                endMinute = extractInt(json, "endMinute"),
                startDateIso = extractString(json, "startDateIso"),
                endDateIso = extractString(json, "endDateIso"),
                crossMidnight = extractBoolean(json, "crossMidnight"),
                days = extractLong(json, "days"),
                hours = extractLong(json, "hours"),
                minutes = extractLong(json, "minutes"),
                totalMinutes = extractLong(json, "totalMinutes"),
                totalSeconds = extractLong(json, "totalSeconds"),
                formattedDuration = extractString(json, "formattedDuration") ?: "",
                isNegative = extractBoolean(json, "isNegative"),
                crossesMidnight = extractBoolean(json, "crossesMidnight")
            )
        }

        fun listToJsonString(items: List<ClockDiffHistoryItem>): String {
            return "[" + items.joinToString(",") { it.toJsonString() } + "]"
        }

        fun listFromJsonString(jsonString: String?): List<ClockDiffHistoryItem> {
            if (jsonString.isNullOrBlank() || jsonString.trim() == "[]") return emptyList()
            val list = mutableListOf<ClockDiffHistoryItem>()
            try {
                val objectRegex = "\\{[^{}]*\\}".toRegex()
                val matches = objectRegex.findAll(jsonString)
                for (match in matches) {
                    list.add(fromJsonString(match.value))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
    }
}
