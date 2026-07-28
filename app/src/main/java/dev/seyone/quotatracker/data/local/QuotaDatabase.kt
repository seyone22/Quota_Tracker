package dev.seyone.quotatracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.seyone.quotatracker.data.local.dao.LogEntryDao
import dev.seyone.quotatracker.data.local.dao.QuotaDao
import dev.seyone.quotatracker.data.local.dao.WeeklySnapshotDao
import dev.seyone.quotatracker.data.local.entity.LogEntryEntity
import dev.seyone.quotatracker.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.data.local.entity.WeeklySnapshotEntity

@Database(
    entities = [
        QuotaEntity::class,
        LogEntryEntity::class,
        WeeklySnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuotaDatabase : RoomDatabase() {
    abstract fun quotaDao(): QuotaDao
    abstract fun logEntryDao(): LogEntryDao
    abstract fun weeklySnapshotDao(): WeeklySnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: QuotaDatabase? = null

        fun getDatabase(context: Context): QuotaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuotaDatabase::class.java,
                    "quota_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
