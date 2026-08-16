package dev.seyone.quotatracker.core.data.backup

import dev.seyone.quotatracker.core.data.local.entity.LogEntryEntity
import dev.seyone.quotatracker.core.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.core.data.local.entity.WeeklySnapshotEntity

data class QuotaBackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val quotas: List<QuotaEntity>,
    val logEntries: List<LogEntryEntity>,
    val weeklySnapshots: List<WeeklySnapshotEntity>
)
