package dev.seyone.quotatracker.data.repository

import dev.seyone.quotatracker.data.local.dao.LogEntryDao
import dev.seyone.quotatracker.data.local.dao.QuotaDao
import dev.seyone.quotatracker.data.local.entity.LogEntryEntity
import dev.seyone.quotatracker.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.data.local.model.QuotaWithProgress
import dev.seyone.quotatracker.util.WeekUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.ZoneId

class QuotaRepository(
    private val quotaDao: QuotaDao,
    private val logEntryDao: LogEntryDao,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    /**
     * Returns a Flow of QuotaWithProgress for the current week starting Mon 00:00 to Sun 23:59,
     * ensuring loggedMinutes is never negative and omitting archived quotas.
     */
    fun getQuotasWithCurrentWeekProgress(
        nowTimestamp: Long = System.currentTimeMillis()
    ): Flow<List<QuotaWithProgress>> {
        val (startOfWeek, endOfWeek) = WeekUtils.getCurrentWeekRange(nowTimestamp, zoneId)
        return quotaDao.getQuotasWithCurrentWeekProgress(startOfWeek, endOfWeek)
            .map { list ->
                list.map { item ->
                    item.copy(loggedMinutes = item.loggedMinutes.coerceAtLeast(0))
                }
            }
    }

    fun getAllQuotas(): Flow<List<QuotaEntity>> = quotaDao.getAllQuotas()

    /**
     * Adds a log entry (or entries if split across midnight).
     * Returns the list of inserted LogEntry IDs so the caller can perform an undo action.
     */
    suspend fun addLog(
        quotaId: Int,
        durationMinutes: Int,
        timestamp: Long = System.currentTimeMillis()
    ): List<Long> {
        val segments = WeekUtils.splitLogAcrossMidnight(timestamp, durationMinutes, zoneId)
        if (segments.isEmpty()) return emptyList()

        val logEntities = segments.map { (segmentTime, segmentMinutes) ->
            LogEntryEntity(
                quotaId = quotaId,
                durationMinutes = segmentMinutes,
                timestamp = segmentTime
            )
        }

        return logEntryDao.insertLogs(logEntities)
    }

    /**
     * Reverts / deletes specific log entries by their generated IDs.
     */
    suspend fun undoLogEntries(ids: List<Long>) {
        if (ids.isNotEmpty()) {
            logEntryDao.deleteLogsByIds(ids)
        }
    }

    suspend fun deleteLogById(id: Long) {
        logEntryDao.deleteLogById(id)
    }

    fun getAllLogsWithQuotas(): Flow<List<dev.seyone.quotatracker.data.local.model.LogEntryWithQuota>> {
        return logEntryDao.getAllLogsWithQuotas()
    }

    fun getLogsForQuota(quotaId: Int): Flow<List<LogEntryEntity>> {
        return logEntryDao.getLogsForQuota(quotaId)
    }

    /**
     * Handles subtracting log duration (e.g. -15m on long-pressing an active card).
     * Prevents total logged time from ever dropping below 0.
     */
    suspend fun subtractLog(
        quotaId: Int,
        durationMinutes: Int = 15,
        timestamp: Long = System.currentTimeMillis()
    ): List<Long> {
        val (startOfWeek, endOfWeek) = WeekUtils.getCurrentWeekRange(timestamp, zoneId)
        val currentProgressList = quotaDao.getQuotasWithCurrentWeekProgress(startOfWeek, endOfWeek).firstOrNull() ?: emptyList()
        val currentQuota = currentProgressList.firstOrNull { it.quota.id == quotaId }
        val currentLogged = (currentQuota?.loggedMinutes ?: 0).coerceAtLeast(0)

        if (currentLogged <= 0) {
            return emptyList()
        }

        val minutesToSubtract = minOf(durationMinutes, currentLogged)
        val negativeEntry = LogEntryEntity(
            quotaId = quotaId,
            durationMinutes = -minutesToSubtract,
            timestamp = timestamp
        )
        return listOf(logEntryDao.insertLog(negativeEntry))
    }

    suspend fun addQuota(quota: QuotaEntity): Long {
        return quotaDao.insertQuota(quota)
    }

    suspend fun updateQuota(quota: QuotaEntity) {
        quotaDao.updateQuota(quota)
    }

    /**
     * Soft delete: mark isArchived = true so quota is removed from active week but historical logs remain intact.
     */
    suspend fun archiveQuota(quotaId: Int) {
        quotaDao.archiveQuota(quotaId)
    }

    /**
     * Hard delete: completely delete the quota and all associated time logs from database.
     */
    suspend fun hardDeleteQuota(quotaId: Int) {
        logEntryDao.deleteLogsForQuota(quotaId)
        quotaDao.deleteQuotaById(quotaId)
    }

    suspend fun deleteQuota(quota: QuotaEntity) {
        quotaDao.deleteQuota(quota)
    }
}
