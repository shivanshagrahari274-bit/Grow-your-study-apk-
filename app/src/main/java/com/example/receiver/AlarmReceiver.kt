package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        val eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Upcoming Event"
        val reminderMinutes = intent.getIntExtra(EXTRA_REMINDER_MINUTES, 15)

        when (action) {
            ACTION_MARK_COMPLETE -> {
                if (eventId != -1L) {
                    val db = AppDatabase.getInstance(context)
                    CoroutineScope(Dispatchers.IO).launch {
                        val event = db.eventDao().getEventById(eventId)
                        if (event != null) {
                            db.eventDao().updateEvent(event.copy(isCompleted = true))
                        }
                    }
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(eventId.toInt())
                }
            }
            ACTION_SNOOZE -> {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(eventId.toInt())
                // Re-schedule in 10 minutes
                com.example.util.NotificationScheduler.scheduleSnooze(context, eventId, eventTitle, 10)
            }
            else -> {
                // Show Notification
                showNotification(context, eventId, eventTitle, reminderMinutes)
            }
        }
    }

    private fun showNotification(
        context: Context,
        eventId: Long,
        title: String,
        reminderMinutes: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "dayplan_event_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Event & Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled calendar events, study sessions, and tasks"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Complete Action Intent
        val completeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_MARK_COMPLETE
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (eventId + 10000).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action Intent
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_EVENT_ID, eventId)
            putExtra(EXTRA_EVENT_TITLE, title)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (eventId + 20000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = if (reminderMinutes > 0) {
            "$reminderMinutes minutes mein start hone wala hai."
        } else {
            "Time to start your scheduled activity!"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔔 $title")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("🔔 $title\n$bodyText"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Open", openPendingIntent)
            .addAction(0, "Mark Complete", completePendingIntent)
            .addAction(0, "Snooze 10m", snoozePendingIntent)
            .build()

        notificationManager.notify(eventId.toInt().coerceAtLeast(1), notification)
    }

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.example.ACTION_ALARM_TRIGGER"
        const val ACTION_MARK_COMPLETE = "com.example.ACTION_MARK_COMPLETE"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"

        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_TITLE = "extra_event_title"
        const val EXTRA_REMINDER_MINUTES = "extra_reminder_minutes"
    }
}
