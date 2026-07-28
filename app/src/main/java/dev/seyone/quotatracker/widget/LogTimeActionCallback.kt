package dev.seyone.quotatracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import dev.seyone.quotatracker.QuotaApplication

val QuotaIdKey = ActionParameters.Key<Int>("quota_id_key")
val DurationKey = ActionParameters.Key<Int>("duration_key")

class LogTimeActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val quotaId = parameters[QuotaIdKey] ?: return
        val duration = parameters[DurationKey] ?: 15

        val app = context.applicationContext as QuotaApplication
        app.repository.addLog(quotaId, duration)

        // Refresh Glance widget UI instantly
        QuotaGlanceWidget().updateAll(context)
    }
}
