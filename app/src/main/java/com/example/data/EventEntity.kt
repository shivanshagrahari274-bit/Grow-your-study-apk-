package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // Format: YYYY-MM-DD
    val startTime: String, // Format: HH:mm (24h internal storage)
    val endTime: String, // Format: HH:mm (24h internal storage)
    val category: String, // e.g. "Study", "School", "Exam", "Personal", "Important", "Other"
    val reminderMinutes: Int = 15, // 0 = No reminder, 5, 15, 30, 60, 1440
    val repeatType: String = "NONE", // "NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val notes: String = "",
    val isImportant: Boolean = false,
    val isCompleted: Boolean = false,
    val colorHex: Long = 0xFF3F51B5
)
