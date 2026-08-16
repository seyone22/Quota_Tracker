package dev.seyone.quotatracker.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class WeekPulseData(
    val hoursDone: Float,
    val hoursGap: Float,
    val hoursLeft: Float,
    val bucketsDone: Int,
    val bucketsOnTrack: Int,
    val bucketsBehind: Int,
    val bucketsNotStarted: Int,
    val currentWeek: String,
    val dayOfWeek: Int
)

@Composable
fun WeekPulseCard(
    modifier: Modifier = Modifier,
    pulseData: WeekPulseData
) {
    // Cohesive Material 3 Palette
    val darkGreenColor = Color(0xFF1B6B46)
    val greenDoneColor = Color(0xFF24A16F)
    val lightGreenOnTrackColor = Color(0xFF4EBA88)
    val amberBehindColor = Color(0xFFD49A5B)
    val darkGrayLeftColor = Color(0xFF4A4A4A)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Week Pulse",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${pulseData.currentWeek} · Day ${pulseData.dayOfWeek} of 7",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- Primary Bar (Hours Pace) ---
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val gapHoursInt = pulseData.hoursGap.roundToInt()
                val headlineText = if (gapHoursInt > 0) {
                    "${gapHoursInt}h Behind Pace"
                } else {
                    "On Pace"
                }
                val headlineColor = if (gapHoursInt > 0) amberBehindColor else greenDoneColor

                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = headlineColor
                )

                // Primary Bar Stack: Continuous rounded container, uniform 28.dp height
                val totalHours = maxOf(0.1f, pulseData.hoursDone + pulseData.hoursGap + pulseData.hoursLeft)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(percent = 50))
                ) {
                    if (pulseData.hoursDone > 0f) {
                        val doneRatio = pulseData.hoursDone / totalHours
                        Box(
                            modifier = Modifier
                                .weight(pulseData.hoursDone)
                                .height(28.dp)
                                .background(greenDoneColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (doneRatio >= 0.12f) {
                                Text(
                                    text = "${pulseData.hoursDone.roundToInt()}h done",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (pulseData.hoursGap > 0f) {
                        val gapRatio = pulseData.hoursGap / totalHours
                        Box(
                            modifier = Modifier
                                .weight(pulseData.hoursGap)
                                .height(28.dp)
                                .background(amberBehindColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (gapRatio >= 0.10f) {
                                Text(
                                    text = "${pulseData.hoursGap.roundToInt()}h gap",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (pulseData.hoursLeft > 0f) {
                        val leftRatio = pulseData.hoursLeft / totalHours
                        Box(
                            modifier = Modifier
                                .weight(pulseData.hoursLeft)
                                .height(28.dp)
                                .background(darkGrayLeftColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (leftRatio >= 0.12f) {
                                Text(
                                    text = "${pulseData.hoursLeft.roundToInt()}h left",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // --- Secondary Bar (Status Counts) ---
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val attentionCount = pulseData.bucketsBehind + pulseData.bucketsNotStarted
                val attentionText = if (attentionCount > 0) {
                    "$attentionCount Buckets Need Attention"
                } else {
                    "All Buckets On Track"
                }
                Text(
                    text = attentionText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Secondary Bar Stack: Continuous rounded container, uniform 28.dp height
                val totalBuckets = maxOf(1, pulseData.bucketsDone + pulseData.bucketsOnTrack + pulseData.bucketsBehind + pulseData.bucketsNotStarted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(percent = 50))
                ) {
                    if (pulseData.bucketsDone > 0) {
                        val doneRatio = pulseData.bucketsDone.toFloat() / totalBuckets
                        Box(
                            modifier = Modifier
                                .weight(pulseData.bucketsDone.toFloat())
                                .height(28.dp)
                                .background(darkGreenColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (doneRatio >= 0.05f) {
                                Text(
                                    text = "${pulseData.bucketsDone}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (pulseData.bucketsOnTrack > 0) {
                        val onTrackRatio = pulseData.bucketsOnTrack.toFloat() / totalBuckets
                        Box(
                            modifier = Modifier
                                .weight(pulseData.bucketsOnTrack.toFloat())
                                .height(28.dp)
                                .background(lightGreenOnTrackColor),
                            contentAlignment = Alignment.Center
                        ) {
                            val labelText = if (onTrackRatio >= 0.28f) "${pulseData.bucketsOnTrack} on track" else "${pulseData.bucketsOnTrack}"
                            if (onTrackRatio >= 0.05f) {
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (pulseData.bucketsBehind > 0) {
                        val behindRatio = pulseData.bucketsBehind.toFloat() / totalBuckets
                        Box(
                            modifier = Modifier
                                .weight(pulseData.bucketsBehind.toFloat())
                                .height(28.dp)
                                .background(amberBehindColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (behindRatio >= 0.05f) {
                                Text(
                                    text = "${pulseData.bucketsBehind}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (pulseData.bucketsNotStarted > 0) {
                        val notStartedRatio = pulseData.bucketsNotStarted.toFloat() / totalBuckets
                        Box(
                            modifier = Modifier
                                .weight(pulseData.bucketsNotStarted.toFloat())
                                .height(28.dp)
                                .background(darkGrayLeftColor),
                            contentAlignment = Alignment.Center
                        ) {
                            val labelText = if (notStartedRatio >= 0.28f) "${pulseData.bucketsNotStarted} not started" else "${pulseData.bucketsNotStarted}"
                            if (notStartedRatio >= 0.05f) {
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = darkGreenColor, label = "Done")
                    LegendItem(color = lightGreenOnTrackColor, label = "On track")
                    LegendItem(color = amberBehindColor, label = "Behind")
                    LegendItem(color = darkGrayLeftColor, label = "Not started")
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeekPulseCardPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            WeekPulseCard(
                pulseData = WeekPulseData(
                    hoursDone = 26f,
                    hoursGap = 7f,
                    hoursLeft = 25f,
                    bucketsDone = 1,
                    bucketsOnTrack = 7,
                    bucketsBehind = 1,
                    bucketsNotStarted = 5,
                    currentWeek = "2026-W31",
                    dayOfWeek = 4
                )
            )
        }
    }
}
