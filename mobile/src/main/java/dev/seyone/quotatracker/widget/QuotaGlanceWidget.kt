package dev.seyone.quotatracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.size
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.glance.appwidget.cornerRadius
import dev.seyone.quotatracker.QuotaApplication
import dev.seyone.quotatracker.core.data.local.model.QuotaWithProgress
import dev.seyone.quotatracker.core.data.repository.WidgetSettings
import dev.seyone.quotatracker.core.data.repository.WidgetSettingsRepository

class QuotaGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as QuotaApplication
        val repo = app.repository
        val widgetSettingsRepo = WidgetSettingsRepository(context.applicationContext)

        provideContent {
            GlanceTheme {
                val quotasWithProgress by repo.getQuotasWithCurrentWeekProgress().collectAsState(initial = emptyList())
                val widgetSettings by widgetSettingsRepo.widgetSettings.collectAsState(initial = WidgetSettings())

                // Identical 3-tier sorting rule:
                // 1. Unfinished (<100%) at top, Completed (>=100%) at bottom.
                // 2. Pinned items first.
                // 3. Newest created first.
                val sortedQuotas = quotasWithProgress.sortedWith(
                    compareBy<QuotaWithProgress> { it.loggedMinutes >= it.quota.targetMinutes }
                        .thenByDescending { it.quota.isPinned }
                        .thenByDescending { it.quota.createdAt }
                )

                val filteredList = if (widgetSettings.selectedQuotaIds.isEmpty()) {
                    sortedQuotas
                } else {
                    sortedQuotas.filter { it.quota.id in widgetSettings.selectedQuotaIds }
                }

                WidgetContent(filteredList, widgetSettings)
            }
        }
    }

    @Composable
    private fun WidgetContent(list: List<QuotaWithProgress>, settings: WidgetSettings) {
        val baseBgColor = when (settings.themeMode) {
            "DARK" -> Color(0xFF1E1E24)
            "LIGHT" -> Color(0xFFF5F5FA)
            "GLASS" -> Color(0xFFFFFFFF)
            else -> Color(0xFF2C2B30)
        }
        val widgetBgColor = baseBgColor.copy(alpha = settings.opacity)

        val mainTextColor = when (settings.themeMode) {
            "LIGHT" -> androidx.glance.unit.ColorProvider(Color(0xFF1C1B20))
            "DARK", "GLASS" -> androidx.glance.unit.ColorProvider(Color(0xFFFFFFFF))
            else -> GlanceTheme.colors.onSurface
        }

        val subTextColor = when (settings.themeMode) {
            "LIGHT" -> androidx.glance.unit.ColorProvider(Color(0xFF49454F))
            "DARK", "GLASS" -> androidx.glance.unit.ColorProvider(Color(0xFFCAC4D0))
            else -> GlanceTheme.colors.onSurfaceVariant
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .cornerRadius(settings.cornerRadiusDp.dp)
                .background(widgetBgColor),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Header: "WEEKLY GOALS"
            Text(
                text = "WEEKLY GOALS",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = GlanceTheme.colors.primary
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            if (list.isEmpty()) {
                Text(
                    text = "No quotas set up",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = subTextColor
                    )
                )
            } else {
                list.take(3).forEachIndexed { index, item ->
                    if (index > 0) {
                        Spacer(modifier = GlanceModifier.height(10.dp))
                    }

                    val logged = item.loggedMinutes.coerceAtLeast(0)
                    val target = item.quota.targetMinutes.coerceAtLeast(1)
                    val isCompleted = logged >= target
                    val progressFraction = (logged.toFloat() / target.toFloat()).coerceIn(0.0f, 1.0f)

                    val titleColor = if (isCompleted) GlanceTheme.colors.tertiary else mainTextColor

                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.quota.title,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = titleColor
                                        )
                                    )
                                    if (item.quota.isPinned) {
                                        Spacer(modifier = GlanceModifier.width(4.dp))
                                        Image(
                                            provider = ImageProvider(dev.seyone.quotatracker.R.drawable.ic_pin),
                                            contentDescription = "Pinned",
                                            modifier = GlanceModifier.size(12.dp)
                                        )
                                    }
                                }

                                val loggedStr = String.format("%.1fh", logged / 60.0)
                                val targetStr = String.format("%.0fh", target / 60.0)
                                Text(
                                    text = "$loggedStr / $targetStr",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = subTextColor
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            if (settings.showQuickLogButtons && !isCompleted) {
                                Button(
                                    text = "+15m",
                                    onClick = actionRunCallback<LogTimeActionCallback>(
                                        actionParametersOf(
                                            QuotaIdKey to item.quota.id,
                                            DurationKey to 15
                                        )
                                    )
                                )
                            } else if (isCompleted) {
                                Text(
                                    text = "Done ✓",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = GlanceTheme.colors.tertiary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = progressFraction,
                            modifier = GlanceModifier.fillMaxWidth().height(4.dp),
                            color = if (isCompleted) GlanceTheme.colors.tertiary else GlanceTheme.colors.primary,
                            backgroundColor = GlanceTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

class QuotaGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuotaGlanceWidget()
}
