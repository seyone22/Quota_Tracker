package dev.seyone.quotatracker.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.quotatracker.data.repository.QuotaRepository
import dev.seyone.quotatracker.data.repository.SettingsRepository
import dev.seyone.quotatracker.ui.settings.components.WeekAllocationData
import dev.seyone.quotatracker.worker.SaturdayCheckInScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SettingsUiState(
    val hapticFeedbackEnabled: Boolean = true,
    val autoSortCompleted: Boolean = true,
    val themeMode: String = "SYSTEM",
    val weekendCheckInEnabled: Boolean = true,
    val sleepHoursPerNight: Int = 8,
    val workHoursPerWeek: Int = 40,
    val maintenanceHoursPerWeek: Int = 14,
    val cardStyle: dev.seyone.quotatracker.data.model.QuotaCardStyle = dev.seyone.quotatracker.data.model.QuotaCardStyle.DUAL_TONE,
    val showPreciseTime: Boolean = false,
    val customNonNegotiables: List<dev.seyone.quotatracker.data.model.CustomNonNegotiable> = emptyList(),
    val allocationData: WeekAllocationData = WeekAllocationData(
        sleepHours = 56,
        workHours = 40,
        maintenanceHours = 14,
        customNonNegotiableHours = 0,
        quotaTargetHours = 0,
        unfilledHours = 58
    )
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val quotaRepository: QuotaRepository? = null
) : ViewModel() {

    private val prefs1 = combine(
        settingsRepository.hapticFeedbackEnabled,
        settingsRepository.autoSortCompleted,
        settingsRepository.themeMode,
        settingsRepository.weekendCheckInEnabled,
        settingsRepository.cardStyle
    ) { haptic, autoSort, theme, checkIn, cardStyle ->
        listOf(haptic, autoSort, theme, checkIn, cardStyle)
    }

    private val preferencesFlow = combine(
        prefs1,
        settingsRepository.showPreciseTime
    ) { p1, precise ->
        listOf(p1[0], p1[1], p1[2], p1[3], p1[4], precise)
    }

    private val baselineFlow = combine(
        settingsRepository.sleepHoursPerNight,
        settingsRepository.workHoursPerWeek,
        settingsRepository.maintenanceHoursPerWeek,
        settingsRepository.customNonNegotiables,
        quotaRepository?.getAllQuotas() ?: flowOf(emptyList())
    ) { sleepNight, workWeek, maintWeek, customs, quotas ->
        val sleepWeekly = sleepNight * 7
        val customWeekly = customs.sumOf { it.hoursPerWeek }
        val activityWeekly = (quotas.sumOf { it.targetMinutes } / 60.0f).roundToInt()
        val unfilled = maxOf(0, 168 - sleepWeekly - workWeek - maintWeek - customWeekly - activityWeekly)

        val allocation = WeekAllocationData(
            sleepHours = sleepWeekly,
            workHours = workWeek,
            maintenanceHours = maintWeek,
            customNonNegotiableHours = customWeekly,
            quotaTargetHours = activityWeekly,
            unfilledHours = unfilled
        )
        listOf(sleepNight, workWeek, maintWeek, customs, allocation)
    }

    val uiState: StateFlow<SettingsUiState> = combine(preferencesFlow, baselineFlow) { pref, base ->
        val haptic = pref[0] as Boolean
        val autoSort = pref[1] as Boolean
        val theme = pref[2] as String
        val checkIn = pref[3] as Boolean
        val cardStyle = pref[4] as dev.seyone.quotatracker.data.model.QuotaCardStyle
        val showPrecise = pref[5] as Boolean

        val sleepNight = base[0] as Int
        val workWeek = base[1] as Int
        val maintWeek = base[2] as Int
        @Suppress("UNCHECKED_CAST")
        val customs = base[3] as List<dev.seyone.quotatracker.data.model.CustomNonNegotiable>
        val allocation = base[4] as WeekAllocationData

        SettingsUiState(
            hapticFeedbackEnabled = haptic,
            autoSortCompleted = autoSort,
            themeMode = theme,
            weekendCheckInEnabled = checkIn,
            sleepHoursPerNight = sleepNight,
            workHoursPerWeek = workWeek,
            maintenanceHoursPerWeek = maintWeek,
            cardStyle = cardStyle,
            showPreciseTime = showPrecise,
            customNonNegotiables = customs,
            allocationData = allocation
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

    fun setSleepHoursPerNight(hours: Int) {
        viewModelScope.launch {
            settingsRepository.setSleepHoursPerNight(hours)
        }
    }

    fun setWorkHoursPerWeek(hours: Int) {
        viewModelScope.launch {
            settingsRepository.setWorkHoursPerWeek(hours)
        }
    }

    fun setMaintenanceHoursPerWeek(hours: Int) {
        viewModelScope.launch {
            settingsRepository.setMaintenanceHoursPerWeek(hours)
        }
    }

    fun setCardStyle(style: dev.seyone.quotatracker.data.model.QuotaCardStyle) {
        viewModelScope.launch {
            settingsRepository.setCardStyle(style)
        }
    }

    fun setShowPreciseTime(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowPreciseTime(enabled)
        }
    }

    fun addCustomNonNegotiable(name: String, emoji: String, hoursPerWeek: Int) {
        viewModelScope.launch {
            val current = uiState.value.customNonNegotiables.toMutableList()
            current.add(
                dev.seyone.quotatracker.data.model.CustomNonNegotiable(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    emoji = emoji,
                    hoursPerWeek = hoursPerWeek
                )
            )
            settingsRepository.setCustomNonNegotiables(current)
        }
    }

    fun updateCustomNonNegotiable(item: dev.seyone.quotatracker.data.model.CustomNonNegotiable) {
        viewModelScope.launch {
            val current = uiState.value.customNonNegotiables.toMutableList()
            val index = current.indexOfFirst { it.id == item.id }
            if (index != -1) {
                current[index] = item
                settingsRepository.setCustomNonNegotiables(current)
            }
        }
    }

    fun deleteCustomNonNegotiable(id: String) {
        viewModelScope.launch {
            val current = uiState.value.customNonNegotiables.filterNot { it.id == id }
            settingsRepository.setCustomNonNegotiables(current)
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val quotaRepository: QuotaRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository, quotaRepository) as T
        }
    }
}
