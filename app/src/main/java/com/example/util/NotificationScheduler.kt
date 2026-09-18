package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.EventEntity
import com.example.receiver.AlarmReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationScheduler {

    fun scheduleEventReminder(context: Context, event: EventEntity) {
        if (event.reminderMinutes <= 0) return

        try {
            val date = LocalDate.parse(event.date)
            val time = LocalTime.parse(event.startTime)
            val eventDateTime = LocalDateTime.of(date, time)
            val triggerDateTime = eventDateTime.minusMinutes(event.reminderMinutes.toLong())
            val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (triggerMillis <= System.currentTimeMillis()) {
                return // in the past
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_ALARM_TRIGGER
                putExtra(AlarmReceiver.EXTRA_EVENT_ID, event.id)
                putExtra(AlarmReceiver.EXTRA_EVENT_TITLE, event.title)
                putExtra(AlarmReceiver.EXTRA_REMINDER_MINUTES, event.reminderMinutes)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                event.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    try {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                    } catch (se: SecurityException) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                    }
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scheduleSnooze(context: Context, eventId: Long, title: String, snoozeMinutes: Int) {
        try {
            val triggerMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_ALARM_TRIGGER
                putExtra(AlarmReceiver.EXTRA_EVENT_ID, eventId)
                putExtra(AlarmReceiver.EXTRA_EVENT_TITLE, title)
                putExtra(AlarmReceiver.EXTRA_REMINDER_MINUTES, 0)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                eventId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelEventReminder(context: Context, eventId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                eventId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
