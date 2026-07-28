package dev.seyone.quotatracker.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object WeeklyResetScheduler {

    private const val WORK_NAME = "WeeklyResetWorkerTask"

    fun scheduleWeeklyReset(context: Context) {
        val now = LocalDateTime.now()
        var nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).with(LocalTime.MIN)
        
        if (now.dayOfWeek == DayOfWeek.MONDAY && now.toLocalTime() == LocalTime.MIN) {
            nextMonday = now
        }

        val initialDelayMinutes = Duration.between(now, nextMonday).toMinutes().coerceAtLeast(0)

        val resetWorkRequest = PeriodicWorkRequestBuilder<WeeklyResetWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            resetWorkRequest
        )
    }
}
