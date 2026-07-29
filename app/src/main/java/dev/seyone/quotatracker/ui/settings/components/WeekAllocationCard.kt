package dev.seyone.quotatracker.ui.settings.components

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

data class WeekAllocationData(
    val sleepHours: Int,
    val workHours: Int,
    val maintenanceHours: Int,
    val quotaTargetHours: Int,
    val unfilledHours: Int
)

@Composable
fun WeekAllocationCard(
    modifier: Modifier = Modifier,
    allocationData: WeekAllocationData
) {
    val sleepColor = Color(0xFF3F51B5)       // Sleep Blue
    val workColor = Color(0xFFFF9800)        // Work Amber
    val maintenanceColor = Color(0xFF8E24AA) // Maintenance Purple
    val activityColor = Color(0xFF24A16F)    // My Activity Emerald Green
    val unfilledColor = Color(0xFF4A4A4A)    // Unfilled Muted Surface

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "168-Hour Week Allocation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "168h total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stacked Bar Container: continuous 32.dp height, clipped to RoundedCornerShape(percent = 50)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(percent = 50))
            ) {
                if (allocationData.sleepHours > 0) {
                    val sleepRatio = allocationData.sleepHours / 168f
                    Box(
                        modifier = Modifier
                            .weight(allocationData.sleepHours.toFloat())
                            .height(32.dp)
                            .background(sleepColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (sleepRatio >= 0.08f) {
                            Text(
                                text = "${allocationData.sleepHours}h",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (allocationData.workHours > 0) {
                    val workRatio = allocationData.workHours / 168f
                    Box(
                        modifier = Modifier
                            .weight(allocationData.workHours.toFloat())
                            .height(32.dp)
                            .background(workColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (workRatio >= 0.08f) {
                            Text(
                                text = "${allocationData.workHours}h",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (allocationData.maintenanceHours > 0) {
                    val maintRatio = allocationData.maintenanceHours / 168f
                    Box(
                        modifier = Modifier
                            .weight(allocationData.maintenanceHours.toFloat())
                            .height(32.dp)
                            .background(maintenanceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (maintRatio >= 0.08f) {
                            Text(
                                text = "${allocationData.maintenanceHours}h",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (allocationData.quotaTargetHours > 0) {
                    val activityRatio = allocationData.quotaTargetHours / 168f
                    Box(
                        modifier = Modifier
                            .weight(allocationData.quotaTargetHours.toFloat())
                            .height(32.dp)
                            .background(activityColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activityRatio >= 0.08f) {
                            Text(
                                text = "${allocationData.quotaTargetHours}h",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (allocationData.unfilledHours > 0) {
                    val unfilledRatio = allocationData.unfilledHours / 168f
                    Box(
                        modifier = Modifier
                            .weight(allocationData.unfilledHours.toFloat())
                            .height(32.dp)
                            .background(unfilledColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (unfilledRatio >= 0.12f) {
                            Text(
                                text = "${allocationData.unfilledHours}h unfilled",
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

            // Legend Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = sleepColor, label = "Sleep (${allocationData.sleepHours}h)")
                    LegendDot(color = workColor, label = "Work (${allocationData.workHours}h)")
                    LegendDot(color = maintenanceColor, label = "Maintenance (${allocationData.maintenanceHours}h)")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = activityColor, label = "My Activity (${allocationData.quotaTargetHours}h)")
                    LegendDot(color = unfilledColor, label = "Unfilled (${allocationData.unfilledHours}h)")
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
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
fun WeekAllocationCardPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            WeekAllocationCard(
                allocationData = WeekAllocationData(
                    sleepHours = 56,
                    workHours = 40,
                    maintenanceHours = 14,
                    quotaTargetHours = 26,
                    unfilledHours = 32
                )
            )
        }
    }
}
