package dev.seyone.quotatracker.wear.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import dev.seyone.quotatracker.wear.sync.WearDataClientManager
import dev.seyone.quotatracker.wear.ui.WearMainActivity

class QuotaComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 2.5f,
                    min = 0f,
                    max = 7f,
                    contentDescription = PlainComplicationText.Builder("Quota Progress").build()
                )
                    .setText(PlainComplicationText.Builder("2.5h").build())
                    .setTitle(PlainComplicationText.Builder("Quota").build())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("2.5h").build(),
                    contentDescription = PlainComplicationText.Builder("Quota Progress").build()
                )
                    .setTitle(PlainComplicationText.Builder("Quota").build())
                    .build()
            }
            else -> null
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val manager = WearDataClientManager(applicationContext)
        val stateList = manager.fetchQuotasDirectly()
        val topQuota = stateList.firstOrNull { it.isPinned } ?: stateList.firstOrNull()

        val intent = Intent(applicationContext, WearMainActivity::class.java)
        val tapAction = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val loggedHours = (topQuota?.loggedMinutes ?: 0) / 60f
        val targetHours = (topQuota?.targetMinutes ?: 60) / 60f
        val title = topQuota?.title ?: "Quota"
        val textValue = String.format("%.1fh", loggedHours)

        return when (request.complicationType) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = loggedHours,
                    min = 0f,
                    max = targetHours.coerceAtLeast(0.1f),
                    contentDescription = PlainComplicationText.Builder(title).build()
                )
                    .setText(PlainComplicationText.Builder(textValue).build())
                    .setTitle(PlainComplicationText.Builder(title).build())
                    .setTapAction(tapAction)
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(textValue).build(),
                    contentDescription = PlainComplicationText.Builder(title).build()
                )
                    .setTitle(PlainComplicationText.Builder(title).build())
                    .setTapAction(tapAction)
                    .build()
            }
            else -> null
        }
    }
}
