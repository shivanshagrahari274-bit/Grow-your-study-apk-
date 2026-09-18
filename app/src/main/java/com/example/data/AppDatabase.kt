package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(entities = [EventEntity::class, TaskEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dayplan_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with sample PRD items
                            CoroutineScope(Dispatchers.IO).launch {
                                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val nextWeek = LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE)

                                val eventDao = getInstance(context).eventDao()
                                val taskDao = getInstance(context).taskDao()

                                eventDao.insertEvent(
                                    EventEntity(
                                        title = "Physics Revision",
                                        date = today,
                                        startTime = "18:00",
                                        endTime = "19:00",
                                        category = "Study",
                                        reminderMinutes = 15,
                                        repeatType = "Daily",
                                        notes = "NLM ke questions solve karne hain.",
                                        isImportant = true,
                                        colorHex = 0xFF3F51B5
                                    )
                                )
                                eventDao.insertEvent(
                                    EventEntity(
                                        title = "Chemistry Organic Chemistry",
                                        date = today,
                                        startTime = "19:15",
                                        endTime = "20:15",
                                        category = "Study",
                                        reminderMinutes = 15,
                                        repeatType = "Daily",
                                        notes = "Reaction mechanisms and IUPAC practice",
                                        isImportant = false,
                                        colorHex = 0xFF00897B
                                    )
                                )
                                eventDao.insertEvent(
                                    EventEntity(
                                        title = "Maths Lecture & Practice",
                                        date = today,
                                        startTime = "21:00",
                                        endTime = "22:00",
                                        category = "Study",
                                        reminderMinutes = 15,
                                        repeatType = "None",
                                        notes = "Calculus integration problems",
                                        isImportant = false,
                                        colorHex = 0xFFE64A19
                                    )
                                )
                                eventDao.insertEvent(
                                    EventEntity(
                                        title = "Physics Weekly Mock Test",
                                        date = nextWeek,
                                        startTime = "10:00",
                                        endTime = "13:00",
                                        category = "Exam",
                                        reminderMinutes = 60,
                                        repeatType = "Weekly",
                                        notes = "Full syllabus Chapter 1-5 test",
                                        isImportant = true,
                                        colorHex = 0xFFD32F2F
                                    )
                                )

                                taskDao.insertTask(
                                    TaskEntity(
                                        title = "Physics DPP 04",
                                        date = today,
                                        isCompleted = false,
                                        category = "DPP",
                                        priority = "HIGH",
                                        notes = "15 questions on Newton's Laws"
                                    )
                                )
                                taskDao.insertTask(
                                    TaskEntity(
                                        title = "Chemistry revision notes",
                                        date = today,
                                        isCompleted = false,
                                        category = "Revision",
                                        priority = "MEDIUM"
                                    )
                                )
                                taskDao.insertTask(
                                    TaskEntity(
                                        title = "Maths Lecture 12",
                                        date = today,
                                        isCompleted = true,
                                        category = "Lecture",
                                        priority = "HIGH"
                                    )
                                )
                                taskDao.insertTask(
                                    TaskEntity(
                                        title = "English chapter summary",
                                        date = today,
                                        isCompleted = false,
                                        category = "Homework",
                                        priority = "LOW"
                                    )
                                )
                                taskDao.insertTask(
                                    TaskEntity(
                                        title = "Submit Assignment & DPP",
                                        date = tomorrow,
                                        isCompleted = false,
                                        category = "Homework",
                                        priority = "HIGH"
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
