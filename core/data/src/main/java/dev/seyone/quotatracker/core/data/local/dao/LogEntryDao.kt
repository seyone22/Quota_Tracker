package dev.seyone.quotatracker.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.seyone.quotatracker.core.data.local.entity.LogEntryEntity
import dev.seyone.quotatracker.core.data.local.model.LogEntryWithQuota
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(logEntry: LogEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logEntries: List<LogEntryEntity>): List<Long>

    @Delete
    suspend fun deleteLog(logEntry: LogEntryEntity)

    @Query("DELETE FROM log_entries WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM log_entries WHERE id IN (:ids)")
    suspend fun deleteLogsByIds(ids: List<Long>)

    @Query("DELETE FROM log_entries WHERE quotaId = :quotaId")
    suspend fun deleteLogsForQuota(quotaId: Int)

    @Query("SELECT * FROM log_entries WHERE quotaId = :quotaId ORDER BY timestamp DESC")
    fun getLogsForQuota(quotaId: Int): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllLogEntries(): Flow<List<LogEntryEntity>>

    @Transaction
    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllLogsWithQuotas(): Flow<List<LogEntryWithQuota>>

    @Query("SELECT * FROM log_entries WHERE quotaId = :quotaId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestLogForQuota(quotaId: Int): LogEntryEntity?
}
