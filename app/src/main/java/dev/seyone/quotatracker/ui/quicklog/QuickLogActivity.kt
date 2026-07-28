package dev.seyone.quotatracker.ui.quicklog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.seyone.quotatracker.QuotaApplication
import dev.seyone.quotatracker.ui.theme.QuotaTrackerTheme
import kotlinx.coroutines.launch

class QuickLogActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = applicationContext as QuotaApplication
        val repo = app.repository

        setContent {
            QuotaTrackerTheme {
                val quotas by repo.getQuotasWithCurrentWeekProgress().collectAsState(initial = emptyList())
                val topQuota = quotas.firstOrNull { it.quota.isPinned } ?: quotas.firstOrNull()
                val scope = rememberCoroutineScope()
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                ModalBottomSheet(
                    onDismissRequest = { finish() },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Quick Log Time",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (topQuota != null) {
                            val loggedHrs = String.format("%.1f", topQuota.loggedMinutes / 60.0)
                            val targetHrs = String.format("%.0f", topQuota.quota.targetMinutes / 60.0)
                            Text(
                                text = "Target: ${topQuota.quota.title} ($loggedHrs / $targetHrs hrs)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            repo.addLog(topQuota.quota.id, 15)
                                            Toast.makeText(this@QuickLogActivity, "Logged +15m to ${topQuota.quota.title}", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+15m")
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            repo.addLog(topQuota.quota.id, 30)
                                            Toast.makeText(this@QuickLogActivity, "Logged +30m to ${topQuota.quota.title}", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+30m")
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            repo.addLog(topQuota.quota.id, 60)
                                            Toast.makeText(this@QuickLogActivity, "Logged +1h to ${topQuota.quota.title}", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+1h")
                                }
                            }
                        } else {
                            Text(
                                text = "No active quotas found. Please add a quota target in the app.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { finish() }) {
                                Text("Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
