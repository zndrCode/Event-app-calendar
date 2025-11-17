package com.example.event

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class EventWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return EventRemoteViewsFactory(this.applicationContext)
    }
}

class EventRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private val events = mutableListOf<Event>()

    override fun onCreate() {
        // Initialize data source
    }

    override fun onDataSetChanged() {
        // Reload events when data changes
        loadEvents()
    }

    override fun onDestroy() {
        events.clear()
    }

    override fun getCount(): Int = events.size

    override fun getViewAt(position: Int): RemoteViews {
        val event = events[position]
        val views = RemoteViews(context.packageName, R.layout.widget_event_item)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = timeFormat.format(Date(event.timeMillis))

        views.setTextViewText(R.id.widget_event_time, timeString)
        views.setTextViewText(R.id.widget_event_title, event.title)

        // Set click intent
        val intent = Intent(context, HomeActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            position,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_event_item, pendingIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = events[position].id

    override fun hasStableIds(): Boolean = true

    private fun loadEvents() {
        events.clear()
        val sharedPref = context.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("event_list", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Event>>() {}.type
            val loadedEvents: MutableList<Event> = Gson().fromJson(json, type)
            events.addAll(loadedEvents)

            // Filter for today's events and sort by time
            val todayEvents = getTodaysEvents(events)
            events.clear()
            events.addAll(todayEvents)
        }
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