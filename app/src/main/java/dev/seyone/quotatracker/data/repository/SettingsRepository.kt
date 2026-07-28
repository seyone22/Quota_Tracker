package dev.seyone.quotatracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback_enabled")
        val KEY_AUTO_SORT = booleanPreferencesKey("auto_sort_completed")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_WEEKEND_CHECKIN = booleanPreferencesKey("weekend_checkin_enabled")
    }

    val hapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAPTIC_FEEDBACK] ?: true
    }

    val autoSortCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_SORT] ?: true
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val weekendCheckInEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_WEEKEND_CHECKIN] ?: true
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun setAutoSortCompleted(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_SORT] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setWeekendCheckInEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WEEKEND_CHECKIN] = enabled
        }
    }
}
