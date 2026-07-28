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

object SaturdayCheckInScheduler {
    private const val WORK_NAME = "saturday_checkin_worker"

    fun scheduleSaturdayCheckIn(context: Context) {
        val now = LocalDateTime.now()
        var nextSaturdayTenAM = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            .with(LocalTime.of(10, 0, 0, 0))

        if (now.isAfter(nextSaturdayTenAM)) {
            nextSaturdayTenAM = now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
                .with(LocalTime.of(10, 0, 0, 0))
        }

        val initialDelaySeconds = Duration.between(now, nextSaturdayTenAM).seconds

        val workRequest = PeriodicWorkRequestBuilder<CheckInWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelSaturdayCheckIn(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
