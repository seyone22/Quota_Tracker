package dev.seyone.quotatracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.widgetDataStore by preferencesDataStore(name = "widget_settings")

data class WidgetSettings(
    val opacity: Float = 0.85f,
    val themeMode: String = "SYSTEM",
    val cornerRadiusDp: Int = 24,
    val selectedQuotaIds: Set<Int> = emptySet(), // emptySet means ALL selected by default
    val showQuickLogButtons: Boolean = true
)

class WidgetSettingsRepository(private val context: Context) {

    companion object {
        val KEY_OPACITY = floatPreferencesKey("widget_opacity")
        val KEY_THEME_MODE = stringPreferencesKey("widget_theme_mode")
        val KEY_CORNER_RADIUS = intPreferencesKey("widget_corner_radius")
        val KEY_SELECTED_QUOTA_IDS = stringPreferencesKey("widget_selected_quota_ids")
        val KEY_SHOW_QUICK_LOG = booleanPreferencesKey("widget_show_quick_log")
    }

    val widgetSettings: Flow<WidgetSettings> = context.widgetDataStore.data.map { prefs ->
        val rawIds = prefs[KEY_SELECTED_QUOTA_IDS] ?: "ALL"
        val idsSet = if (rawIds.isBlank() || rawIds == "ALL") {
            emptySet()
        } else {
            rawIds.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }

        WidgetSettings(
            opacity = prefs[KEY_OPACITY] ?: 0.85f,
            themeMode = prefs[KEY_THEME_MODE] ?: "SYSTEM",
            cornerRadiusDp = prefs[KEY_CORNER_RADIUS] ?: 24,
            selectedQuotaIds = idsSet,
            showQuickLogButtons = prefs[KEY_SHOW_QUICK_LOG] ?: true
        )
    }

    suspend fun updateWidgetSettings(settings: WidgetSettings) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_OPACITY] = settings.opacity
            prefs[KEY_THEME_MODE] = settings.themeMode
            prefs[KEY_CORNER_RADIUS] = settings.cornerRadiusDp
            prefs[KEY_SELECTED_QUOTA_IDS] = if (settings.selectedQuotaIds.isEmpty()) "ALL" else settings.selectedQuotaIds.joinToString(",")
            prefs[KEY_SHOW_QUICK_LOG] = settings.showQuickLogButtons
        }
    }
}
