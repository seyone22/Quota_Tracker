package dev.seyone.quotatracker.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import dev.seyone.quotatracker.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.data.local.entity.WeeklySnapshotEntity

data class WeeklySnapshotWithQuota(
    @Embedded val snapshot: WeeklySnapshotEntity,
    @Relation(
        parentColumn = "quotaId",
        entityColumn = "id"
    )
    val quota: QuotaEntity?
)
