package dev.seyone.quotatracker.core.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import dev.seyone.quotatracker.core.data.local.entity.LogEntryEntity
import dev.seyone.quotatracker.core.data.local.entity.QuotaEntity

data class LogEntryWithQuota(
    @Embedded val logEntry: LogEntryEntity,
    @Relation(
        parentColumn = "quotaId",
        entityColumn = "id"
    )
    val quota: QuotaEntity?
)
