package dev.seyone.quotatracker.sync

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dev.seyone.quotatracker.QuotaApplication
import dev.seyone.quotatracker.core.data.sync.LogTimeMessagePayload
import dev.seyone.quotatracker.core.data.sync.WearSyncJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PhoneWearableListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        if (messageEvent.path == "/log_time") {
            try {
                val jsonString = String(messageEvent.data, Charsets.UTF_8)
                val payload = WearSyncJson.fromJson(jsonString, LogTimeMessagePayload::class.java)

                Log.d("PhoneWearListener", "Received /log_time for quotaId=${payload.quotaId}, duration=${payload.durationMinutes}m")

                val app = applicationContext as QuotaApplication
                serviceScope.launch {
                    if (payload.durationMinutes < 0) {
                        app.repository.subtractLog(
                            quotaId = payload.quotaId,
                            durationMinutes = -payload.durationMinutes
                        )
                    } else {
                        app.repository.addLog(
                            quotaId = payload.quotaId,
                            durationMinutes = payload.durationMinutes
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PhoneWearListener", "Failed to process /log_time message", e)
            }
        }
    }
}
