package dev.seyone.quotatracker.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.quotatracker.data.repository.SettingsRepository
import dev.seyone.quotatracker.worker.SaturdayCheckInScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val hapticFeedbackEnabled: Boolean = true,
    val autoSortCompleted: Boolean = true,
    val themeMode: String = "SYSTEM",
    val weekendCheckInEnabled: Boolean = true
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.hapticFeedbackEnabled,
        settingsRepository.autoSortCompleted,
        settingsRepository.themeMode,
        settingsRepository.weekendCheckInEnabled
    ) { haptic, autoSort, theme, checkIn ->
        SettingsUiState(
            hapticFeedbackEnabled = haptic,
            autoSortCompleted = autoSort,
            themeMode = theme,
            weekendCheckInEnabled = checkIn
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedback(enabled)
        }
    }

    fun setAutoSortCompleted(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSortCompleted(enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setWeekendCheckInEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWeekendCheckInEnabled(enabled)
            if (enabled) {
                SaturdayCheckInScheduler.scheduleSaturdayCheckIn(context)
            } else {
                SaturdayCheckInScheduler.cancelSaturdayCheckIn(context)
            }
        }
    }

    class Factory(private val settingsRepository: SettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository) as T
        }
    }
}
