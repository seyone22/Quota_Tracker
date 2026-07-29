package dev.seyone.quotatracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val KEY_SLEEP_HOURS_PER_NIGHT = intPreferencesKey("sleep_hours_per_night")
        val KEY_WORK_HOURS_PER_WEEK = intPreferencesKey("work_hours_per_week")
        val KEY_HAS_SEEN_FAB_TOOLTIP = booleanPreferencesKey("has_seen_fab_tooltip")
        val KEY_HAS_SEEN_COMPLETION_TIP = booleanPreferencesKey("has_seen_completion_tip")
        val KEY_MAINTENANCE_HOURS_PER_WEEK = intPreferencesKey("maintenance_hours_per_week")
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

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_COMPLETED_ONBOARDING] ?: false
    }

    val sleepHoursPerNight: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_SLEEP_HOURS_PER_NIGHT] ?: 8
    }

    val workHoursPerWeek: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_WORK_HOURS_PER_WEEK] ?: 40
    }

    val maintenanceHoursPerWeek: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_MAINTENANCE_HOURS_PER_WEEK] ?: 14
    }

    val hasSeenFabTooltip: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_SEEN_FAB_TOOLTIP] ?: false
    }

    val hasSeenCompletionTip: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_SEEN_COMPLETION_TIP] ?: false
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

    suspend fun setHasCompletedOnboarding(completed: Boolean, sleepHours: Int = 8, workHours: Int = 40) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_COMPLETED_ONBOARDING] = completed
            preferences[KEY_SLEEP_HOURS_PER_NIGHT] = sleepHours
            preferences[KEY_WORK_HOURS_PER_WEEK] = workHours
        }
    }

    suspend fun setSleepHoursPerNight(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SLEEP_HOURS_PER_NIGHT] = hours
        }
    }

    suspend fun setWorkHoursPerWeek(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WORK_HOURS_PER_WEEK] = hours
        }
    }

    suspend fun setMaintenanceHoursPerWeek(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MAINTENANCE_HOURS_PER_WEEK] = hours
        }
    }

    suspend fun setHasSeenFabTooltip(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_FAB_TOOLTIP] = seen
        }
    }

    suspend fun setHasSeenCompletionTip(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_COMPLETION_TIP] = seen
        }
    }
}
