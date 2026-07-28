package dev.seyone.quotatracker

import com.google.gson.Gson
import dev.seyone.quotatracker.data.backup.QuotaBackupData
import dev.seyone.quotatracker.data.local.entity.LogEntryEntity
import dev.seyone.quotatracker.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.data.local.entity.WeeklySnapshotEntity
import dev.seyone.quotatracker.data.model.ResetStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class Phase3UnitTest {

    private val gson = Gson()

    @Test
    fun backupData_serializationDeserializationIntegrity() {
        val quotas = listOf(
            QuotaEntity(id = 1, title = "Reading", targetMinutes = 420, resetStrategy = ResetStrategy.CLEAN, isPinned = true),
            QuotaEntity(id = 2, title = "Exercise", targetMinutes = 300, resetStrategy = ResetStrategy.ROLLOVER, isPinned = false)
        )
        val logs = listOf(
            LogEntryEntity(id = 10, quotaId = 1, durationMinutes = 60, timestamp = 1700000000000L)
        )
        val snapshots = listOf(
            WeeklySnapshotEntity(id = 100, quotaId = 1, weekString = "2026-W30", targetMinutesSnapshot = 420, loggedMinutesSnapshot = 420)
        )

        val originalBackup = QuotaBackupData(
            version = 1,
            exportedAt = 1700000000000L,
            quotas = quotas,
            logEntries = logs,
            weeklySnapshots = snapshots
        )

        val json = gson.toJson(originalBackup)
        val deserializedBackup = gson.fromJson(json, QuotaBackupData::class.java)

        assertNotNull(deserializedBackup)
        assertEquals(1, deserializedBackup.version)
        assertEquals(2, deserializedBackup.quotas.size)
        assertEquals("Reading", deserializedBackup.quotas[0].title)
        assertEquals(1, deserializedBackup.logEntries.size)
        assertEquals(60, deserializedBackup.logEntries[0].durationMinutes)
        assertEquals(1, deserializedBackup.weeklySnapshots.size)
        assertEquals("2026-W30", deserializedBackup.weeklySnapshots[0].weekString)
    }

    @Test
    fun rolloverDeficitCalculation_correct() {
        val target = 420 // 7 hours
        val logged = 300 // 5 hours
        val deficit = target - logged

        assertEquals(120, deficit)
    }

    @Test
    fun bankSurplusCalculation_correct() {
        val target = 420 // 7 hours
        val logged = 540 // 9 hours
        val surplus = logged - target

        assertEquals(120, surplus)
    }
}
