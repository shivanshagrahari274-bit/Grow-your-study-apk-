package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.AddEditEventDialog
import com.example.ui.dialogs.AddEditTaskDialog
import com.example.ui.dialogs.AuthDialog
import com.example.ui.dialogs.SearchDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen

@Composable
fun DayPlanApp(
    viewModel: DayPlanViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = remember(state.language) { getStrings(state.language) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    // Android back button navigation handler
    BackHandler(
        enabled = state.isSearchOpen || state.isAddEventOpen || state.isAddTaskOpen ||
                state.isAuthDialogOpen || state.currentTab != NavTab.CALENDAR
    ) {
        when {
            state.isSearchOpen -> viewModel.setSearchOpen(false)
            state.isAddEventOpen -> viewModel.closeAddEditEvent()
            state.isAddTaskOpen -> viewModel.closeAddEditTask()
            state.isAuthDialogOpen -> viewModel.dismissAuthDialog()
            state.currentTab != NavTab.CALENDAR -> viewModel.setNavTab(NavTab.CALENDAR)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                NavigationBarItem(
                    selected = state.currentTab == NavTab.CALENDAR,
                    onClick = { viewModel.setNavTab(NavTab.CALENDAR) },
                    icon = {
                        Icon(
                            if (state.currentTab == NavTab.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = strings.calendar
                        )
                    },
                    label = { Text(strings.calendar) },
                    modifier = Modifier.testTag("nav_tab_calendar")
                )
                NavigationBarItem(
                    selected = state.currentTab == NavTab.TASKS,
                    onClick = { viewModel.setNavTab(NavTab.TASKS) },
                    icon = {
                        Icon(
                            if (state.currentTab == NavTab.TASKS) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                            contentDescription = strings.tasks
                        )
                    },
                    label = { Text(strings.tasks) },
                    modifier = Modifier.testTag("nav_tab_tasks")
                )
                NavigationBarItem(
                    selected = state.currentTab == NavTab.PROGRESS,
                    onClick = { viewModel.setNavTab(NavTab.PROGRESS) },
                    icon = {
                        Icon(
                            if (state.currentTab == NavTab.PROGRESS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            contentDescription = strings.progress
                        )
                    },
                    label = { Text(strings.progress) },
                    modifier = Modifier.testTag("nav_tab_progress")
                )
                NavigationBarItem(
                    selected = state.currentTab == NavTab.SETTINGS,
                    onClick = { viewModel.setNavTab(NavTab.SETTINGS) },
                    icon = {
                        Icon(
                            if (state.currentTab == NavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = strings.settings
                        )
                    },
                    label = { Text(strings.settings) },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        floatingActionButton = {
            // Floating + Button on Calendar & Tasks tab
            if (state.currentTab == NavTab.CALENDAR || state.currentTab == NavTab.TASKS) {
                FloatingActionButton(
                    onClick = {
                        if (state.currentTab == NavTab.TASKS) {
                            viewModel.openAddTask()
                        } else {
                            viewModel.openAddEvent()
                        }
                    },
                    modifier = Modifier.testTag("fab_add")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        }
    ) { innerPadding ->
        when (state.currentTab) {
            NavTab.CALENDAR -> {
                CalendarScreen(
                    state = state,
                    strings = strings,
                    onSelectDate = { viewModel.selectDate(it) },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onGoToToday = { viewModel.goToToday() },
                    onChangeViewMode = { viewModel.setCalendarViewMode(it) },
                    onOpenSearch = { viewModel.setSearchOpen(true) },
                    onOpenAddEvent = { viewModel.openAddEvent(it) },
                    onOpenEditEvent = { viewModel.openEditEvent(it) },
                    onDeleteEvent = { viewModel.deleteEvent(it) },
                    onToggleEventCompleted = { viewModel.toggleEventCompleted(it) },
                    onOpenAddTask = { viewModel.openAddTask(it) },
                    onOpenEditTask = { viewModel.openEditTask(it) },
                    onToggleTaskCompleted = { viewModel.toggleTaskCompleted(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onOpenAccount = { viewModel.openAuthDialog() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            NavTab.TASKS -> {
                TasksScreen(
                    state = state,
                    strings = strings,
                    onFilterChange = { viewModel.setTaskFilter(it) },
                    onToggleTaskComplete = { viewModel.toggleTaskCompleted(it) },
                    onQuickAddTask = { viewModel.quickAddTask(it) },
                    onOpenAddTask = { viewModel.openAddTask(it) },
                    onOpenEditTask = { viewModel.openEditTask(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onRescheduleTask = { task, newDate -> viewModel.rescheduleTask(task, newDate) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            NavTab.PROGRESS -> {
                ProgressScreen(
                    state = state,
                    strings = strings,
                    onOpenAddEvent = { viewModel.openAddEvent(it) },
                    onOpenEditEvent = { viewModel.openEditEvent(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            NavTab.SETTINGS -> {
                SettingsScreen(
                    state = state,
                    strings = strings,
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onFirstDayOfWeekChange = { viewModel.setFirstDayOfWeek(it) },
                    on24HourChange = { viewModel.set24Hour(it) },
                    onStudentModeToggle = { viewModel.setStudentModeEnabled(it) },
                    onExportData = { viewModel.exportDataToJson() },
                    onImportData = { viewModel.importDataFromJson(it) },
                    onClearAllData = { viewModel.clearAllData() },
                    onOpenAuthDialog = { viewModel.openAuthDialog() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // Add / Edit Event Dialog
    if (state.isAddEventOpen) {
        AddEditEventDialog(
            initialDate = state.selectedDate,
            eventToEdit = state.eventToEdit,
            strings = strings,
            is24Hour = state.is24Hour,
            onDismiss = {
                viewModel.closeAddEditEvent()
                Modifier
            },
            onSave = { title, date, startTime, endTime, category, reminderMinutes, repeatType, notes, isImportant, colorHex ->
                viewModel.saveEvent(
                    title = title,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    category = category,
                    reminderMinutes = reminderMinutes,
                    repeatType = repeatType,
                    notes = notes,
                    isImportant = isImportant,
                    colorHex = colorHex
                )
            }
        )
    }

    // Add / Edit Task Dialog
    if (state.isAddTaskOpen) {
        AddEditTaskDialog(
            initialDate = state.selectedDate,
            taskToEdit = state.taskToEdit,
            strings = strings,
            onDismiss = { viewModel.closeAddEditTask() },
            onSave = { title, date, category, priority, reminderTime, notes ->
                viewModel.saveTask(
                    title = title,
                    date = date,
                    category = category,
                    priority = priority,
                    reminderTime = reminderTime,
                    notes = notes
                )
            }
        )
    }

    // Search Dialog
    if (state.isSearchOpen) {
        SearchDialog(
            allEvents = state.allEvents,
            allTasks = state.allTasks,
            strings = strings,
            onDismiss = { viewModel.setSearchOpen(false) },
            onSelectDate = {
                viewModel.selectDate(it)
                viewModel.setNavTab(NavTab.CALENDAR)
            },
            onOpenEvent = { event ->
                viewModel.openEditEvent(event)
            }
        )
    }

    // Authentication Dialog
    if (state.isAuthDialogOpen) {
        AuthDialog(
            authUser = state.authUser,
            strings = strings,
            onDismiss = { viewModel.dismissAuthDialog() },
            onSignIn = { email, pass, onResult ->
                viewModel.signInWithEmail(email, pass, onResult)
            },
            onSignUp = { email, pass, name, onResult ->
                viewModel.signUpWithEmail(email, pass, name, onResult)
            },
            onSignInAnonymously = { onResult ->
                viewModel.signInAnonymously(onResult)
            },
            onSendPasswordReset = { email, onResult ->
                viewModel.sendPasswordReset(email, onResult)
            },
            onSignOut = {
                viewModel.signOut()
                viewModel.dismissAuthDialog()
            }
        )
    }
}
