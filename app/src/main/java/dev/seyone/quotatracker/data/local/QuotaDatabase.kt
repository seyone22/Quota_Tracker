package dev.seyone.quotatracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
    exportSchema = false
)
abstract class QuotaDatabase : RoomDatabase() {
    abstract fun quotaDao(): QuotaDao
    abstract fun logEntryDao(): LogEntryDao
    abstract fun weeklySnapshotDao(): WeeklySnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: QuotaDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quotas ADD COLUMN iconKey TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quotas ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): QuotaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuotaDatabase::class.java,
                    "quota_tracker_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
