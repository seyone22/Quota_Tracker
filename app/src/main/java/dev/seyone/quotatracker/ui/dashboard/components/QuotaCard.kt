package dev.seyone.quotatracker.ui.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.seyone.quotatracker.ui.dashboard.QuotaUiItem
import dev.seyone.quotatracker.ui.theme.CompletedGreenContainer
import dev.seyone.quotatracker.ui.theme.CompletedGreenProgress
import dev.seyone.quotatracker.ui.theme.CompletedGreenText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuotaCard(
    item: QuotaUiItem,
    onQuickLog: (Int) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = item.isCompleted

    val scale = remember { Animatable(1f) }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            scale.animateTo(1.03f, animationSpec = tween(150))
            scale.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    val containerColor by animateColorAsState(
        targetValue = if (isCompleted) CompletedGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "containerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isCompleted) CompletedGreenText else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "contentColor"
    )

    val progressColor by animateColorAsState(
        targetValue = if (isCompleted) CompletedGreenProgress else MaterialTheme.colorScheme.primary,
        label = "progressColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Title, Reset Strategy badge, Pin Icon, Checkmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.quota.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    if (item.quota.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.quota.resetStrategy.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = CompletedGreenProgress,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress text and Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.formattedProgressText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                val pct = (item.progressFraction * 100).toInt()
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { item.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = contentColor.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions Row: +15m, +30m, +1h (Disabled when completed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val chipEnabled = !isCompleted

                SuggestionChip(
                    onClick = { if (chipEnabled) onQuickLog(15) },
                    enabled = chipEnabled,
                    label = { Text("+15m") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        disabledContainerColor = Color.Transparent,
                        disabledLabelColor = contentColor.copy(alpha = 0.4f)
                    )
                )

                SuggestionChip(
                    onClick = { if (chipEnabled) onQuickLog(30) },
                    enabled = chipEnabled,
                    label = { Text("+30m") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        disabledContainerColor = Color.Transparent,
                        disabledLabelColor = contentColor.copy(alpha = 0.4f)
                    )
                )

                SuggestionChip(
                    onClick = { if (chipEnabled) onQuickLog(60) },
                    enabled = chipEnabled,
                    label = { Text("+1h") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        disabledContainerColor = Color.Transparent,
                        disabledLabelColor = contentColor.copy(alpha = 0.4f)
                    )
                )

                if (isCompleted) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Long-press to override",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
