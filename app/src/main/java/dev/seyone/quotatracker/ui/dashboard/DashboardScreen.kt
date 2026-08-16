package dev.seyone.quotatracker.ui.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import dev.seyone.quotatracker.ui.dashboard.components.AddEditQuotaBottomSheet
import dev.seyone.quotatracker.ui.dashboard.components.ManualOverrideBottomSheet
import dev.seyone.quotatracker.ui.dashboard.components.QuotaCard
import dev.seyone.quotatracker.ui.dashboard.components.WeekPulseCard

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun DashboardScreen(
    viewModel: QuotaDashboardViewModel,
    topBarActions: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cardStyle by viewModel.cardStyle.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editQuotaTarget by viewModel.editQuotaTarget.collectAsState()
    val deleteQuotaTarget by viewModel.deleteQuotaTarget.collectAsState()
    val adjustQuotaTargetItem by viewModel.adjustQuotaTarget.collectAsState()
    val adjustQuotaRecentLogs by viewModel.adjustQuotaRecentLogs.collectAsState()
    val hasSeenFabTooltip by viewModel.hasSeenFabTooltip.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val windowSizeClass = androidx.compose.material3.adaptive.currentWindowAdaptiveInfo().windowSizeClass
    val isLandscape = windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT
    val isTabletOrLandscape = isLandscape || windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM || windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val gridColumnCount = if (isTabletOrLandscape) 2 else 1

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is DashboardUiEvent.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onUndoLog(event.lastLogIds)
                    }
                }
                is DashboardUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quota Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = { topBarActions() },
                windowInsets = if (isTabletOrLandscape) androidx.compose.foundation.layout.WindowInsets(0.dp) else TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog.value = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Quota")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (uiState.quotaItems.isEmpty()) {
                        EmptyState(
                            onAddClick = { viewModel.showAddDialog.value = true },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(gridColumnCount),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp
                        ) {
                            uiState.pulseData?.let { pulse ->
                                item(
                                    key = "week_pulse_card",
                                    span = StaggeredGridItemSpan.FullLine
                                ) {
                                    WeekPulseCard(pulseData = pulse)
                                }
                            }

                            items(
                                items = uiState.quotaItems,
                                key = { it.quota.id }
                            ) { item ->
                                QuotaCard(
                                    item = item,
                                    cardStyle = cardStyle,
                                    onQuickLog = { minutes ->
                                        viewModel.onQuickLog(item, minutes)
                                    },
                                    onLongPress = {
                                        viewModel.adjustQuotaTarget.value = item
                                    },
                                    onEdit = {
                                        viewModel.onRequestEditQuota(item.quota)
                                    },
                                    onDelete = {
                                        viewModel.onRequestDeleteQuota(item)
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }

                        // Persistent Tooltip Guidance Overlay (Shown ONLY ONCE on first run)
                        if (!hasSeenFabTooltip) {
                            val tooltipBottomPadding = if (isLandscape) 20.dp else 88.dp
                            Popup(
                                alignment = Alignment.BottomEnd,
                                onDismissRequest = { viewModel.onDismissFabTooltip() },
                                properties = PopupProperties(dismissOnClickOutside = false)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .padding(bottom = tooltipBottomPadding, end = 16.dp, start = 32.dp)
                                        .widthIn(max = 400.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.inverseSurface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Tap + to add quotas, or tap cards to log time!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.inverseOnSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(
                                            onClick = { viewModel.onDismissFabTooltip() },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Got it", color = MaterialTheme.colorScheme.inversePrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            detailPane = {
                // Placeholder Detail Pane for future Quota Insights & Editing
            }
        )

        // Modal Bottom Sheets & Smart Delete Dialog
        if (showAddDialog) {
            AddEditQuotaBottomSheet(
                quotaToEdit = null,
                onDismiss = { viewModel.showAddDialog.value = false },
                onConfirm = { quotaId, title, h, m, resetStrategy, isPinned, iconKey ->
                    viewModel.onSaveQuota(quotaId, title, h, m, resetStrategy, isPinned, iconKey)
                }
            )
        }

        editQuotaTarget?.let { target ->
            AddEditQuotaBottomSheet(
                quotaToEdit = target,
                onDismiss = { viewModel.onDismissEditDialog() },
                onConfirm = { quotaId, title, h, m, resetStrategy, isPinned, iconKey ->
                    viewModel.onSaveQuota(quotaId, title, h, m, resetStrategy, isPinned, iconKey)
                }
            )
        }

        deleteQuotaTarget?.let { item ->
            var deleteHistory by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { viewModel.onDismissDeleteDialog() },
                title = { Text("Delete Quota?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("This will remove the quota from your active week.")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = deleteHistory,
                                onCheckedChange = { deleteHistory = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete all historical time logs as well.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.onConfirmDeleteQuota(deleteHistory) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        adjustQuotaTargetItem?.let { targetItem ->
            dev.seyone.quotatracker.ui.dashboard.components.LogAdjustmentBottomSheet(
                quotaItem = targetItem,
                recentLogs = adjustQuotaRecentLogs,
                onDismiss = { viewModel.adjustQuotaTarget.value = null },
                onAdjustTime = { minutesDelta ->
                    viewModel.onQuickLog(targetItem, minutesDelta)
                    viewModel.adjustQuotaTarget.value = null
                },
                onDeleteLogEntry = { logId ->
                    viewModel.onDeleteLogEntry(logId)
                }
            )
        }
    }
}

@Composable
private fun EmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.HourglassEmpty,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Quotas Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set weekly time goals for your hobbies, studies, or habits.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Your First Quota")
        }
    }
}
