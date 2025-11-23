package com.example.event

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "event_reminder_channel"
        const val CHANNEL_NAME = "Event Reminders"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for event reminders"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        // Check if notifications are enabled in app settings
        val sharedPrefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)

        if (!notificationsEnabled) {
            return false
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showEventReminder(event: Event) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        val eventDate = dateFormat.format(Date(event.dateMillis))
        val eventTime = timeFormat.format(Date(event.timeMillis))

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event_notification)
            .setContentTitle("Event Reminder: ${event.title}")
            .setContentText("${eventDate} at ${eventTime}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${event.description}\n\nWhen: ${eventDate} at ${eventTime}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(NOTIFICATION_ID + event.hashCode(), notification)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showUpcomingEventReminder(event: Event, minutesBefore: Int) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val eventTime = timeFormat.format(Date(event.timeMillis))

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event_notification)
            .setContentTitle("Event Starting Soon")
            .setContentText("${event.title} in $minutesBefore minutes")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${event.title} starts at ${eventTime}\n\n${event.description}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(NOTIFICATION_ID + event.hashCode() + 1, notification)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showEventStartNotification(event: Event) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val eventTime = timeFormat.format(Date(event.timeMillis))

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event_notification)
            .setContentTitle("Event Started: ${event.title}")
            .setContentText("Your event has started now")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${event.title} started at ${eventTime}\n\n${event.description}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(NOTIFICATION_ID + event.hashCode() + 2, notification)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showEventEndNotification(event: Event) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val eventTime = timeFormat.format(Date(event.endTimeMillis))

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event_notification)
            .setContentTitle("Event Ended: ${event.title}")
            .setContentText("Your event has ended")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${event.title} ended at ${eventTime}\n\n${event.description}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(NOTIFICATION_ID + event.hashCode() + 3, notification)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}