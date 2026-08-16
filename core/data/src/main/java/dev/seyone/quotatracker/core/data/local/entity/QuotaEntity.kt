package dev.seyone.quotatracker.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.seyone.quotatracker.core.domain.model.ResetStrategy

@Entity(tableName = "quotas")
data class QuotaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val targetMinutes: Int,
    val resetStrategy: ResetStrategy,
    val isPinned: Boolean = false,
    val iconKey: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
