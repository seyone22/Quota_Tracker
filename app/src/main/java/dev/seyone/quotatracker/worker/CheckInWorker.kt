package dev.seyone.quotatracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.seyone.quotatracker.R
import dev.seyone.quotatracker.data.local.QuotaDatabase
import dev.seyone.quotatracker.data.repository.QuotaRepository
import kotlinx.coroutines.flow.firstOrNull

class CheckInWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "quota_weekend_checkin"
        const val NOTIFICATION_ID = 2001
    }

    override suspend fun doWork(): Result {
        val database = QuotaDatabase.getDatabase(applicationContext)
        val repository = QuotaRepository(
            quotaDao = database.quotaDao(),
            logEntryDao = database.logEntryDao()
        )

        val activeQuotas = repository.getQuotasWithCurrentWeekProgress().firstOrNull() ?: emptyList()
        val pinnedUnderperforming = activeQuotas.filter { item ->
            item.quota.isPinned && item.loggedMinutes < (item.quota.targetMinutes * 0.5)
        }

        if (pinnedUnderperforming.isNotEmpty()) {
            val targetItem = pinnedUnderperforming.first()
            val remainingMinutes = (targetItem.quota.targetMinutes - targetItem.loggedMinutes).coerceAtLeast(0)
            val remainingHours = remainingMinutes / 60.0

            sendNotification(
                title = "Weekend Check-in",
                body = "${targetItem.quota.title} needs ${String.format("%.1f", remainingHours)} more hours to hit your target."
            )
        }

        return Result.success()
    }

    private fun sendNotification(title: String, body: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Weekend Check-in",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent Saturday check-in reminder for underperforming quotas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
