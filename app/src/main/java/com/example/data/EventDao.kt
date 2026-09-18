package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC, startTime ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE date = :date ORDER BY startTime ASC")
    fun getEventsByDate(date: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC, startTime ASC")
    fun getEventsForMonth(monthPrefix: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE isImportant = 1 AND date >= :fromDate ORDER BY date ASC, startTime ASC")
    fun getUpcomingImportantEvents(fromDate: String): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events 
        WHERE title LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
        ORDER BY date ASC, startTime ASC
    """)
    fun searchEvents(query: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
