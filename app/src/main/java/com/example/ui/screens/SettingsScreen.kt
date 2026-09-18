package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppLanguage
import com.example.ui.DayPlanUiState
import com.example.ui.StringsDefinition
import java.time.DayOfWeek

@Composable
fun SettingsScreen(
    state: DayPlanUiState,
    strings: StringsDefinition,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeModeChange: (String) -> Unit,
    onFirstDayOfWeekChange: (DayOfWeek) -> Unit,
    on24HourChange: (Boolean) -> Unit,
    onStudentModeToggle: (Boolean) -> Unit,
    onExportData: () -> String,
    onImportData: (String) -> Boolean,
    onClearAllData: () -> Unit,
    onOpenAuthDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.settings,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Account & Firebase Auth Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.authUser.isAuthenticated)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val initial = remember(state.authUser.displayName, state.authUser.email) {
                        val name = state.authUser.displayName?.ifBlank { null } ?: state.authUser.email ?: "U"
                        name.take(1).uppercase()
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.authUser.isAuthenticated && !state.authUser.isAnonymous) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.authUser.isAuthenticated) {
                                if (state.authUser.isAnonymous) strings.guestAccount
                                else state.authUser.displayName?.ifBlank { null } ?: "Firebase User"
                            } else {
                                strings.notSignedIn
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (state.authUser.isAuthenticated) {
                                if (state.authUser.isAnonymous) "Guest Mode (Local Only)"
                                else state.authUser.email ?: "Signed in with Firebase"
                            } else {
                                "Sign in with Firebase to sync your plan"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onOpenAuthDialog,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("account_action_button")
                    ) {
                        Text(if (state.authUser.isAuthenticated) "Manage" else strings.signIn)
                    }
                }
            }
        }

        // 1. Language Section (English, Hindi, Hinglish)
        item {
            SettingsCard(
                title = strings.language,
                icon = Icons.Default.Language
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLanguageChange(lang) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.language == lang,
                                onClick = { onLanguageChange(lang) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (state.language == lang) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // 2. Appearance (Light, Dark, System Default)
        item {
            SettingsCard(
                title = strings.appearance,
                icon = Icons.Default.Brightness4
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.themeMode == "SYSTEM",
                        onClick = { onThemeModeChange("SYSTEM") },
                        label = { Text(strings.system) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.themeMode == "LIGHT",
                        onClick = { onThemeModeChange("LIGHT") },
                        label = { Text(strings.light) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.themeMode == "DARK",
                        onClick = { onThemeModeChange("DARK") },
                        label = { Text(strings.dark) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Calendar & Time Preferences
        item {
            SettingsCard(
                title = "Calendar & Time",
                icon = Icons.Default.CalendarMonth
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 12 vs 24 hour toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.timeFormat,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (state.is24Hour) "24-Hour (18:00)" else "12-Hour (6:00 PM)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.is24Hour,
                            onCheckedChange = on24HourChange
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // First day of week (Monday vs Sunday)
                    Column {
                        Text(
                            text = strings.firstDayOfWeek,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.firstDayOfWeek == DayOfWeek.MONDAY,
                                onClick = { onFirstDayOfWeekChange(DayOfWeek.MONDAY) },
                                label = { Text("Monday") }
                            )
                            FilterChip(
                                selected = state.firstDayOfWeek == DayOfWeek.SUNDAY,
                                onClick = { onFirstDayOfWeekChange(DayOfWeek.SUNDAY) },
                                label = { Text("Sunday") }
                            )
                        }
                    }
                }
            }
        }

        // 4. Student Study Mode Toggle
        item {
            SettingsCard(
                title = strings.studentMode,
                icon = Icons.Default.School
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Student Mode",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = strings.studentModeDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.studentModeEnabled,
                        onCheckedChange = onStudentModeToggle
                    )
                }
            }
        }

        // 5. Notifications
        item {
            SettingsCard(
                title = "Notifications & Reminders",
                icon = Icons.Default.Notifications
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Alarm reminders are scheduled for your events.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Notification Permission")
                        }
                    }
                }
            }
        }

        // 6. Data & Privacy (Local storage, Export, Import, Clear data)
        item {
            SettingsCard(
                title = "Data & Privacy",
                icon = Icons.Default.Security
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your calendar data is saved locally on this device. No account or external cloud sync required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            val json = onExportData()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("DayPlan Backup", json)
                            clipboard.setPrimaryClip(clip)

                            // Also provide share sheet
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "DayPlan Calendar Backup JSON")
                                putExtra(Intent.EXTRA_TEXT, json)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export Calendar Backup"))
                            exportSuccessMessage = strings.dataExported
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.exportCalendar)
                    }

                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.importCalendar)
                    }

                    Button(
                        onClick = { showClearDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.clearData)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.clearData) },
            text = { Text(strings.clearDataConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllData()
                        showClearDialog = false
                    }
                ) {
                    Text("Erase Everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Import Data Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(strings.importCalendar) },
            text = {
                Column {
                    Text("Paste your exported JSON calendar data below:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{ \"events\": [...], \"tasks\": [...] }") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            onImportData(importJsonText)
                            showImportDialog = false
                            importJsonText = ""
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
