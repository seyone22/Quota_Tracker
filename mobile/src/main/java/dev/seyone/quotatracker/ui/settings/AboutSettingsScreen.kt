package dev.seyone.quotatracker.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import dev.seyone.quotatracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var showWhatsNewDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val githubUrl = "https://github.com/seyone22/Quota_Tracker"
    val privacyPolicyUrl = "https://github.com/seyone22/Quota_Tracker/blob/master/PRIVACY_POLICY.md"
    val websiteUrl = "https://seyone.dev"
    val devEmail = "hi@seyone.dev"

    val versionName = stringResource(id = R.string.app_version_name)
    val buildInfo = stringResource(id = R.string.app_full_build_info)
    val whatsNewList = stringArrayResource(id = R.array.whats_new_highlights)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = "About",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Brand Logo & Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = dev.seyone.quotatracker.R.drawable.logo),
                    contentDescription = "Quota Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(144.dp)
                )
                Text(
                    text = "Quota Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "168 hours a week. Make them count.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Created by Seyone Gunasingham",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider()

            // List Options
            SettingsClickableItem(
                icon = Icons.Outlined.SystemUpdate,
                title = "Version",
                subtitle = buildInfo,
                onClick = {
                    Toast.makeText(context, "Quota Tracker v$versionName Alpha Build", Toast.LENGTH_SHORT).show()
                }
            )

            SettingsClickableItem(
                icon = Icons.Outlined.NewReleases,
                title = "What's new",
                subtitle = "See release notes & new features",
                onClick = { showWhatsNewDialog = true }
            )

            SettingsClickableItem(
                icon = Icons.Outlined.Code,
                title = "GitHub Repository",
                subtitle = "seyone22/Quota_Tracker",
                onClick = { openUrl(context, githubUrl) }
            )

            SettingsClickableItem(
                icon = Icons.Outlined.Code,
                title = "Open source licenses",
                subtitle = "Third-party libraries & tools",
                onClick = { showLicensesDialog = true }
            )

            SettingsClickableItem(
                icon = Icons.Outlined.PrivacyTip,
                title = "Privacy policy",
                subtitle = "View privacy policy on GitHub",
                onClick = { openUrl(context, privacyPolicyUrl) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Social & Developer Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { openUrl(context, websiteUrl) }) {
                    Icon(Icons.Outlined.Language, contentDescription = "Developer Website", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { openUrl(context, githubUrl) }) {
                    Icon(Icons.Outlined.Code, contentDescription = "GitHub", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { openEmail(context, devEmail) }) {
                    Icon(Icons.Outlined.Email, contentDescription = "Email Developer", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // What's New Dialog
    if (showWhatsNewDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsNewDialog = false },
            title = { Text("What's New in v$versionName") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    whatsNewList.forEach { item ->
                        Text("• $item", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWhatsNewDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Open Source Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Licenses") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LicenseItem(name = "Jetpack Compose", license = "Apache License 2.0")
                    LicenseItem(name = "Material 3", license = "Apache License 2.0")
                    LicenseItem(name = "AndroidX Room", license = "Apache License 2.0")
                    LicenseItem(name = "Kotlin Coroutines", license = "Apache License 2.0")
                    LicenseItem(name = "Horologist Wear OS", license = "Apache License 2.0")
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Quota Tracker respects your absolute privacy.",
                        fontWeight = FontWeight.Bold
                    )
                    Text("• All data, quotas, and logs remain strictly on your local device SQLite database.")
                    Text("• Zero analytics, zero ad tracking, and zero telemetry data collected.")
                    Text("• Full data portability with instant JSON & CSV export/import.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPrivacyDialog = false
                    openUrl(context, privacyPolicyUrl)
                }) {
                    Text("View Online Policy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link: $url", Toast.LENGTH_SHORT).show()
    }
}

private fun openEmail(context: Context, email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Contact: $email", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun LicenseItem(name: String, license: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(text = license, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
