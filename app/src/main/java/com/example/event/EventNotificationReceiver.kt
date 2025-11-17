package com.example.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.Gson

class EventNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventJson = intent.getStringExtra("event_data")
        val minutesBefore = intent.getIntExtra("minutes_before", 15)

        eventJson?.let {
            val event = Gson().fromJson(it, Event::class.java)
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showNotification(event, minutesBefore)
        }
    }
}