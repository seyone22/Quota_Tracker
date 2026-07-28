package dev.seyone.quotatracker.wear.tile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceModifier
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
import androidx.glance.unit.ColorProvider
import androidx.glance.wear.tiles.GlanceTileService
import androidx.glance.wear.tiles.action.actionRunCallback
import dev.seyone.quotatracker.wear.sync.WearDataClientManager
import dev.seyone.quotatracker.wear.sync.WearQuotaItem

class WearQuotaGlanceTileService : GlanceTileService() {

    @Composable
    override fun Content() {
        val manager = WearDataClientManager(this)
        manager.startListening()
        val quotas by manager.quotas.collectAsState()

        val pinnedList = quotas.filter { it.isPinned }
            .ifEmpty { quotas.take(3) }

        TileContent(pinnedList)
    }

    @Composable
    private fun TileContent(list: List<WearQuotaItem>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .background(ColorProvider(Color(0xFF1C1B1F))),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Quotas",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ColorProvider(Color(0xFFE6E1E5))
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            if (list.isEmpty()) {
                Text(
                    text = "No pinned quotas",
                    style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFFCAC4D0)))
                )
            } else {
                list.take(3).forEach { item ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = item.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = ColorProvider(Color(0xFFE6E1E5))
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(4.dp))

                        if (!item.isCompleted) {
                            Button(
                                text = "+15m",
                                onClick = actionRunCallback(WearLogTimeActionCallback::class.java)
                            )
                        } else {
                            Text(
                                text = "✓",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ColorProvider(Color(0xFF4CAF50))
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
