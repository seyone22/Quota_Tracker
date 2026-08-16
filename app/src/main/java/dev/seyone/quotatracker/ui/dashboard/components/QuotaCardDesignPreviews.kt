package dev.seyone.quotatracker.ui.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.seyone.quotatracker.ui.theme.CompletedGreenContainer
import dev.seyone.quotatracker.ui.theme.CompletedGreenProgress
import dev.seyone.quotatracker.ui.theme.CompletedGreenText
import java.util.Locale

// ============================================================================
// MATERIAL 3 EXPRESSIVE OPTION 1: "Dual-Tone Meter & Mode Segment Pill"
// ============================================================================
@Composable
fun M3ExpressiveCard_Option1(
    title: String = "Reading & Research",
    targetMinutes: Int = 270, // 4.5 hrs
    initialLoggedMinutes: Int = 345, // 5.75 hrs (+1.25h overtime)
    modifier: Modifier = Modifier
) {
    var loggedMinutes by remember { mutableIntStateOf(initialLoggedMinutes) }
    var isSubtractMode by remember { mutableStateOf(false) }

    val isCompleted = loggedMinutes >= targetMinutes
    val overtimeMinutes = maxOf(0, loggedMinutes - targetMinutes)
    val percentage = (loggedMinutes.toFloat() / targetMinutes.toFloat() * 100).toInt()

    // Smooth animated progress fraction
    val progressFraction by animateFloatAsState(
        targetValue = (loggedMinutes.toFloat() / targetMinutes.toFloat()).coerceAtMost(1.0f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "progressFraction"
    )

    val overtimeFraction by animateFloatAsState(
        targetValue = if (loggedMinutes > targetMinutes) {
            ((loggedMinutes - targetMinutes).toFloat() / targetMinutes.toFloat()).coerceAtMost(1.0f)
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "overtimeFraction"
    )

    val cardBgColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.surfaceContainerHighest
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "cardBg"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
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
            // Header Row: Icon Container + Title + Overtime Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (overtimeMinutes > 0) MaterialTheme.colorScheme.tertiaryContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = if (overtimeMinutes > 0) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (overtimeMinutes > 0) "Overtime Active" else "Weekly Quota",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

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
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "+${formatHoursShort(overtimeMinutes)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Stat & Percentage Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatHours(loggedMinutes),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " / ${formatHours(targetMinutes)} hrs",
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

            // Expressive Dual-Tone Progress Meter (14dp thickness)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                // Main Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progressFraction)
                        .background(
                            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                        )
                )

                // Layered Overfill Bar
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

            // Mode Toggle Pill & Quick Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // M3 Expressive Segmented Mode Switcher
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (!isSubtractMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { isSubtractMode = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Mode",
                                tint = if (!isSubtractMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSubtractMode) MaterialTheme.colorScheme.error else Color.Transparent)
                                .clickable { isSubtractMode = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Subtract Mode",
                                tint = if (isSubtractMode) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Quick Action Chips
                val deltas = if (isSubtractMode) listOf(-15, -30, -60) else listOf(15, 30, 60)
                val labels = if (isSubtractMode) listOf("-15m", "-30m", "-1h") else listOf("+15m", "+30m", "+1h")

                deltas.forEachIndexed { idx, delta ->
                    Surface(
                        onClick = { loggedMinutes = (loggedMinutes + delta).coerceAtLeast(0) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSubtractMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
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


// ============================================================================
// MATERIAL 3 EXPRESSIVE OPTION 2: "Segmented Meter & Stepper Dial"
// ============================================================================
@Composable
fun M3ExpressiveCard_Option2(
    title: String = "Gym & Athletic Conditioning",
    targetMinutes: Int = 180, // 3.0 hrs
    initialLoggedMinutes: Int = 225, // 3.75 hrs (+45m overtime)
    modifier: Modifier = Modifier
) {
    var loggedMinutes by remember { mutableIntStateOf(initialLoggedMinutes) }

    val isCompleted = loggedMinutes >= targetMinutes
    val overtimeMinutes = maxOf(0, loggedMinutes - targetMinutes)
    val percentage = (loggedMinutes.toFloat() / targetMinutes.toFloat() * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (overtimeMinutes > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text(
                                text = "🔥 +${formatHoursShort(overtimeMinutes)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Big Stat Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatHours(loggedMinutes)} / ${formatHours(targetMinutes)} hrs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = if (overtimeMinutes > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
            }

            // Segmented Step Blocks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        if (fill > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = fill)
                                    .background(
                                        if (isOvertimeSeg) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                        }
                    }
                }
            }

            // Stepper Dial Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        IconButton(
                            onClick = { loggedMinutes = (loggedMinutes - 15).coerceAtLeast(0) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-15m", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }

                        Text(
                            text = "15m",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { loggedMinutes += 15 },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+15m", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Quick Add Chips (+30m, +1h)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { loggedMinutes += 30 },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                            Text("+30m", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    Surface(
                        onClick = { loggedMinutes += 60 },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                            Text("+1h", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
        }
    }
}


// ============================================================================
// MATERIAL 3 EXPRESSIVE OPTION 3: "Glow Card & Overachiever Banner"
// ============================================================================
@Composable
fun M3ExpressiveCard_Option3(
    title: String = "Guitar & Music Production",
    targetMinutes: Int = 120, // 2.0 hrs
    initialLoggedMinutes: Int = 210, // 3.5 hrs (+1.5h overtime)
    modifier: Modifier = Modifier
) {
    var loggedMinutes by remember { mutableIntStateOf(initialLoggedMinutes) }
    var isDeductMode by remember { mutableStateOf(false) }

    val isCompleted = loggedMinutes >= targetMinutes
    val overtimeMinutes = maxOf(0, loggedMinutes - targetMinutes)
    val percentage = (loggedMinutes.toFloat() / targetMinutes.toFloat() * 100).toInt()
    val progressFraction = (loggedMinutes.toFloat() / targetMinutes.toFloat()).coerceAtMost(1.0f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Overachiever Banner (When overtime is active)
            if (overtimeMinutes > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = "OVERACHIEVER MODE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "+${formatHoursShort(overtimeMinutes)} Extra",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (overtimeMinutes > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
            }

            // Stats
            Text(
                text = "${formatHours(loggedMinutes)} / ${formatHours(targetMinutes)} hrs logged",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Neon Glowing Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progressFraction)
                        .background(
                            Brush.horizontalGradient(
                                if (overtimeMinutes > 0) listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                else listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        )
                )
            }

            // Switchable Adjuster Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isDeductMode = !isDeductMode },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDeductMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Icon(
                        imageVector = if (isDeductMode) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = "Toggle mode",
                        tint = if (isDeductMode) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                val deltas = if (isDeductMode) listOf(-15, -30, -60) else listOf(15, 30, 60)
                val labels = if (isDeductMode) listOf("-15m", "-30m", "-1h") else listOf("+15m", "+30m", "+1h")

                deltas.forEachIndexed { idx, delta ->
                    Surface(
                        onClick = { loggedMinutes = (loggedMinutes + delta).coerceAtLeast(0) },
                        shape = RoundedCornerShape(50),
                        color = if (isDeductMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = labels[idx],
                                fontWeight = FontWeight.Bold,
                                color = if (isDeductMode) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                            )
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

// ============================================================================
// PREVIEWS
// ============================================================================
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun Preview_M3Expressive_Option1() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            M3ExpressiveCard_Option1()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun Preview_M3Expressive_Option2() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            M3ExpressiveCard_Option2()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun Preview_M3Expressive_Option3() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            M3ExpressiveCard_Option3()
        }
    }
}
