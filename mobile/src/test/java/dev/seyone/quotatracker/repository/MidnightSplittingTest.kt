package dev.seyone.quotatracker.repository

import dev.seyone.quotatracker.core.data.local.dao.LogEntryDao
import dev.seyone.quotatracker.core.data.local.dao.QuotaDao
import dev.seyone.quotatracker.core.data.local.entity.LogEntryEntity
import dev.seyone.quotatracker.core.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.core.data.repository.QuotaRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class MidnightSplittingTest {

    private val zoneId = ZoneId.of("UTC")

    class FakeQuotaDao : QuotaDao {
        override fun getQuotasWithCurrentWeekProgress(
            startOfWeekTimestamp: Long,
            endOfWeekTimestamp: Long
        ) = flowOf(emptyList<dev.seyone.quotatracker.core.data.local.model.QuotaWithProgress>())

        override suspend fun getQuotaById(id: Int): QuotaEntity? = null
        override fun getAllQuotas() = flowOf(emptyList<QuotaEntity>())
        override suspend fun insertQuota(quota: QuotaEntity): Long = 1L
        override suspend fun updateQuota(quota: QuotaEntity) {}
        override suspend fun archiveQuota(quotaId: Int) {}
        override suspend fun deleteQuotaById(quotaId: Int) {}
        override suspend fun deleteQuota(quota: QuotaEntity) {}
    }

    class FakeLogEntryDao : LogEntryDao {
        val insertedLogs = mutableListOf<LogEntryEntity>()
        private var idCounter = 1L

        override suspend fun insertLog(logEntry: LogEntryEntity): Long {
            insertedLogs.add(logEntry.copy(id = idCounter.toInt()))
            return idCounter++
        }

        override suspend fun insertLogs(logEntries: List<LogEntryEntity>): List<Long> {
            val ids = mutableListOf<Long>()
            logEntries.forEach { entry ->
                insertedLogs.add(entry.copy(id = idCounter.toInt()))
                ids.add(idCounter++)
            }
            return ids
        }

        override suspend fun deleteLog(logEntry: LogEntryEntity) {}
        override suspend fun deleteLogById(id: Long) {
            insertedLogs.removeIf { it.id.toLong() == id }
        }

        override suspend fun deleteLogsByIds(ids: List<Long>) {
            insertedLogs.removeIf { ids.contains(it.id.toLong()) }
        }

        override suspend fun deleteLogsForQuota(quotaId: Int) {
            insertedLogs.removeIf { it.quotaId == quotaId }
        }

        override fun getLogsForQuota(quotaId: Int) = flowOf(emptyList<LogEntryEntity>())
        override fun getAllLogEntries() = flowOf(insertedLogs)
        override fun getAllLogsWithQuotas() = flowOf(emptyList<dev.seyone.quotatracker.core.data.local.model.LogEntryWithQuota>())
        override suspend fun getLatestLogForQuota(quotaId: Int): LogEntryEntity? = null
    }

    @Test
    fun addLog_splitsAcrossMidnightCorrectly() = runBlocking {
        val fakeLogDao = FakeLogEntryDao()
        val fakeQuotaDao = FakeQuotaDao()
        val repository = QuotaRepository(fakeQuotaDao, fakeLogDao, zoneId)

        // 23:00 on Sunday July 26, 2026 UTC
        val sundayNight = LocalDateTime.of(2026, 7, 26, 23, 0, 0)
        val timestamp = sundayNight.toInstant(ZoneOffset.UTC).toEpochMilli()

        // 120 minutes log starting at 23:00 Sunday -> 60m Sunday, 60m Monday
        val insertedIds = repository.addLog(quotaId = 1, durationMinutes = 120, timestamp = timestamp)

        assertEquals(2, insertedIds.size)
        assertEquals(2, fakeLogDao.insertedLogs.size)

        // Check Sunday portion
        assertEquals(60, fakeLogDao.insertedLogs[0].durationMinutes)
        // Check Monday portion
        assertEquals(60, fakeLogDao.insertedLogs[1].durationMinutes)
    }

    @Test
    fun undoLogEntries_removesSplitLogs() = runBlocking {
        val fakeLogDao = FakeLogEntryDao()
        val fakeQuotaDao = FakeQuotaDao()
        val repository = QuotaRepository(fakeQuotaDao, fakeLogDao, zoneId)

        val sundayNight = LocalDateTime.of(2026, 7, 26, 23, 0, 0)
        val timestamp = sundayNight.toInstant(ZoneOffset.UTC).toEpochMilli()

        val insertedIds = repository.addLog(quotaId = 1, durationMinutes = 120, timestamp = timestamp)
        assertEquals(2, fakeLogDao.insertedLogs.size)

        repository.undoLogEntries(insertedIds)
        assertEquals(0, fakeLogDao.insertedLogs.size)
    }

    @Test
    fun subtractLog_preventsNegativeLoggedMinutes() = runBlocking {
        val fakeLogDao = FakeLogEntryDao()
        val fakeQuotaDao = FakeQuotaDao()
        val repository = QuotaRepository(fakeQuotaDao, fakeLogDao, zoneId)

        val result = repository.subtractLog(quotaId = 1, durationMinutes = 15)
        assertEquals(0, result.size)
        assertEquals(0, fakeLogDao.insertedLogs.size)
    }
}
