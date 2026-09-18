package com.example.ui

import com.example.data.EventEntity
import com.example.data.TaskEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

enum class NavTab {
    CALENDAR,
    TASKS,
    PROGRESS,
    SETTINGS
}

enum class CalendarViewMode {
    MONTH,
    WEEK,
    DAY
}

enum class TaskFilter {
    ALL,
    TODAY,
    UPCOMING,
    COMPLETED
}

data class AuthUserState(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val isAnonymous: Boolean = false,
    val isAuthenticated: Boolean = false
)

data class DayPlanUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val calendarViewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val currentTab: NavTab = NavTab.CALENDAR,
    val allEvents: List<EventEntity> = emptyList(),
    val allTasks: List<TaskEntity> = emptyList(),
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val isAddEventOpen: Boolean = false,
    val eventToEdit: EventEntity? = null,
    val isAddTaskOpen: Boolean = false,
    val taskToEdit: TaskEntity? = null,
    val isAuthDialogOpen: Boolean = false,
    val authUser: AuthUserState = AuthUserState(),
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val is24Hour: Boolean = false,
    val studentModeEnabled: Boolean = true,
    val taskFilter: TaskFilter = TaskFilter.ALL,
    val selectedSubjectCategory: String? = null,
    val userMessage: String? = null
)
