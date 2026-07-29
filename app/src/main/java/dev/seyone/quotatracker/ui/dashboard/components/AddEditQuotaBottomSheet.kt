package dev.seyone.quotatracker.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.seyone.quotatracker.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.data.model.ResetStrategy
import dev.seyone.quotatracker.ui.components.QuotaIconRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditQuotaBottomSheet(
    quotaToEdit: QuotaEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (quotaId: Int?, title: String, targetHours: Int, targetMinutes: Int, resetStrategy: ResetStrategy, isPinned: Boolean, iconKey: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditMode = quotaToEdit != null

    var title by remember(quotaToEdit) { mutableStateOf(quotaToEdit?.title ?: "") }
    var hoursText by remember(quotaToEdit) {
        mutableStateOf(quotaToEdit?.let { (it.targetMinutes / 60).toString() } ?: "7")
    }
    var minutesText by remember(quotaToEdit) {
        mutableStateOf(quotaToEdit?.let { (it.targetMinutes % 60).toString() } ?: "0")
    }
    var resetStrategy by remember(quotaToEdit) {
        mutableStateOf(quotaToEdit?.resetStrategy ?: ResetStrategy.CLEAN)
    }
    var isPinned by remember(quotaToEdit) {
        mutableStateOf(quotaToEdit?.isPinned ?: false)
    }
    var selectedIconKey by remember(quotaToEdit) {
        mutableStateOf(quotaToEdit?.iconKey)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title on the left, Star Icon Button on the far right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Edit Quota" else "Add New Quota",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { isPinned = !isPinned }
                ) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Pin to Tile",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Icon Picker Section (20 Material Icons)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Icon (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(QuotaIconRegistry.availableIcons) { iconOption ->
                        val isSelected = selectedIconKey == iconOption.key
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedIconKey = if (isSelected) null else iconOption.key
                            },
                            label = { Text(iconOption.label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = iconOption.icon,
                                    contentDescription = iconOption.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Quota Title (e.g. Drawing)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Target Hours and Minutes Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { hoursText = it.filter { char -> char.isDigit() } },
                    label = { Text("Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it.filter { char -> char.isDigit() } },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Reset Strategy Button Group
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Reset Strategy",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResetStrategy.entries.forEachIndexed { index, strategy ->
                        SegmentedButton(
                            selected = resetStrategy == strategy,
                            onClick = { resetStrategy = strategy },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ResetStrategy.entries.size
                            )
                        ) {
                            Text(
                                text = strategy.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (resetStrategy == strategy) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Strategy description note in sentence case
                Text(
                    text = when (resetStrategy) {
                        ResetStrategy.CLEAN -> "Clean: Progress resets strictly to 0 every Monday at 00:00."
                        ResetStrategy.ROLLOVER -> "Rollover: Unfinished deficit or extra minutes roll into next week."
                        ResetStrategy.BANK -> "Bank: Accumulated surplus time is saved into a time bank."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val h = hoursText.toIntOrNull() ?: 0
                        val m = minutesText.toIntOrNull() ?: 0
                        if (title.isNotBlank() && (h > 0 || m > 0)) {
                            onConfirm(quotaToEdit?.id, title, h, m, resetStrategy, isPinned, selectedIconKey)
                        }
                    }
                ) {
                    Text(if (isEditMode) "Update Quota" else "Save Quota")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
