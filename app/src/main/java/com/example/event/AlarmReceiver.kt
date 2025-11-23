package com.example.event

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.gson.Gson

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventJson = intent.getStringExtra("event")

        eventJson?.let { json ->
            val event = Gson().fromJson(json, Event::class.java)
            val notificationHelper = NotificationHelper(context)

            when (intent.action) {
                "EVENT_REMINDER" -> {
                    val reminderType = intent.getStringExtra("reminder_type")
                    when (reminderType) {
                        "exact_time" -> notificationHelper.showEventReminder(event)
                        "15_minutes_before" -> notificationHelper.showUpcomingEventReminder(event, 15)
                        "30_minutes_before" -> notificationHelper.showUpcomingEventReminder(event, 30)
                        "1_hour_before" -> notificationHelper.showUpcomingEventReminder(event, 60)
                    }
                }
                "EVENT_START" -> {
                    notificationHelper.showEventStartNotification(event)
                }
                "EVENT_END" -> {
                    notificationHelper.showEventEndNotification(event)
                }
            }
        }
    }

    companion object {
        fun scheduleEventReminder(context: Context, event: Event, reminderMinutes: Int = 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "EVENT_REMINDER"
                putExtra("event", Gson().toJson(event))
                putExtra("reminder_type", when (reminderMinutes) {
                    0 -> "exact_time"
                    15 -> "15_minutes_before"
                    30 -> "30_minutes_before"
                    60 -> "1_hour_before"
                    else -> "exact_time"
                })
            }

            val requestCode = event.hashCode() + reminderMinutes
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate trigger time (event time minus reminder minutes)
            val triggerTime = event.timeMillis - (reminderMinutes * 60 * 1000)

            // Schedule the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }

        fun scheduleEventStartNotification(context: Context, event: Event) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "EVENT_START"
                putExtra("event", Gson().toJson(event))
            }

            val requestCode = event.hashCode() + 1000
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule for event start time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    event.timeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    event.timeMillis,
                    pendingIntent
                )
            }
        }

        fun scheduleEventEndNotification(context: Context, event: Event) {
            // Only schedule end notification if it's not an all-day event and has an end time
            if (event.isAllDay || event.endTimeMillis == 0L) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "EVENT_END"
                putExtra("event", Gson().toJson(event))
            }

            val requestCode = event.hashCode() + 2000
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule for event end time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    event.endTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    event.endTimeMillis,
                    pendingIntent
                )
            }
        }

        fun cancelAllEventNotifications(context: Context, event: Event) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Cancel all reminder types
            listOf(0, 15, 30, 60).forEach { minutes ->
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    action = "EVENT_REMINDER"
                    putExtra("event", Gson().toJson(event))
                }
                val requestCode = event.hashCode() + minutes
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }

            // Cancel start notification
            val startIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "EVENT_START"
                putExtra("event", Gson().toJson(event))
            }
            val startRequestCode = event.hashCode() + 1000
            val startPendingIntent = PendingIntent.getBroadcast(
                context,
                startRequestCode,
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(startPendingIntent)

            // Cancel end notification
            val endIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "EVENT_END"
                putExtra("event", Gson().toJson(event))
            }
            val endRequestCode = event.hashCode() + 2000
            val endPendingIntent = PendingIntent.getBroadcast(
                context,
                endRequestCode,
                endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(endPendingIntent)
        }

        // Keep the old cancel method for backward compatibility
        fun cancelEventReminder(context: Context, event: Event) {
            cancelAllEventNotifications(context, event)
        }
    }
}