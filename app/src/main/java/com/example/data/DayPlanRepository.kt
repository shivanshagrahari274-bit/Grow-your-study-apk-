package com.example.data

import kotlinx.coroutines.flow.Flow

class DayPlanRepository(
    private val eventDao: EventDao,
    private val taskDao: TaskDao
) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getEventsByDate(date: String): Flow<List<EventEntity>> = eventDao.getEventsByDate(date)

    fun getTasksByDate(date: String): Flow<List<TaskEntity>> = taskDao.getTasksByDate(date)

    fun getEventsForMonth(monthPrefix: String): Flow<List<EventEntity>> =
        eventDao.getEventsForMonth(monthPrefix)

    fun getTasksForMonth(monthPrefix: String): Flow<List<TaskEntity>> =
        taskDao.getTasksForMonth(monthPrefix)

    fun getUpcomingImportantEvents(fromDate: String): Flow<List<EventEntity>> =
        eventDao.getUpcomingImportantEvents(fromDate)

    fun searchEvents(query: String): Flow<List<EventEntity>> = eventDao.searchEvents(query)

    fun searchTasks(query: String): Flow<List<TaskEntity>> = taskDao.searchTasks(query)

    suspend fun getEventById(id: Long): EventEntity? = eventDao.getEventById(id)

    suspend fun insertEvent(event: EventEntity): Long = eventDao.insertEvent(event)

    suspend fun updateEvent(event: EventEntity) = eventDao.updateEvent(event)

    suspend fun deleteEvent(event: EventEntity) = eventDao.deleteEvent(event)

    suspend fun deleteEventById(id: Long) = eventDao.deleteEventById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean) =
        taskDao.setTaskCompleted(id, isCompleted)

    suspend fun clearAllData() {
        eventDao.deleteAllEvents()
        taskDao.deleteAllTasks()
    }
}
