package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.EventEntity
import com.example.ui.StringsDefinition
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditEventDialog(
    initialDate: LocalDate,
    eventToEdit: EventEntity?,
    strings: StringsDefinition,
    is24Hour: Boolean,
    onDismiss: () -> Modifier,
    onSave: (
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        category: String,
        reminderMinutes: Int,
        repeatType: String,
        notes: String,
        isImportant: Boolean,
        colorHex: Long
    ) -> Unit
) {
    var title by remember { mutableStateOf(eventToEdit?.title ?: "") }
    var selectedDate by remember {
        mutableStateOf(
            if (eventToEdit != null) LocalDate.parse(eventToEdit.date) else initialDate
        )
    }
    var startTime by remember {
        mutableStateOf(
            if (eventToEdit != null) LocalTime.parse(eventToEdit.startTime)
            else LocalTime.of(18, 0)
        )
    }
    var endTime by remember {
        mutableStateOf(
            if (eventToEdit != null) LocalTime.parse(eventToEdit.endTime)
            else LocalTime.of(19, 0)
        )
    }

    val categories = listOf(
        "📚 Study",
        "🏫 School",
        "📝 Exam",
        "👤 Personal",
        "⭐ Important",
        "Other"
    )

    var selectedCategory by remember {
        mutableStateOf(
            eventToEdit?.let { existing ->
                categories.firstOrNull { it.contains(existing.category, ignoreCase = true) } ?: existing.category
            } ?: "📚 Study"
        )
    }

    val reminderOptions = listOf(
        0 to "No reminder",
        5 to "5 min before",
        15 to "15 min before",
        30 to "30 min before",
        60 to "1 hour before",
        1440 to "1 day before"
    )
    var selectedReminder by remember {
        mutableIntStateOf(eventToEdit?.reminderMinutes ?: 15)
    }

    val repeatOptions = listOf(
        "NONE" to "Does not repeat",
        "DAILY" to "Daily",
        "WEEKLY" to "Weekly",
        "MONTHLY" to "Monthly",
        "YEARLY" to "Yearly"
    )
    var selectedRepeat by remember {
        mutableStateOf(eventToEdit?.repeatType ?: "NONE")
    }

    var notes by remember { mutableStateOf(eventToEdit?.notes ?: "") }
    var isImportant by remember { mutableStateOf(eventToEdit?.isImportant ?: false) }

    val paletteColors = listOf(
        0xFF3F51B5, // Indigo
        0xFF00897B, // Teal
        0xFFE64A19, // Deep Orange
        0xFF8E24AA, // Purple
        0xFF1E88E5, // Blue
        0xFF43A047, // Green
        0xFFE53935  // Red
    )
    var selectedColor by remember {
        mutableLongStateOf(eventToEdit?.colorHex ?: paletteColors.first())
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_event_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (eventToEdit != null) strings.editEvent else strings.addEvent,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text(strings.title) },
                    placeholder = { Text("e.g. Physics Revision") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title is required") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${strings.date}: ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()))}"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Pickers (Start & End)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (is24Hour) {
                                startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                            } else {
                                startTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (is24Hour) {
                                endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                            } else {
                                endTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Chips
                Text(
                    text = strings.category,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                if (cat.contains("Important")) {
                                    isImportant = true
                                }
                            },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Dropdown
                Text(
                    text = strings.reminder,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                var reminderExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = reminderExpanded,
                    onExpandedChange = { reminderExpanded = !reminderExpanded }
                ) {
                    OutlinedTextField(
                        value = reminderOptions.firstOrNull { it.first == selectedReminder }?.second ?: "15 min before",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderExpanded) },
                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = reminderExpanded,
                        onDismissRequest = { reminderExpanded = false }
                    ) {
                        reminderOptions.forEach { (mins, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedReminder = mins
                                    reminderExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Repeat Dropdown
                Text(
                    text = strings.repeat,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                var repeatExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = repeatExpanded,
                    onExpandedChange = { repeatExpanded = !repeatExpanded }
                ) {
                    OutlinedTextField(
                        value = repeatOptions.firstOrNull { it.first == selectedRepeat }?.second ?: "Does not repeat",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatExpanded) },
                        leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = repeatExpanded,
                        onDismissRequest = { repeatExpanded = false }
                    ) {
                        repeatOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedRepeat = key
                                    repeatExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Important Star Toggle
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isImportant = !isImportant },
                    color = if (isImportant) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isImportant) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (isImportant) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.markImportant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isImportant) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color Selector
                Text(
                    text = "Color Badge",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    paletteColors.forEach { colorVal ->
                        val isColorSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isColorSelected) 3.dp else 1.dp,
                                    color = if (isColorSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorVal }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.notes) },
                    placeholder = { Text("e.g. NLM ke questions solve karne hain.") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Cancel | Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(strings.cancel)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }
                            val cleanCategory = selectedCategory.replace("📚 ", "").replace("🏫 ", "")
                                .replace("📝 ", "").replace("👤 ", "").replace("⭐ ", "").trim()

                            onSave(
                                title.trim(),
                                selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                cleanCategory,
                                selectedReminder,
                                selectedRepeat,
                                notes.trim(),
                                isImportant,
                                selectedColor
                            )
                        },
                        modifier = Modifier.testTag("save_event_button")
                    ) {
                        Text(strings.save)
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(strings.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = startTime.hour,
            initialMinute = startTime.minute,
            is24Hour = is24Hour
        )
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.startTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showStartTimePicker = false }) {
                            Text(strings.cancel)
                        }
                        TextButton(onClick = {
                            startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showStartTimePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = endTime.hour,
            initialMinute = endTime.minute,
            is24Hour = is24Hour
        )
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.endTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEndTimePicker = false }) {
                            Text(strings.cancel)
                        }
                        TextButton(onClick = {
                            endTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showEndTimePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}
