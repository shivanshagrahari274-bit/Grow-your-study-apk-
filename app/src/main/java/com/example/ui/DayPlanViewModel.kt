package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthRepository
import com.example.data.AppDatabase
import com.example.data.DayPlanRepository
import com.example.data.EventEntity
import com.example.data.TaskEntity
import com.example.util.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DayPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DayPlanRepository
    private val authRepository: AuthRepository = AuthRepository()
    private val _uiState = MutableStateFlow(DayPlanUiState())
    val uiState: StateFlow<DayPlanUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = DayPlanRepository(db.eventDao(), db.taskDao())

        // Load saved settings from SharedPreferences
        val prefs = application.getSharedPreferences("dayplan_prefs", Application.MODE_PRIVATE)
        val langStr = prefs.getString("language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
        val themeMode = prefs.getString("themeMode", "SYSTEM") ?: "SYSTEM"
        val firstDay = prefs.getInt("firstDayOfWeek", DayOfWeek.MONDAY.value)
        val is24H = prefs.getBoolean("is24Hour", false)
        val studentMode = prefs.getBoolean("studentModeEnabled", true)

        _uiState.update {
            it.copy(
                language = try { AppLanguage.valueOf(langStr) } catch (_: Exception) { AppLanguage.ENGLISH },
                themeMode = themeMode,
                firstDayOfWeek = DayOfWeek.of(firstDay),
                is24Hour = is24H,
                studentModeEnabled = studentMode
            )
        }

        // Collect events and tasks reactively
        viewModelScope.launch {
            combine(repository.allEvents, repository.allTasks) { events, tasks ->
                Pair(events, tasks)
            }.collect { (events, tasks) ->
                _uiState.update {
                    it.copy(allEvents = events, allTasks = tasks)
                }
            }
        }

        // Collect Firebase Auth state reactively
        viewModelScope.launch {
            authRepository.authStateFlow.collect { firebaseUser ->
                _uiState.update {
                    it.copy(
                        authUser = AuthUserState(
                            uid = firebaseUser?.uid ?: "",
                            email = firebaseUser?.email,
                            displayName = firebaseUser?.displayName,
                            isAnonymous = firebaseUser?.isAnonymous ?: false,
                            isAuthenticated = firebaseUser != null
                        )
                    )
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                currentMonth = YearMonth.of(date.year, date.month)
            )
        }
    }

    fun nextMonth() {
        _uiState.update {
            val next = it.currentMonth.plusMonths(1)
            it.copy(currentMonth = next)
        }
    }

    fun previousMonth() {
        _uiState.update {
            val prev = it.currentMonth.minusMonths(1)
            it.copy(currentMonth = prev)
        }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                selectedDate = today,
                currentMonth = YearMonth.of(today.year, today.month)
            )
        }
    }

    fun setCalendarViewMode(mode: CalendarViewMode) {
        _uiState.update { it.copy(calendarViewMode = mode) }
    }

    fun setNavTab(tab: NavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setTaskFilter(filter: TaskFilter) {
        _uiState.update { it.copy(taskFilter = filter) }
    }

    fun setSubjectCategoryFilter(subject: String?) {
        _uiState.update { it.copy(selectedSubjectCategory = subject) }
    }

    fun openAddEvent(initialDate: LocalDate = _uiState.value.selectedDate) {
        _uiState.update {
            it.copy(
                isAddEventOpen = true,
                eventToEdit = null,
                selectedDate = initialDate
            )
        }
    }

    fun openEditEvent(event: EventEntity) {
        _uiState.update {
            it.copy(
                isAddEventOpen = true,
                eventToEdit = event
            )
        }
    }

    fun closeAddEditEvent() {
        _uiState.update {
            it.copy(
                isAddEventOpen = false,
                eventToEdit = null
            )
        }
    }

    fun saveEvent(
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
    ) {
        viewModelScope.launch {
            val currentEdit = _uiState.value.eventToEdit
            if (currentEdit != null) {
                val updated = currentEdit.copy(
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
                repository.updateEvent(updated)
                NotificationScheduler.scheduleEventReminder(getApplication(), updated)
            } else {
                val newEvent = EventEntity(
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
                val id = repository.insertEvent(newEvent)
                val savedEvent = newEvent.copy(id = id)
                NotificationScheduler.scheduleEventReminder(getApplication(), savedEvent)
            }
            closeAddEditEvent()
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            NotificationScheduler.cancelEventReminder(getApplication(), event.id)
            repository.deleteEvent(event)
        }
    }

    fun toggleEventCompleted(event: EventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event.copy(isCompleted = !event.isCompleted))
        }
    }

    fun openAddTask(initialDate: LocalDate = _uiState.value.selectedDate) {
        _uiState.update {
            it.copy(
                isAddTaskOpen = true,
                taskToEdit = null,
                selectedDate = initialDate
            )
        }
    }

    fun openEditTask(task: TaskEntity) {
        _uiState.update {
            it.copy(
                isAddTaskOpen = true,
                taskToEdit = task
            )
        }
    }

    fun closeAddEditTask() {
        _uiState.update {
            it.copy(
                isAddTaskOpen = false,
                taskToEdit = null
            )
        }
    }

    fun saveTask(
        title: String,
        date: String,
        category: String,
        priority: String,
        reminderTime: String?,
        notes: String
    ) {
        viewModelScope.launch {
            val currentEdit = _uiState.value.taskToEdit
            if (currentEdit != null) {
                val updated = currentEdit.copy(
                    title = title,
                    date = date,
                    category = category,
                    priority = priority,
                    reminderTime = reminderTime,
                    notes = notes
                )
                repository.updateTask(updated)
            } else {
                val newTask = TaskEntity(
                    title = title,
                    date = date,
                    isCompleted = false,
                    category = category,
                    priority = priority,
                    reminderTime = reminderTime,
                    notes = notes
                )
                repository.insertTask(newTask)
            }
            closeAddEditTask()
        }
    }

    fun quickAddTask(title: String, date: String = _uiState.value.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newTask = TaskEntity(
                title = title.trim(),
                date = date,
                isCompleted = false,
                category = "General",
                priority = "MEDIUM"
            )
            repository.insertTask(newTask)
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun rescheduleTask(task: TaskEntity, newDate: LocalDate) {
        viewModelScope.launch {
            val updated = task.copy(date = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            repository.updateTask(updated)
        }
    }

    fun setSearchOpen(isOpen: Boolean) {
        _uiState.update {
            it.copy(
                isSearchOpen = isOpen,
                searchQuery = if (!isOpen) "" else it.searchQuery
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        val prefs = getApplication<Application>().getSharedPreferences("dayplan_prefs", Application.MODE_PRIVATE)
        prefs.edit().putString("language", language.name).apply()
    }

    fun setThemeMode(themeMode: String) {
        _uiState.update { it.copy(themeMode = themeMode) }
        val prefs = getApplication<Application>().getSharedPreferences("dayplan_prefs", Application.MODE_PRIVATE)
        prefs.edit().putString("themeMode", themeMode).apply()
    }

    fun setFirstDayOfWeek(day: DayOfWeek) {
        _uiState.update { it.copy(firstDayOfWeek = day) }
        val prefs = getApplication<Application>().getSharedPreferences("dayplan_prefs", Application.MODE_PRIVATE)
        prefs.edit().putInt("firstDayOfWeek", day.value).apply()
    }

    fun set24Hour(is24H: Boolean) {
        _uiState.update { it.copy(is24Hour = is24H) }
        val prefs = getApplication<Application>().getSharedPreferences("dayplan_prefs", Application.MODE_PRIVATE)
        prefs.edit().putBoolean("is24Hour", is24H).apply()
    }

    fun setStudentModeEnabled(enabled: Boolean) {
        _uiState.update { it.copy(studentModeEnabled = enabled) }
        val prefs = getApplication<Application>().getSharedPreferences("dayplan_prefs", Application.MODE_PRIVATE)
        prefs.edit().putBoolean("studentModeEnabled", enabled).apply()
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _uiState.update { it.copy(userMessage = "All calendar events and tasks cleared") }
        }
    }

    fun exportDataToJson(): String {
        val state = _uiState.value
        val root = JSONObject()

        val eventsArray = JSONArray()
        for (event in state.allEvents) {
            val obj = JSONObject().apply {
                put("title", event.title)
                put("date", event.date)
                put("startTime", event.startTime)
                put("endTime", event.endTime)
                put("category", event.category)
                put("reminderMinutes", event.reminderMinutes)
                put("repeatType", event.repeatType)
                put("notes", event.notes)
                put("isImportant", event.isImportant)
                put("isCompleted", event.isCompleted)
                put("colorHex", event.colorHex)
            }
            eventsArray.put(obj)
        }
        root.put("events", eventsArray)

        val tasksArray = JSONArray()
        for (task in state.allTasks) {
            val obj = JSONObject().apply {
                put("title", task.title)
                put("date", task.date)
                put("isCompleted", task.isCompleted)
                put("category", task.category)
                put("priority", task.priority)
                put("reminderTime", task.reminderTime ?: "")
                put("notes", task.notes)
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)
        root.put("exportedAt", System.currentTimeMillis())

        return root.toString(2)
    }

    fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            val eventsArray = root.optJSONArray("events")
            val tasksArray = root.optJSONArray("tasks")

            viewModelScope.launch {
                if (eventsArray != null) {
                    for (i in 0 until eventsArray.length()) {
                        val obj = eventsArray.getJSONObject(i)
                        val event = EventEntity(
                            title = obj.getString("title"),
                            date = obj.getString("date"),
                            startTime = obj.optString("startTime", "09:00"),
                            endTime = obj.optString("endTime", "10:00"),
                            category = obj.optString("category", "Study"),
                            reminderMinutes = obj.optInt("reminderMinutes", 15),
                            repeatType = obj.optString("repeatType", "NONE"),
                            notes = obj.optString("notes", ""),
                            isImportant = obj.optBoolean("isImportant", false),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            colorHex = obj.optLong("colorHex", 0xFF3F51B5)
                        )
                        repository.insertEvent(event)
                    }
                }

                if (tasksArray != null) {
                    for (i in 0 until tasksArray.length()) {
                        val obj = tasksArray.getJSONObject(i)
                        val task = TaskEntity(
                            title = obj.getString("title"),
                            date = obj.getString("date"),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            category = obj.optString("category", "General"),
                            priority = obj.optString("priority", "MEDIUM"),
                            reminderTime = obj.optString("reminderTime").ifEmpty { null },
                            notes = obj.optString("notes", "")
                        )
                        repository.insertTask(task)
                    }
                }
                _uiState.update { it.copy(userMessage = "Data imported successfully!") }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(userMessage = "Error importing data: invalid format") }
            false
        }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // --- Authentication Actions ---

    fun openAuthDialog() {
        _uiState.update { it.copy(isAuthDialogOpen = true) }
    }

    fun dismissAuthDialog() {
        _uiState.update { it.copy(isAuthDialogOpen = false) }
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, pass)
            result.onSuccess { user ->
                val identifier = user.displayName?.ifBlank { null } ?: user.email ?: "Account"
                _uiState.update {
                    it.copy(
                        isAuthDialogOpen = false,
                        userMessage = "Signed in as $identifier"
                    )
                }
                onResult(true, null)
            }.onFailure { e ->
                onResult(false, e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, displayName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, pass, displayName)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isAuthDialogOpen = false,
                        userMessage = "Account created successfully!"
                    )
                }
                onResult(true, null)
            }.onFailure { e ->
                onResult(false, e.localizedMessage ?: "Sign up failed")
            }
        }
    }

    fun signInAnonymously(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signInAnonymously()
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isAuthDialogOpen = false,
                        userMessage = "Signed in as Guest"
                    )
                }
                onResult(true, null)
            }.onFailure { e ->
                onResult(false, e.localizedMessage ?: "Guest sign-in failed")
            }
        }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            result.onSuccess {
                onResult(true, null)
            }.onFailure { e ->
                onResult(false, e.localizedMessage ?: "Failed to send reset email")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.update { it.copy(userMessage = "Signed out successfully") }
    }
}
