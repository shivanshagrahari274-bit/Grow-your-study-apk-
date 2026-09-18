package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // Format: YYYY-MM-DD
    val isCompleted: Boolean = false,
    val category: String = "General", // "DPP", "Homework", "Revision", "Test", "Lecture", "General"
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val reminderTime: String? = null, // Format: HH:mm
    val notes: String = ""
)
