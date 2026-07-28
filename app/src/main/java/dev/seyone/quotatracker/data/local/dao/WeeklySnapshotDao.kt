package dev.seyone.quotatracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.seyone.quotatracker.data.local.entity.WeeklySnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklySnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: WeeklySnapshotEntity): Long

    @Query("SELECT * FROM weekly_snapshots WHERE quotaId = :quotaId ORDER BY weekString DESC")
    fun getSnapshotsForQuota(quotaId: Int): Flow<List<WeeklySnapshotEntity>>

    @Query("SELECT * FROM weekly_snapshots ORDER BY weekString DESC")
    fun getAllSnapshots(): Flow<List<WeeklySnapshotEntity>>

    @androidx.room.Transaction
    @Query("SELECT * FROM weekly_snapshots ORDER BY weekString DESC")
    fun getAllSnapshotsWithQuotas(): Flow<List<dev.seyone.quotatracker.data.local.model.WeeklySnapshotWithQuota>>
}
