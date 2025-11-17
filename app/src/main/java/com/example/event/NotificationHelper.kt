package com.example.event

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "event_reminder_channel"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use default notification sound
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Event Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for your upcoming events"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleEventNotification(event: Event, minutesBefore: Int = 15) {
        val alarmTime = event.timeMillis - (minutesBefore * 60 * 1000)

        // Only schedule if the event is in the future
        if (alarmTime > System.currentTimeMillis()) {
            val alarmHelper = AlarmHelper(context)
            alarmHelper.scheduleAlarm(event, alarmTime, minutesBefore)

            // Debug log
            val timeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            println("🔔 Scheduled notification for: ${event.title} at ${timeFormat.format(Date(alarmTime))}")
        } else {
            println("⚠️ Event is in the past, not scheduling: ${event.title}")
        }
    }

    fun showNotification(event: Event, minutesBefore: Int = 15) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeString = timeFormat.format(Date(event.timeMillis))

        // Use default notification sound
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔔 Event Reminder")
            .setContentText("${event.title} at $timeString")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // This includes sound, vibration, and lights
            .setSound(soundUri)
            .setVibrate(longArrayOf(1000, 1000, 1000)) // Vibrate pattern
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(event.id.toInt(), notification)

        // Debug log
        println("🔔 Notification shown for: ${event.title}")
    }

    fun cancelNotification(eventId: Long) {
        val alarmHelper = AlarmHelper(context)
        alarmHelper.cancelAlarm(eventId)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(eventId.toInt())
    }
}