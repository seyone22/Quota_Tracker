package dev.seyone.quotatracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.seyone.quotatracker.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.data.local.model.QuotaWithProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotaDao {
    @Query("""
        SELECT q.*, COALESCE(SUM(l.durationMinutes), 0) AS loggedMinutes 
        FROM quotas q 
        LEFT JOIN log_entries l ON q.id = l.quotaId 
            AND l.timestamp >= :startOfWeekTimestamp 
            AND l.timestamp <= :endOfWeekTimestamp 
        GROUP BY q.id
    """)
    fun getQuotasWithCurrentWeekProgress(
        startOfWeekTimestamp: Long,
        endOfWeekTimestamp: Long
    ): Flow<List<QuotaWithProgress>>

    @Query("SELECT * FROM quotas WHERE id = :id")
    suspend fun getQuotaById(id: Int): QuotaEntity?

    @Query("SELECT * FROM quotas")
    fun getAllQuotas(): Flow<List<QuotaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuota(quota: QuotaEntity): Long

    @Update
    suspend fun updateQuota(quota: QuotaEntity)

    @Delete
    suspend fun deleteQuota(quota: QuotaEntity)
}
