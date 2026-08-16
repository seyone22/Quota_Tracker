package dev.seyone.quotatracker.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.seyone.quotatracker.QuotaApplication
import dev.seyone.quotatracker.core.data.local.entity.WeeklySnapshotEntity
import dev.seyone.quotatracker.core.domain.model.ResetStrategy
import dev.seyone.quotatracker.core.domain.util.WeekUtils
import kotlinx.coroutines.flow.firstOrNull

class WeeklyResetWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as QuotaApplication
            val db = app.database
            val repository = app.repository

            val now = System.currentTimeMillis()
            val (currentWeekStart, _) = WeekUtils.getCurrentWeekRange(now)
            val lastWeekTimestamp = currentWeekStart - 1000
            val (lastWeekStart, lastWeekEnd) = WeekUtils.getCurrentWeekRange(lastWeekTimestamp)
            val lastWeekString = WeekUtils.getWeekString(lastWeekTimestamp)

            Log.d("WeeklyResetWorker", "Executing weekly reset for week: $lastWeekString")

            val quotas = db.quotaDao().getAllQuotas().firstOrNull() ?: emptyList()

            quotas.forEach { quota ->
                val progressList = db.quotaDao().getQuotasWithCurrentWeekProgress(lastWeekStart, lastWeekEnd).firstOrNull() ?: emptyList()
                val snapshot = progressList.firstOrNull { it.quota.id == quota.id }
                val loggedMinutes = (snapshot?.loggedMinutes ?: 0).coerceAtLeast(0)
                val targetMinutes = quota.targetMinutes

                val snapshotEntity = WeeklySnapshotEntity(
                    quotaId = quota.id,
                    weekString = lastWeekString,
                    targetMinutesSnapshot = targetMinutes,
                    loggedMinutesSnapshot = loggedMinutes
                )
                db.weeklySnapshotDao().insertSnapshot(snapshotEntity)

                when (quota.resetStrategy) {
                    ResetStrategy.CLEAN -> {
                        // Clean reset: starts 0 for new week
                    }
                    ResetStrategy.ROLLOVER -> {
                        if (loggedMinutes < targetMinutes) {
                            val deficitMinutes = targetMinutes - loggedMinutes
                            repository.addLog(
                                quotaId = quota.id,
                                durationMinutes = -deficitMinutes,
                                timestamp = currentWeekStart
                            )
                        }
                    }
                    ResetStrategy.BANK -> {
                        if (loggedMinutes > targetMinutes) {
                            val surplusMinutes = loggedMinutes - targetMinutes
                            repository.addLog(
                                quotaId = quota.id,
                                durationMinutes = surplusMinutes,
                                timestamp = currentWeekStart
                            )
                        }
                    }
                }
            }

            Log.d("WeeklyResetWorker", "Completed reset for ${quotas.size} quotas.")
            Result.success()
        } catch (e: Exception) {
            Log.e("WeeklyResetWorker", "Failed weekly reset worker", e)
            Result.retry()
        }
    }
}
