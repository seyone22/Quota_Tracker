package dev.seyone.quotatracker.wear.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.seyone.quotatracker.wear.sync.WearDataClientManager
import dev.seyone.quotatracker.wear.sync.WearQuotaItem
import kotlinx.coroutines.launch

class WearMainActivity : ComponentActivity() {

    private lateinit var dataClientManager: WearDataClientManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataClientManager = WearDataClientManager(this)

        setContent {
            MaterialTheme {
                WearApp(dataClientManager)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataClientManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        dataClientManager.stopListening()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun WearApp(dataClientManager: WearDataClientManager) {
    val quotas by dataClientManager.quotas.collectAsState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val sortedQuotas = remember(quotas) {
        quotas.sortedWith(
            compareBy<WearQuotaItem> { it.isCompleted }
                .thenByDescending { it.isPinned }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (sortedQuotas.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Quota Tracker",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Syncing with Phone...",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
            }
        } else {
            ScalingLazyColumn(
                columnState = rememberResponsiveColumnState(),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedQuotas.size) { index ->
                    val item = sortedQuotas[index]
                    val isCompleted = item.isCompleted

                    val cardBgColor = if (isCompleted) Color(0xFF1E3829) else MaterialTheme.colorScheme.background

                    Card(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        dataClientManager.sendLogTimeMessage(item.id, -15)
                                        Toast.makeText(context, "Removed -15m (${item.title})", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) Color(0xFFA3F3C2) else MaterialTheme.colorScheme.onSurface
                                )
                                val loggedHrs = String.format("%.1f", item.loggedMinutes / 60.0)
                                val targetHrs = String.format("%.0f", item.targetMinutes / 60.0)
                                Text(
                                    text = "$loggedHrs / ${targetHrs}h",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = if (isCompleted) Color(0xFFA3F3C2).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            if (!isCompleted) {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        scope.launch {
                                            val success = dataClientManager.sendLogTimeMessage(item.id, 15)
                                            if (success) {
                                                Toast.makeText(context, "+15m Logged!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("+15", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Text(
                                    text = "Done ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
