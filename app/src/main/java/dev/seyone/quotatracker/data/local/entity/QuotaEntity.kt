package dev.seyone.quotatracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.seyone.quotatracker.data.model.ResetStrategy

@Entity(tableName = "quotas")
data class QuotaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val targetMinutes: Int,
    val resetStrategy: ResetStrategy,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
