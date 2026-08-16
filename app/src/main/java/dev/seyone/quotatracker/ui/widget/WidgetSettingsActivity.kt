package dev.seyone.quotatracker.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import dev.seyone.quotatracker.QuotaApplication
import dev.seyone.quotatracker.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.data.repository.WidgetSettings
import dev.seyone.quotatracker.data.repository.WidgetSettingsRepository
import dev.seyone.quotatracker.ui.theme.QuotaTrackerTheme
import dev.seyone.quotatracker.widget.QuotaGlanceWidget
import kotlinx.coroutines.launch

class WidgetSettingsActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result CANCELED by default so if user backs out, widget creation is canceled
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val app = applicationContext as QuotaApplication
        val repository = app.repository
        val widgetSettingsRepository = WidgetSettingsRepository(applicationContext)

        setContent {
            QuotaTrackerTheme {
                WidgetSettingsScreen(
                    repository = repository,
                    widgetSettingsRepository = widgetSettingsRepository,
                    onSave = { settings ->
                        lifecycleScope.launch {
                            widgetSettingsRepository.updateWidgetSettings(settings)
                            
                            // Trigger Glance widget update across all active instances
                            val glanceManager = GlanceAppWidgetManager(applicationContext)
                            val glanceIds = glanceManager.getGlanceIds(QuotaGlanceWidget::class.java)
                            for (glanceId in glanceIds) {
                                QuotaGlanceWidget().update(applicationContext, glanceId)
                            }

                            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        }
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsScreen(
    repository: dev.seyone.quotatracker.data.repository.QuotaRepository,
    widgetSettingsRepository: WidgetSettingsRepository,
    onSave: (WidgetSettings) -> Unit,
    onCancel: () -> Unit
) {
    val initialSettings by widgetSettingsRepository.widgetSettings.collectAsState(initial = WidgetSettings())
    val quotas by repository.getAllQuotas().collectAsState(initial = emptyList())

    var opacity by remember(initialSettings) { mutableFloatStateOf(initialSettings.opacity) }
    var themeMode by remember(initialSettings) { mutableStateOf(initialSettings.themeMode) }
    var cornerRadiusDp by remember(initialSettings) { mutableIntStateOf(initialSettings.cornerRadiusDp) }
    var selectedIds by remember(initialSettings) { mutableStateOf(initialSettings.selectedQuotaIds) }
    var showQuickLogButtons by remember(initialSettings) { mutableStateOf(initialSettings.showQuickLogButtons) }

    Scaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Widget settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSave(
                                WidgetSettings(
                                    opacity = opacity,
                                    themeMode = themeMode,
                                    cornerRadiusDp = cornerRadiusDp,
                                    selectedQuotaIds = selectedIds,
                                    showQuickLogButtons = showQuickLogButtons
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Preview Section with Wallpaper Simulation
            Text(
                text = "Live Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2C3E50),
                                Color(0xFF000000),
                                Color(0xFF4CA1AF)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Widget Container Simulation
                val containerColor = when (themeMode) {
                    "DARK" -> Color(0xFF1E1E24).copy(alpha = opacity)
                    "LIGHT" -> Color(0xFFF5F5FA).copy(alpha = opacity)
                    "GLASS" -> Color(0x33FFFFFF).copy(alpha = opacity.coerceAtLeast(0.15f))
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = opacity)
                }

                val textColor = when (themeMode) {
                    "DARK" -> Color.White
                    "LIGHT" -> Color(0xFF1C1B1F)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(cornerRadiusDp.dp))
                        .background(containerColor)
                        .border(
                            width = if (themeMode == "GLASS") 1.dp else 0.dp,
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(cornerRadiusDp.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "WEEKLY GOALS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        val displayList = if (selectedIds.isEmpty()) {
                            quotas.take(2)
                        } else {
                            quotas.filter { it.id in selectedIds }.take(2)
                        }

                        if (displayList.isEmpty()) {
                            Text(
                                text = "Drawing Time • 4.5h / 8.0h",
                                fontSize = 12.sp,
                                color = textColor
                            )
                            LinearProgressIndicator(
                                progress = { 0.56f },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            displayList.forEach { quota ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = quota.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Text(
                                            text = "${quota.targetMinutes / 120}h / ${quota.targetMinutes / 60}h",
                                            fontSize = 11.sp,
                                            color = textColor.copy(alpha = 0.7f)
                                        )
                                    }
                                    if (showQuickLogButtons) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "+15m",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { 0.5f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Opacity Setting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Opacity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(opacity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = opacity,
                        onValueChange = { opacity = it },
                        valueRange = 0.1f..1.0f
                    )
                }
            }

            // Colors Setting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Colors & Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SYSTEM" to "System", "DARK" to "Dark", "LIGHT" to "Light", "GLASS" to "Glass").forEach { (mode, label) ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { themeMode = mode },
                                label = { Text(label) },
                                leadingIcon = if (themeMode == mode) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Shape Setting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Shape & Radius",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(28 to "Pill (28dp)", 24 to "Rounded (24dp)", 16 to "Medium (16dp)", 8 to "Subtle (8dp)").forEach { (radius, label) ->
                            val isSelected = cornerRadiusDp == radius
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { cornerRadiusDp = radius }
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                        else Modifier
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$radius",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = label.split(" ")[0],
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quota Filter Card (Multi-select Switches)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Displayed Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val isAllSelected = selectedIds.isEmpty() || (quotas.isNotEmpty() && selectedIds.size == quotas.size)

                    // Master All Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedIds = if (isAllSelected) emptySet() else emptySet()
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("All Goals", fontWeight = FontWeight.Bold)
                            Text("Show all active goals on widget", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isAllSelected,
                            onCheckedChange = { checked ->
                                selectedIds = if (checked) emptySet() else quotas.map { it.id }.toSet().minus(quotas.firstOrNull()?.id ?: -1)
                            }
                        )
                    }

                    // Individual Goal Switches
                    quotas.forEach { quota ->
                        val isGoalSelected = selectedIds.isEmpty() || quota.id in selectedIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newSet = if (selectedIds.isEmpty()) {
                                        quotas.map { it.id }.filter { it != quota.id }.toSet()
                                    } else if (quota.id in selectedIds) {
                                        selectedIds - quota.id
                                    } else {
                                        val added = selectedIds + quota.id
                                        if (added.size == quotas.size) emptySet() else added
                                    }
                                    selectedIds = newSet
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(quota.title, fontWeight = FontWeight.SemiBold)
                            Switch(
                                checked = isGoalSelected,
                                onCheckedChange = { checked ->
                                    val newSet = if (selectedIds.isEmpty()) {
                                        if (!checked) quotas.map { it.id }.filter { it != quota.id }.toSet() else emptySet()
                                    } else if (checked) {
                                        val added = selectedIds + quota.id
                                        if (added.size == quotas.size) emptySet() else added
                                    } else {
                                        selectedIds - quota.id
                                    }
                                    selectedIds = newSet
                                }
                            )
                        }
                    }
                }
            }

            // Quick Log Button Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showQuickLogButtons = !showQuickLogButtons }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Quick Log Buttons",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Show +15m button directly on widget",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = showQuickLogButtons,
                        onCheckedChange = { showQuickLogButtons = it }
                    )
                }
            }
        }
    }
}
