package dev.seyone.quotatracker.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.seyone.quotatracker.core.domain.model.CustomNonNegotiable
import dev.seyone.quotatracker.core.domain.model.QuotaCardStyle
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
        val KEY_CARD_STYLE = stringPreferencesKey("quota_card_style")
        val KEY_SHOW_PRECISE_TIME = booleanPreferencesKey("show_precise_time")
        val KEY_CUSTOM_NON_NEGOTIABLES = stringPreferencesKey("custom_non_negotiables")
    }

    val showPreciseTime: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SHOW_PRECISE_TIME] ?: false
    }

    val customNonNegotiables: Flow<List<CustomNonNegotiable>> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_CUSTOM_NON_NEGOTIABLES] ?: ""
        CustomNonNegotiable.listFromJsonString(raw)
    }

    val cardStyle: Flow<QuotaCardStyle> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_CARD_STYLE] ?: QuotaCardStyle.DUAL_TONE.name
        try {
            QuotaCardStyle.valueOf(raw)
        } catch (_: Exception) {
            QuotaCardStyle.DUAL_TONE
        }
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

    suspend fun setCardStyle(style: QuotaCardStyle) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CARD_STYLE] = style.name
        }
    }

    suspend fun setShowPreciseTime(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SHOW_PRECISE_TIME] = enabled
        }
    }

    suspend fun setCustomNonNegotiables(list: List<CustomNonNegotiable>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CUSTOM_NON_NEGOTIABLES] = CustomNonNegotiable.listToJsonString(list)
        }
    }
}
