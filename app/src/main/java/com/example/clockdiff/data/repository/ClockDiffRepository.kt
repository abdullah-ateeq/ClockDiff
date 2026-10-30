package com.example.clockdiff.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.clockdiff.data.model.AppSettings
import com.example.clockdiff.data.model.ClockDiffHistoryItem
import com.example.clockdiff.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clock_diff_preferences")

interface ClockDiffRepository {
    val settingsFlow: Flow<AppSettings>
    val historyFlow: Flow<List<ClockDiffHistoryItem>>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setIs24HourFormat(is24Hour: Boolean)
    suspend fun setDefaultCrossMidnight(enabled: Boolean)
    suspend fun saveHistoryItem(item: ClockDiffHistoryItem)
    suspend fun deleteHistoryItem(id: String)
    suspend fun clearAllHistory()
}

class ClockDiffRepositoryImpl(private val context: Context) : ClockDiffRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val DEFAULT_CROSS_MIDNIGHT = booleanPreferencesKey("default_cross_midnight")
        val HISTORY_JSON = stringPreferencesKey("history_json")
    }

    override val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val themeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeString)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        val is24Hour = preferences[PreferencesKeys.IS_24_HOUR] ?: false
        val defaultCrossMidnight = preferences[PreferencesKeys.DEFAULT_CROSS_MIDNIGHT] ?: true

        AppSettings(
            themeMode = themeMode,
            is24HourFormat = is24Hour,
            defaultCrossMidnight = defaultCrossMidnight
        )
    }

    override val historyFlow: Flow<List<ClockDiffHistoryItem>> = context.dataStore.data.map { preferences ->
        val historyJson = preferences[PreferencesKeys.HISTORY_JSON]
        ClockDiffHistoryItem.listFromJsonString(historyJson)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    override suspend fun setIs24HourFormat(is24Hour: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_24_HOUR] = is24Hour
        }
    }

    override suspend fun setDefaultCrossMidnight(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_CROSS_MIDNIGHT] = enabled
        }
    }

    override suspend fun saveHistoryItem(item: ClockDiffHistoryItem) {
        context.dataStore.edit { preferences ->
            val currentList = ClockDiffHistoryItem.listFromJsonString(preferences[PreferencesKeys.HISTORY_JSON]).toMutableList()
            // Add new item at the top (most recent first)
            currentList.removeAll { it.id == item.id }
            currentList.add(0, item)
            // Keep top 100 entries to prevent unbounded growth
            val trimmedList = currentList.take(100)
            preferences[PreferencesKeys.HISTORY_JSON] = ClockDiffHistoryItem.listToJsonString(trimmedList)
        }
    }

    override suspend fun deleteHistoryItem(id: String) {
        context.dataStore.edit { preferences ->
            val currentList = ClockDiffHistoryItem.listFromJsonString(preferences[PreferencesKeys.HISTORY_JSON]).toMutableList()
            currentList.removeAll { it.id == id }
            preferences[PreferencesKeys.HISTORY_JSON] = ClockDiffHistoryItem.listToJsonString(currentList)
        }
    }

    override suspend fun clearAllHistory() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HISTORY_JSON] = "[]"
        }
    }
}
