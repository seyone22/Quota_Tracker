package dev.seyone.quotatracker.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "log_entries",
    foreignKeys = [
        ForeignKey(
            entity = QuotaEntity::class,
            parentColumns = ["id"],
            childColumns = ["quotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quotaId"), Index("timestamp")]
)
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val quotaId: Int,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
