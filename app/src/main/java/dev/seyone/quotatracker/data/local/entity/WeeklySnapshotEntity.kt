package dev.seyone.quotatracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = QuotaEntity::class,
            parentColumns = ["id"],
            childColumns = ["quotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quotaId")]
)
data class WeeklySnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val quotaId: Int,
    val weekString: String,
    val targetMinutesSnapshot: Int,
    val loggedMinutesSnapshot: Int
)
