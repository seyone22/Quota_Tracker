package dev.seyone.quotatracker.sync

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dev.seyone.quotatracker.data.repository.QuotaRepository
import dev.seyone.quotatracker.util.awaitTask
import dev.seyone.quotatracker.widget.QuotaGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PhoneWearSyncBroadcaster(
    private val context: Context,
    private val repository: QuotaRepository
) {
    private val dataClient by lazy { Wearable.getDataClient(context) }
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun startSync() {
        scope.launch {
            repository.getQuotasWithCurrentWeekProgress().collectLatest { list ->
                // 1. Broadcast to Wear OS DataClient
                try {
                    val wearItems = list.map { model ->
                        val target = model.quota.targetMinutes.coerceAtLeast(1)
                        val logged = model.loggedMinutes.coerceAtLeast(0)
                        WearQuotaItem(
                            id = model.quota.id,
                            title = model.quota.title,
                            targetMinutes = target,
                            loggedMinutes = logged,
                            isCompleted = logged >= target,
                            isPinned = model.quota.isPinned
                        )
                    }

                    val payload = WearQuotaStatePayload(
                        timestamp = System.currentTimeMillis(),
                        quotas = wearItems
                    )

                    val request = PutDataMapRequest.create("/quotas_state").apply {
                        dataMap.putString("payload", WearSyncJson.toJson(payload))
                        dataMap.putLong("timestamp", System.currentTimeMillis())
                    }.asPutDataRequest().setUrgent()

                    dataClient.putDataItem(request).awaitTask()
                    Log.d("PhoneWearSync", "Broadcasted ${wearItems.size} quotas to Wearable DataClient")
                } catch (e: Exception) {
                    Log.e("PhoneWearSync", "Failed to broadcast quotas", e)
                }

                // 2. Real-time refresh for Phone Home Screen Glance Widget
                try {
                    QuotaGlanceWidget().updateAll(context)
                } catch (e: Exception) {
                    Log.d("PhoneWearSync", "Glance widget update skipped (no active widget)")
                }
            }
        }
    }
}
