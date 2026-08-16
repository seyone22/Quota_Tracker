package dev.seyone.quotatracker.ui.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.seyone.quotatracker.data.model.QuotaCardStyle
import dev.seyone.quotatracker.ui.components.QuotaIconRegistry
import dev.seyone.quotatracker.ui.dashboard.QuotaUiItem
import dev.seyone.quotatracker.ui.theme.CompletedGreenContainer
import dev.seyone.quotatracker.ui.theme.CompletedGreenProgress
import dev.seyone.quotatracker.ui.theme.CompletedGreenText
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuotaCard(
    item: QuotaUiItem,
    cardStyle: QuotaCardStyle = QuotaCardStyle.DUAL_TONE,
    onQuickLog: (minutesDelta: Int) -> Unit,
    onLongPress: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(item.isCompleted) {
        if (item.isCompleted) {
            scale.animateTo(1.03f, animationSpec = tween(150))
            scale.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    val cardModifier = modifier
        .fillMaxWidth()
        .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
        .combinedClickable(
            onClick = {},
            onLongClick = onLongPress
        )

    when (cardStyle) {
        QuotaCardStyle.DUAL_TONE -> QuotaCard_DualTone(
            item = item,
            onQuickLog = onQuickLog,
            onLongPress = onLongPress,
            onEdit = onEdit,
            onDelete = onDelete,
            modifier = cardModifier
        )
        QuotaCardStyle.SEGMENTED_STEPPER -> QuotaCard_SegmentedStepper(
            item = item,
            onQuickLog = onQuickLog,
            onLongPress = onLongPress,
            onEdit = onEdit,
            onDelete = onDelete,
            modifier = cardModifier
        )
        QuotaCardStyle.GLOW_BANNER -> QuotaCard_GlowBanner(
            item = item,
            onQuickLog = onQuickLog,
            onLongPress = onLongPress,
            onEdit = onEdit,
            onDelete = onDelete,
            modifier = cardModifier
        )
    }
}

@Composable
private fun QuotaCard_DualTone(
    item: QuotaUiItem,
    onQuickLog: (Int) -> Unit,
    onLongPress: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSubtractMode by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val loggedMinutes = item.loggedMinutes
    val targetMinutes = item.targetMinutes
    val isCompleted = item.isCompleted
    val overtimeMinutes = maxOf(0, loggedMinutes - targetMinutes)
    val percentage = if (targetMinutes > 0) (loggedMinutes.toFloat() / targetMinutes.toFloat() * 100).toInt() else 0

    val primaryFraction = (loggedMinutes.toFloat() / targetMinutes.toFloat().coerceAtLeast(1f)).coerceAtMost(1.0f)
    val overtimeFraction = if (loggedMinutes > targetMinutes) {
        ((loggedMinutes - targetMinutes).toFloat() / targetMinutes.toFloat().coerceAtLeast(1f)).coerceAtMost(1.0f)
    } else 0f

    val cardBgColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.surfaceContainerHighest
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "cardBg"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    QuotaIconBadge(
                        iconKey = item.quota.iconKey,
                        isPinned = item.quota.isPinned,
                        isOvertime = overtimeMinutes > 0,
                        sizeDp = 44.dp,
                        iconSizeDp = 22.dp,
                        shape = CircleShape
                    )

                    Column {
                        Text(
                            text = item.quota.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (overtimeMinutes > 0) "Overtime Active" else item.quota.resetStrategy.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (overtimeMinutes > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "+${formatHoursShort(overtimeMinutes)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }

                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Adjust / Manage Logs") },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                                onClick = { showMenu = false; onLongPress() }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = { showMenu = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete() }
                            )
                        }
                    }
                }
            }

            // Stat Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = item.loggedHoursStr,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (item.formattedProgressText.contains("hrs")) " / ${item.targetHoursStr} hrs" else " / ${item.targetHoursStr}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (overtimeMinutes > 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (overtimeMinutes > 0) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Expressive Dual-Tone Progress Meter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = primaryFraction)
                        .background(
                            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                        )
                )

                if (overtimeFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = overtimeFraction)
                            .background(
                                Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.error))
                            )
                    )
                }
            }

            // Mode Toggle Pill & Quick Action Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(modifier = Modifier.padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (!isSubtractMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { isSubtractMode = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Mode", tint = if (!isSubtractMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSubtractMode) MaterialTheme.colorScheme.error else Color.Transparent)
                                .clickable { isSubtractMode = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Subtract Mode", tint = if (isSubtractMode) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                val deltas = if (isSubtractMode) listOf(-15, -30, -60) else listOf(15, 30, 60)
                val labels = if (isSubtractMode) listOf("-15m", "-30m", "-1h") else listOf("+15m", "+30m", "+1h")

                deltas.forEachIndexed { idx, delta ->
                    Surface(
                        onClick = { onQuickLog(delta) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSubtractMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = labels[idx],
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSubtractMode) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaCard_SegmentedStepper(
    item: QuotaUiItem,
    onQuickLog: (Int) -> Unit,
    onLongPress: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val loggedMinutes = item.loggedMinutes
    val targetMinutes = item.targetMinutes
    val overtimeMinutes = maxOf(0, loggedMinutes - targetMinutes)
    val percentage = if (targetMinutes > 0) (loggedMinutes.toFloat() / targetMinutes.toFloat() * 100).toInt() else 0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuotaIconBadge(
                        iconKey = item.quota.iconKey,
                        isPinned = item.quota.isPinned,
                        isOvertime = overtimeMinutes > 0,
                        sizeDp = 40.dp,
                        iconSizeDp = 20.dp,
                        shape = CircleShape
                    )

                    Text(text = item.quota.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (overtimeMinutes > 0) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("🔥 +${formatHoursShort(overtimeMinutes)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Adjust / Manage Logs") }, leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }, onClick = { showMenu = false; onLongPress() })
                            DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, onClick = { showMenu = false; onEdit() })
                            DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; onDelete() })
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.formattedProgressText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text("$percentage%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = if (overtimeMinutes > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
            }

            Row(modifier = Modifier.fillMaxWidth().height(12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val segmentCount = 5
                val currentMax = maxOf(targetMinutes, loggedMinutes).coerceAtLeast(1)
                val targetFraction = targetMinutes.toFloat() / currentMax.toFloat()
                val loggedFraction = loggedMinutes.toFloat() / currentMax.toFloat()

                repeat(segmentCount) { idx ->
                    val segStart = idx.toFloat() / segmentCount.toFloat()
                    val segEnd = (idx + 1).toFloat() / segmentCount.toFloat()
                    val fill = when {
                        loggedFraction >= segEnd -> 1.0f
                        loggedFraction > segStart -> (loggedFraction - segStart) / (segEnd - segStart)
                        else -> 0.0f
                    }
                    val isOvertimeSeg = segStart >= targetFraction

                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
                        if (fill > 0f) {
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = fill).background(if (isOvertimeSeg) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.height(44.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp)) {
                        IconButton(onClick = { onQuickLog(-15) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "-15m", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                        Text("15m", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { onQuickLog(15) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "+15m", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(onClick = { onQuickLog(30) }, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.height(44.dp)) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                            Text("+30m", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Surface(onClick = { onQuickLog(60) }, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.height(44.dp)) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                            Text("+1h", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaCard_GlowBanner(
    item: QuotaUiItem,
    onQuickLog: (Int) -> Unit,
    onLongPress: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDeductMode by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val loggedMinutes = item.loggedMinutes
    val targetMinutes = item.targetMinutes
    val overtimeMinutes = maxOf(0, loggedMinutes - targetMinutes)
    val percentage = if (targetMinutes > 0) (loggedMinutes.toFloat() / targetMinutes.toFloat() * 100).toInt() else 0
    val progressFraction = (loggedMinutes.toFloat() / targetMinutes.toFloat().coerceAtLeast(1f)).coerceAtMost(1.0f)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (overtimeMinutes > 0) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("OVERACHIEVER MODE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                        }
                        Text("+${formatHoursShort(overtimeMinutes)} Extra", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuotaIconBadge(
                        iconKey = item.quota.iconKey,
                        isPinned = item.quota.isPinned,
                        isOvertime = overtimeMinutes > 0,
                        sizeDp = 40.dp,
                        iconSizeDp = 20.dp,
                        shape = CircleShape
                    )
                    Text(item.quota.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$percentage%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = if (overtimeMinutes > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Adjust / Manage Logs") }, leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }, onClick = { showMenu = false; onLongPress() })
                            DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, onClick = { showMenu = false; onEdit() })
                            DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; onDelete() })
                        }
                    }
                }
            }

            Text("${item.formattedProgressText} logged", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = progressFraction).background(Brush.horizontalGradient(if (overtimeMinutes > 0) listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary) else listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isDeductMode = !isDeductMode }, modifier = Modifier.size(40.dp).clip(CircleShape).background(if (!isDeductMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceContainerHighest)) {
                    Icon(imageVector = if (!isDeductMode) Icons.Default.Remove else Icons.Default.Add, contentDescription = null, tint = if (!isDeductMode) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }

                val deltas = if (isDeductMode) listOf(-15, -30, -60) else listOf(15, 30, 60)
                val labels = if (isDeductMode) listOf("-15m", "-30m", "-1h") else listOf("+15m", "+30m", "+1h")

                deltas.forEachIndexed { idx, delta ->
                    Surface(onClick = { onQuickLog(delta) }, shape = RoundedCornerShape(50), color = if (isDeductMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.height(40.dp)) {
                        Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                            Text(text = labels[idx], fontWeight = FontWeight.Bold, color = if (isDeductMode) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

private fun formatHours(minutes: Int): String {
    val hrs = minutes / 60.0
    return if (hrs % 1.0 == 0.0) String.format(Locale.getDefault(), "%.0f", hrs)
    else String.format(Locale.getDefault(), "%.1f", hrs)
}

private fun formatHoursShort(minutes: Int): String {
    val hrs = minutes / 60
    val mins = minutes % 60
    return when {
        hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
        hrs > 0 -> "${hrs}h"
        else -> "${mins}m"
    }
}

@Composable
private fun QuotaIconBadge(
    iconKey: String?,
    isPinned: Boolean,
    isOvertime: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 44.dp,
    iconSizeDp: Dp = 22.dp,
    shape: Shape = CircleShape
) {
    Box(modifier = modifier.size(sizeDp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    if (isOvertime) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            val iconVector = QuotaIconRegistry.getIcon(iconKey) ?: Icons.Default.AutoAwesome
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = if (isOvertime) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(iconSizeDp)
            )
        }

        if (isPinned) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(1.5.dp, MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(9.dp)
                )
            }
        }
    }
}
