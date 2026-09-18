package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.TaskEntity
import com.example.ui.DayPlanUiState
import com.example.ui.StringsDefinition
import com.example.ui.TaskFilter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TasksScreen(
    state: DayPlanUiState,
    strings: StringsDefinition,
    onFilterChange: (TaskFilter) -> Unit,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onQuickAddTask: (String) -> Unit,
    onOpenAddTask: (LocalDate) -> Unit,
    onOpenEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onRescheduleTask: (TaskEntity, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var quickTaskTitle by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    val today = remember { LocalDate.now() }
    val todayStr = remember(today) { today.format(DateTimeFormatter.ISO_LOCAL_DATE) }

    val categories = listOf("All", "DPP", "Homework", "Revision", "Test", "Lecture", "General")

    val filteredTasks = remember(state.allTasks, state.taskFilter, selectedCategoryFilter, todayStr) {
        state.allTasks.filter { task ->
            // Category filter
            val matchesCategory = selectedCategoryFilter == null || selectedCategoryFilter == "All" ||
                    task.category.equals(selectedCategoryFilter, ignoreCase = true)

            // Tab filter
            val matchesTab = when (state.taskFilter) {
                TaskFilter.ALL -> true
                TaskFilter.TODAY -> task.date == todayStr
                TaskFilter.UPCOMING -> task.date > todayStr
                TaskFilter.COMPLETED -> task.isCompleted
            }
            matchesCategory && matchesTab
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("tasks_screen")
    ) {
        // Top Header Card with Quick Add
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.tasks,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onOpenAddTask(state.selectedDate) },
                        modifier = Modifier.testTag("add_task_header_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Add Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quickTaskTitle,
                        onValueChange = { quickTaskTitle = it },
                        placeholder = { Text(strings.quickAddPlaceholder) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (quickTaskTitle.isNotBlank()) {
                                onQuickAddTask(quickTaskTitle)
                                quickTaskTitle = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("quick_add_submit_button")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Submit quick task",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Filter Tabs (All, Today, Upcoming, Done)
        TabRow(
            selectedTabIndex = state.taskFilter.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = state.taskFilter == TaskFilter.ALL,
                onClick = { onFilterChange(TaskFilter.ALL) },
                text = { Text(strings.filterAll) }
            )
            Tab(
                selected = state.taskFilter == TaskFilter.TODAY,
                onClick = { onFilterChange(TaskFilter.TODAY) },
                text = { Text(strings.filterToday) }
            )
            Tab(
                selected = state.taskFilter == TaskFilter.UPCOMING,
                onClick = { onFilterChange(TaskFilter.UPCOMING) },
                text = { Text(strings.filterUpcoming) }
            )
            Tab(
                selected = state.taskFilter == TaskFilter.COMPLETED,
                onClick = { onFilterChange(TaskFilter.COMPLETED) },
                text = { Text(strings.filterCompleted) }
            )
        }

        // Category Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = (selectedCategoryFilter == null && category == "All") ||
                        (selectedCategoryFilter == category)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCategoryFilter = if (category == "All") null else category
                    },
                    label = { Text(category) }
                )
            }
        }

        // Task List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks found in this section",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredTasks, key = { "task_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        todayStr = todayStr,
                        onToggleComplete = { onToggleTaskComplete(task) },
                        onEdit = { onOpenEditTask(task) },
                        onDelete = { onDeleteTask(task) },
                        onReschedule = { newDate -> onRescheduleTask(task, newDate) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    todayStr: String,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReschedule: (LocalDate) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val dateLabel = remember(task.date, todayStr) {
        when (task.date) {
            todayStr -> "Today"
            LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE) -> "Tomorrow"
            else -> try {
                LocalDate.parse(task.date).format(DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()))
            } catch (_: Exception) {
                task.date
            }
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Complete",
                    tint = if (task.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Badge
                    Surface(
                        color = if (task.date == todayStr) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (task.date == todayStr) FontWeight.Bold else FontWeight.Normal,
                            color = if (task.date == todayStr) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Category
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Priority
                    if (task.priority == "HIGH") {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "High",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Options menu (Reschedule, Edit, Delete)
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Reschedule to Today") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        onClick = {
                            onReschedule(LocalDate.now())
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reschedule to Tomorrow") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        onClick = {
                            onReschedule(LocalDate.now().plusDays(1))
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reschedule to Next Week") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        onClick = {
                            onReschedule(LocalDate.now().plusDays(7))
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            onEdit()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    }
}
