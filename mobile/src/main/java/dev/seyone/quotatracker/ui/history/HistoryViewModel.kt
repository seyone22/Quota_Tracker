package dev.seyone.quotatracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.quotatracker.core.data.local.QuotaDatabase
import dev.seyone.quotatracker.core.data.repository.QuotaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class QuotaSnapshotUiItem(
    val quotaTitle: String,
    val loggedMinutes: Int,
    val targetMinutes: Int,
    val isMet: Boolean,
    val formattedProgressText: String,
    val percentageText: String
)

data class WeekHistoryGroup(
    val weekString: String,
    val snapshots: List<QuotaSnapshotUiItem>
)

data class AgendaLogUiItem(
    val logId: Long,
    val quotaTitle: String,
    val formattedTime: String,
    val durationText: String,
    val isPositive: Boolean
)

data class AgendaDayGroup(
    val dayHeader: String,
    val logs: List<AgendaLogUiItem>
)

data class HistoryUiState(
    val historyGroups: List<WeekHistoryGroup> = emptyList(),
    val agendaGroups: List<AgendaDayGroup> = emptyList(),
    val isLoading: Boolean = false
)

class HistoryViewModel(
    private val database: QuotaDatabase,
    private val repository: QuotaRepository
) : ViewModel() {

    private val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val zoneId = ZoneId.systemDefault()

    val uiState: StateFlow<HistoryUiState> = combine(
        database.weeklySnapshotDao().getAllSnapshotsWithQuotas(),
        repository.getAllLogsWithQuotas()
    ) { snapshotList, logList ->

        val groupedSnapshots = snapshotList.groupBy { it.snapshot.weekString }
        val weekGroups = groupedSnapshots.map { (weekStr, items) ->
            val snapshotUiItems = items.map { model ->
                val title = model.quota?.title ?: "Deleted Quota"
                val logged = model.snapshot.loggedMinutesSnapshot
                val target = model.snapshot.targetMinutesSnapshot.coerceAtLeast(1)
                val isMet = logged >= target
                val loggedHrs = String.format(Locale.getDefault(), "%.1f", logged / 60.0)
                val targetHrs = String.format(Locale.getDefault(), "%.0f", target / 60.0)
                val pct = (logged.toFloat() / target.toFloat() * 100).toInt()

                QuotaSnapshotUiItem(
                    quotaTitle = title,
                    loggedMinutes = logged,
                    targetMinutes = target,
                    isMet = isMet,
                    formattedProgressText = "$loggedHrs / $targetHrs hrs",
                    percentageText = "$pct%"
                )
            }
            WeekHistoryGroup(weekString = weekStr, snapshots = snapshotUiItems)
        }.sortedByDescending { it.weekString }

        val agendaLogsByDay = logList.groupBy { log ->
            val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(log.logEntry.timestamp), zoneId)
            ldt.format(dayHeaderFormatter)
        }

        val agendaGroups = agendaLogsByDay.map { (dayStr, items) ->
            val logUiItems = items.map { logModel ->
                val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(logModel.logEntry.timestamp), zoneId)
                val timeStr = ldt.format(timeFormatter)
                val dur = logModel.logEntry.durationMinutes
                val sign = if (dur >= 0) "+" else ""
                val durStr = "$sign${dur}m"

                AgendaLogUiItem(
                    logId = logModel.logEntry.id.toLong(),
                    quotaTitle = logModel.quota?.title ?: "Deleted Quota",
                    formattedTime = timeStr,
                    durationText = durStr,
                    isPositive = dur >= 0
                )
            }
            AgendaDayGroup(dayHeader = dayStr, logs = logUiItems)
        }

        HistoryUiState(
            historyGroups = weekGroups,
            agendaGroups = agendaGroups,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun deleteLogEntry(logId: Long) {
        viewModelScope.launch {
            repository.deleteLogById(logId)
        }
    }

    class Factory(
        private val database: QuotaDatabase,
        private val repository: QuotaRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(database, repository) as T
        }
    }
}
