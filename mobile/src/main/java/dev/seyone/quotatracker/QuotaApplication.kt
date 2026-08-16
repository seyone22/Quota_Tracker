package dev.seyone.quotatracker

import android.app.Application
import dev.seyone.quotatracker.core.data.local.QuotaDatabase
import dev.seyone.quotatracker.core.data.repository.QuotaRepository
import dev.seyone.quotatracker.core.data.repository.SettingsRepository
import dev.seyone.quotatracker.sync.PhoneWearSyncBroadcaster
import dev.seyone.quotatracker.worker.SaturdayCheckInScheduler
import dev.seyone.quotatracker.worker.WeeklyResetScheduler

class QuotaApplication : Application() {
    val database by lazy { QuotaDatabase.getDatabase(this) }
    val repository by lazy {
        QuotaRepository(
            quotaDao = database.quotaDao(),
            logEntryDao = database.logEntryDao()
        )
    }
    val settingsRepository by lazy {
        SettingsRepository(this)
    }

    val wearSyncBroadcaster by lazy {
        PhoneWearSyncBroadcaster(this, repository)
    }

    override fun onCreate() {
        super.onCreate()
        wearSyncBroadcaster.startSync()
        WeeklyResetScheduler.scheduleWeeklyReset(this)
        SaturdayCheckInScheduler.scheduleSaturdayCheckIn(this)
    }
}
