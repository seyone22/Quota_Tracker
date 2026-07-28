package dev.seyone.quotatracker.data.local.model

import androidx.room.Embedded
import dev.seyone.quotatracker.data.local.entity.QuotaEntity

data class QuotaWithProgress(
    @Embedded val quota: QuotaEntity,
    val loggedMinutes: Int
)
