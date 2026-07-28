package dev.seyone.quotatracker.wear.tile

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.wear.tiles.action.ActionCallback
import dev.seyone.quotatracker.wear.sync.WearDataClientManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WearLogTimeActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId
    ) {
        val manager = WearDataClientManager(context)
        CoroutineScope(Dispatchers.IO).launch {
            val topQuota = manager.quotas.value.firstOrNull { !it.isCompleted } ?: manager.quotas.value.firstOrNull()
            topQuota?.let { quota ->
                manager.sendLogTimeMessage(quota.id, 15)
            }
        }
    }
}
