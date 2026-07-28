package dev.seyone.quotatracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.quotatracker.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.data.local.model.QuotaWithProgress
import dev.seyone.quotatracker.data.model.ResetStrategy
import dev.seyone.quotatracker.data.repository.QuotaRepository
import dev.seyone.quotatracker.util.WeekUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class QuotaUiItem(
    val quota: QuotaEntity,
    val loggedMinutes: Int,
    val targetMinutes: Int,
    val progressFraction: Float,
    val isCompleted: Boolean,
    val formattedProgressText: String
)

data class DashboardUiState(
    val quotaItems: List<QuotaUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val currentWeekText: String = ""
)

sealed interface DashboardUiEvent {
    data class ShowUndoSnackbar(val message: String, val lastLogIds: List<Long>) : DashboardUiEvent
    data class ShowMessage(val message: String) : DashboardUiEvent
}

class QuotaDashboardViewModel(
    private val repository: QuotaRepository
) : ViewModel() {

    private val _uiEvents = MutableSharedFlow<DashboardUiEvent>()
    val uiEvents: SharedFlow<DashboardUiEvent> = _uiEvents.asSharedFlow()

    // Dialog State Controls
    val showAddDialog = MutableStateFlow(false)
    val manualOverrideQuota = MutableStateFlow<QuotaUiItem?>(null)

    val uiState: StateFlow<DashboardUiState> = repository.getQuotasWithCurrentWeekProgress()
        .map { list ->
            val items = list.map { createUiItem(it) }
            // Sorting Rule: Unfinished quotas sat at the top, completed (>=100%) at bottom.
            // Secondary sort: pinned first, then newest creation time.
            val sorted = items.sortedWith(
                compareBy<QuotaUiItem> { it.isCompleted }
                    .thenByDescending { it.quota.isPinned }
                    .thenByDescending { it.quota.createdAt }
            )
            DashboardUiState(
                quotaItems = sorted,
                isLoading = false,
                currentWeekText = WeekUtils.getWeekString()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true, currentWeekText = WeekUtils.getWeekString())
        )

    fun onQuickLog(quotaItem: QuotaUiItem, minutes: Int) {
        viewModelScope.launch {
            val insertedIds = repository.addLog(quotaItem.quota.id, minutes)
            val minutesStr = if (minutes >= 60) "${minutes / 60}h" else "${minutes}m"
            _uiEvents.emit(
                DashboardUiEvent.ShowUndoSnackbar(
                    message = "Logged +$minutesStr to ${quotaItem.quota.title}",
                    lastLogIds = insertedIds
                )
            )
        }
    }

    fun onUndoLog(logIds: List<Long>) {
        viewModelScope.launch {
            repository.undoLogEntries(logIds)
            _uiEvents.emit(DashboardUiEvent.ShowMessage("Reverted time entry"))
        }
    }

    fun onLongPressCard(item: QuotaUiItem) {
        if (item.isCompleted) {
            // Completed card long-press triggers manual override dialog
            manualOverrideQuota.value = item
        } else {
            // Unfinished card long-press triggers -15m undo/subtract directly
            viewModelScope.launch {
                val ids = repository.subtractLog(item.quota.id, 15)
                _uiEvents.emit(
                    DashboardUiEvent.ShowUndoSnackbar(
                        message = "Removed -15m from ${item.quota.title}",
                        lastLogIds = ids
                    )
                )
            }
        }
    }

    fun onAddQuota(
        title: String,
        targetHours: Int,
        targetMinutes: Int,
        resetStrategy: ResetStrategy,
        isPinned: Boolean
    ) {
        viewModelScope.launch {
            val totalMinutes = (targetHours * 60) + targetMinutes
            if (title.isBlank() || totalMinutes <= 0) return@launch

            val entity = QuotaEntity(
                title = title.trim(),
                targetMinutes = totalMinutes,
                resetStrategy = resetStrategy,
                isPinned = isPinned
            )
            repository.addQuota(entity)
            showAddDialog.value = false
        }
    }

    fun onManualOverrideLog(quotaId: Int, extraMinutes: Int) {
        viewModelScope.launch {
            if (extraMinutes <= 0) return@launch
            val ids = repository.addLog(quotaId, extraMinutes)
            manualOverrideQuota.value = null
            val minStr = if (extraMinutes >= 60) "${extraMinutes / 60}h ${extraMinutes % 60}m" else "${extraMinutes}m"
            _uiEvents.emit(
                DashboardUiEvent.ShowUndoSnackbar(
                    message = "Logged extra +$minStr",
                    lastLogIds = ids
                )
            )
        }
    }

    private fun createUiItem(model: QuotaWithProgress): QuotaUiItem {
        val target = model.quota.targetMinutes.coerceAtLeast(1)
        val logged = model.loggedMinutes.coerceAtLeast(0)
        val fraction = (logged.toFloat() / target.toFloat()).coerceAtMost(1.0f)
        val isCompleted = logged >= target

        val loggedHoursStr = formatHours(logged)
        val targetHoursStr = formatHours(target)
        val progressText = "$loggedHoursStr / $targetHoursStr hrs"

        return QuotaUiItem(
            quota = model.quota,
            loggedMinutes = logged,
            targetMinutes = target,
            progressFraction = fraction,
            isCompleted = isCompleted,
            formattedProgressText = progressText
        )
    }

    private fun formatHours(minutes: Int): String {
        val hours = minutes / 60.0
        return if (hours % 1.0 == 0.0) {
            String.format(Locale.getDefault(), "%.0f", hours)
        } else {
            String.format(Locale.getDefault(), "%.1f", hours)
        }
    }

    class Factory(private val repository: QuotaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuotaDashboardViewModel(repository) as T
        }
    }
}
