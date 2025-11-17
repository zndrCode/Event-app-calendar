package com.example.event

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class EventWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, EventWidget::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.event_widget)

            // Set up click intent to open app
            val intent = Intent(context, HomeActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

            // Get events
            val sharedPref = context.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)
            val json = sharedPref.getString("event_list", null)
            val events = if (json != null) {
                val type = object : TypeToken<MutableList<Event>>() {}.type
                Gson().fromJson<MutableList<Event>>(json, type) ?: mutableListOf()
            } else {
                mutableListOf()
            }

            // Get today's events
            val todayEvents = getTodaysEvents(events)

            if (todayEvents.isNotEmpty()) {
                views.setTextViewText(R.id.widget_title, "Today (${todayEvents.size})")
                val eventsText = todayEvents.take(3).joinToString("\n") { event ->
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.timeMillis))
                    "• $time - ${event.title}"
                }
                views.setTextViewText(R.id.widget_events, eventsText)
            } else {
                views.setTextViewText(R.id.widget_title, "Eventra")
                views.setTextViewText(R.id.widget_events, "No events today")
            }

            // Update refresh button
            val refreshIntent = Intent(context, EventWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

            // Update add event button
            val addIntent = Intent(context, HomeActivity::class.java).apply {
                putExtra("open_add_dialog", true)
            }
            val addPendingIntent = PendingIntent.getActivity(
                context,
                1,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_add, addPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getTodaysEvents(events: List<Event>): List<Event> {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val tomorrow = Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }

            return events.filter { event ->
                event.timeMillis >= today.timeInMillis && event.timeMillis < tomorrow.timeInMillis
            }.sortedBy { it.timeMillis }
        }
    }
}