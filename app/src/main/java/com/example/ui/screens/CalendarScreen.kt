package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventEntity
import com.example.data.TaskEntity
import com.example.ui.CalendarViewMode
import com.example.ui.DayPlanUiState
import com.example.ui.StringsDefinition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun CalendarScreen(
    state: DayPlanUiState,
    strings: StringsDefinition,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onGoToToday: () -> Unit,
    onChangeViewMode: (CalendarViewMode) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAddEvent: (LocalDate) -> Unit,
    onOpenEditEvent: (EventEntity) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit,
    onToggleEventCompleted: (EventEntity) -> Unit,
    onOpenAddTask: (LocalDate) -> Unit,
    onOpenEditTask: (TaskEntity) -> Unit,
    onToggleTaskCompleted: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onOpenAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val selectedDateStr = remember(state.selectedDate) {
        state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    val dayEvents = remember(state.allEvents, selectedDateStr) {
        state.allEvents.filter { it.date == selectedDateStr }
    }
    val dayTasks = remember(state.allTasks, selectedDateStr) {
        state.allTasks.filter { it.date == selectedDateStr }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Section: Month Year Header, Navigation, Today & Search buttons
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val monthName = state.currentMonth.month.getDisplayName(
                                TextStyle.FULL, Locale.getDefault()
                            )
                            Text(
                                text = "$monthName ${state.currentMonth.year}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onPreviousMonth,
                                modifier = Modifier.testTag("prev_month_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                            }
                            IconButton(
                                onClick = onNextMonth,
                                modifier = Modifier.testTag("next_month_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                            }
                            FilledTonalButton(
                                onClick = onGoToToday,
                                contentPadding = ButtonDefaults.TextButtonContentPadding,
                                modifier = Modifier.testTag("today_button")
                            ) {
                                Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.today)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onOpenSearch,
                                modifier = Modifier.testTag("search_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(
                                onClick = onOpenAccount,
                                modifier = Modifier.testTag("account_button")
                            ) {
                                if (state.authUser.isAuthenticated && !state.authUser.isAnonymous) {
                                    val initial = (state.authUser.displayName?.ifBlank { null } ?: state.authUser.email ?: "U").take(1).uppercase()
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initial,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                } else {
                                    Icon(Icons.Default.AccountCircle, contentDescription = "Account")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // View Switcher (Month / Week / Day)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.calendarViewMode == CalendarViewMode.MONTH,
                            onClick = { onChangeViewMode(CalendarViewMode.MONTH) },
                            label = { Text(strings.month) },
                            modifier = Modifier.weight(1f).testTag("view_month")
                        )
                        FilterChip(
                            selected = state.calendarViewMode == CalendarViewMode.WEEK,
                            onClick = { onChangeViewMode(CalendarViewMode.WEEK) },
                            label = { Text(strings.week) },
                            modifier = Modifier.weight(1f).testTag("view_week")
                        )
                        FilterChip(
                            selected = state.calendarViewMode == CalendarViewMode.DAY,
                            onClick = { onChangeViewMode(CalendarViewMode.DAY) },
                            label = { Text(strings.day) },
                            modifier = Modifier.weight(1f).testTag("view_day")
                        )
                    }
                }
            }
        }

        // 2. Calendar Views: Month / Week / Day
        item {
            when (state.calendarViewMode) {
                CalendarViewMode.MONTH -> {
                    MonthCalendarView(
                        currentMonth = state.currentMonth,
                        selectedDate = state.selectedDate,
                        firstDayOfWeek = state.firstDayOfWeek,
                        allEvents = state.allEvents,
                        allTasks = state.allTasks,
                        onDateClick = onSelectDate
                    )
                }
                CalendarViewMode.WEEK -> {
                    WeekCalendarView(
                        selectedDate = state.selectedDate,
                        firstDayOfWeek = state.firstDayOfWeek,
                        allEvents = state.allEvents,
                        allTasks = state.allTasks,
                        onDateClick = onSelectDate
                    )
                }
                CalendarViewMode.DAY -> {
                    DayTimelineView(
                        selectedDate = state.selectedDate,
                        events = dayEvents,
                        is24Hour = state.is24Hour,
                        onOpenEvent = onOpenEditEvent
                    )
                }
            }
        }

        // 3. Selected Day Summary Header & Quick Actions
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedSelected = state.selectedDate.format(
                        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
                    )
                    val isToday = state.selectedDate == today
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isToday) "${strings.today}, ${state.selectedDate.format(DateTimeFormatter.ofPattern("d MMM"))}" else formattedSelected,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isToday) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = strings.today,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AssistChip(
                            onClick = { onOpenAddEvent(state.selectedDate) },
                            label = { Text("+ ${strings.addEvent}") },
                            modifier = Modifier.testTag("add_event_chip")
                        )
                        AssistChip(
                            onClick = { onOpenAddTask(state.selectedDate) },
                            label = { Text("+ ${strings.addTask}") },
                            modifier = Modifier.testTag("add_task_chip")
                        )
                    }
                }
            }
        }

        // 4. Day's Events List
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Events (${dayEvents.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        if (dayEvents.isEmpty()) {
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.noEventsToday,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(dayEvents, key = { "event_${it.id}" }) { event ->
                EventCard(
                    event = event,
                    is24Hour = state.is24Hour,
                    onEdit = { onOpenEditEvent(event) },
                    onDelete = { onDeleteEvent(event) },
                    onToggleComplete = { onToggleEventCompleted(event) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // 5. Day's Tasks List
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    text = "Tasks (${dayTasks.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        if (dayTasks.isEmpty()) {
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.noTasksToday,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(dayTasks, key = { "task_${it.id}" }) { task ->
                DayTaskItemCard(
                    task = task,
                    onToggleComplete = { onToggleTaskCompleted(task) },
                    onEdit = { onOpenEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun MonthCalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
    allEvents: List<EventEntity>,
    allTasks: List<TaskEntity>,
    onDateClick: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val daysOfWeek = remember(firstDayOfWeek) {
        val days = DayOfWeek.values().toList()
        val startIndex = days.indexOf(firstDayOfWeek)
        days.subList(startIndex, days.size) + days.subList(0, startIndex)
    }

    val monthDays = remember(currentMonth, firstDayOfWeek) {
        generateMonthGrid(currentMonth, firstDayOfWeek)
    }

    // Map dates to events and tasks for fast lookup
    val eventsByDate = remember(allEvents) {
        allEvents.groupBy { it.date }
    }
    val tasksByDate = remember(allTasks) {
        allTasks.groupBy { it.date }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Day of week headers: Mon, Tue, Wed, Thu, Fri, Sat, Sun
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Calendar weeks
            val weeks = monthDays.chunked(7)
            weeks.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        val isCurrentMonth = date.month == currentMonth.month
                        val isSelected = date == selectedDate
                        val isToday = date == today

                        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val dateEvents = eventsByDate[dateStr] ?: emptyList()
                        val dateTasks = tasksByDate[dateStr] ?: emptyList()
                        val hasCompletedTasks = dateTasks.any { it.isCompleted }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                    color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onDateClick(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                // Visual Indicator Dots: Event dot & Task checkmark indicator
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(5.dp)
                                ) {
                                    if (dateEvents.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else Color(dateEvents.first().colorHex)
                                                )
                                        )
                                    }
                                    if (hasCompletedTasks) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else Color(0xFF4CAF50)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekCalendarView(
    selectedDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
    allEvents: List<EventEntity>,
    allTasks: List<TaskEntity>,
    onDateClick: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val weekStart = remember(selectedDate, firstDayOfWeek) {
        selectedDate.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    }
    val weekDays = remember(weekStart) {
        (0..6).map { weekStart.plusDays(it.toLong()) }
    }

    val eventsByDate = remember(allEvents) { allEvents.groupBy { it.date } }
    val tasksByDate = remember(allTasks) { allTasks.groupBy { it.date } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { date ->
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val eventCount = eventsByDate[dateStr]?.size ?: 0
                    val taskCount = tasksByDate[dateStr]?.size ?: 0

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { onDateClick(date) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (eventCount > 0 || taskCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(5.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayTimelineView(
    selectedDate: LocalDate,
    events: List<EventEntity>,
    is24Hour: Boolean,
    onOpenEvent: (EventEntity) -> Unit
) {
    val hours = (6..23).toList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Day Timeline — ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            hours.forEach { hour ->
                val hourPrefix = String.format(Locale.US, "%02d", hour)
                val hourEvents = events.filter { it.startTime.startsWith(hourPrefix) }
                val timeLabel = if (is24Hour) {
                    "$hourPrefix:00"
                } else {
                    val amPm = if (hour < 12) "AM" else "PM"
                    val displayHour = if (hour % 12 == 0) 12 else hour % 12
                    "$displayHour:00 $amPm"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(65.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    ) {
                        if (hourEvents.isEmpty()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                hourEvents.forEach { event ->
                                    Surface(
                                        color = Color(event.colorHex).copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(event.colorHex)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onOpenEvent(event) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${event.startTime} - ${event.endTime}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(event.colorHex)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = event.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (event.isImportant) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    Icons.Filled.Star,
                                                    contentDescription = "Important",
                                                    tint = Color(0xFFFFB300),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: EventEntity,
    is24Hour: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category color strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(event.colorHex))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (event.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (event.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (event.isImportant) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Important",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${event.startTime} - ${event.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = event.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (event.repeatType != "NONE") {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = event.repeatType,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                if (event.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (event.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Complete Event",
                    tint = if (event.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Event",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun DayTaskItemCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Complete",
                    tint = if (task.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    if (task.priority == "HIGH") {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "High",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun generateMonthGrid(currentMonth: YearMonth, firstDayOfWeek: DayOfWeek): List<LocalDate> {
    val firstOfMonth = currentMonth.atDay(1)
    val lastOfMonth = currentMonth.atEndOfMonth()

    val daysBefore = (firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    val startDate = firstOfMonth.minusDays(daysBefore.toLong())

    val totalDays = ((daysBefore + lastOfMonth.dayOfMonth + 6) / 7) * 7
    return (0 until totalDays).map { startDate.plusDays(it.toLong()) }
}
