package com.example.event

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: View
    private lateinit var navHome: LinearLayout
    private lateinit var navSettings: LinearLayout
    private lateinit var textEmpty: TextView

    private lateinit var eventAdapter: EventAdapter
    private val events = mutableListOf<Event>()
    private var selectedDateMillis: Long = 0L

    companion object {
        private const val ADD_EVENT_REQUEST_CODE = 1001
        private const val EDIT_EVENT_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.rvEvents)
        fabAdd = findViewById(R.id.fabAdd)
        navHome = findViewById(R.id.navHome)
        navSettings = findViewById(R.id.navSettings)
        textEmpty = findViewById(R.id.textEmpty)

        // RecyclerView setup
        recyclerView.layoutManager = LinearLayoutManager(this)
        eventAdapter = EventAdapter(
            events,
            onEditClick = { event -> editEvent(event) },
            onDeleteClick = { event -> deleteEvent(event) }
        )
        recyclerView.adapter = eventAdapter

        // Load saved events
        loadEvents()

        // Default date (today)
        val today = Calendar.getInstance()
        selectedDateMillis = today.timeInMillis
        calendarView.setDate(today)
        filterEventsByDate()

        // Highlight dates with events
        highlightDatesWithEvents()

        // When user selects a date
        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val calendar = eventDay.calendar
                selectedDateMillis = calendar.timeInMillis
                filterEventsByDate()
            }
        })

        // Add button popup
        fabAdd.setOnClickListener {
            showAddEventDialog()
        }

        // Bottom navigation
        navHome.setOnClickListener {
            Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show()
        }

        navSettings.setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        // Check if we should open add dialog from widget
        if (intent.getBooleanExtra("open_add_dialog", false)) {
            showAddEventDialog()
        }
    }

    // --- Exit confirmation when pressing BACK ---
    override fun onBackPressed() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit the application?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }

    // --- Show Add Event Activity ---
    private fun showAddEventDialog() {
        val intent = Intent(this, AddEventActivity::class.java)
        intent.putExtra("selected_date", selectedDateMillis)
        startActivityForResult(intent, ADD_EVENT_REQUEST_CODE)
    }

    // --- Edit existing event ---
    private fun editEvent(event: Event) {
        val intent = Intent(this, AddEventActivity::class.java)
        intent.putExtra("selected_date", selectedDateMillis)
        intent.putExtra("edit_event", event)
        startActivityForResult(intent, EDIT_EVENT_REQUEST_CODE)
    }

    // --- Handle activity results ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ADD_EVENT_REQUEST_CODE -> {
                    val newEvent = data?.getSerializableExtra("new_event") as? Event
                    newEvent?.let {
                        events.add(it)
                        onEventsChanged()
                        Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                EDIT_EVENT_REQUEST_CODE -> {
                    // Events are updated in-place, just refresh
                    onEventsChanged()
                    Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Delete Event ---
    private fun deleteEvent(event: Event) {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Yes") { _, _ ->
                events.remove(event)
                onEventsChanged()
                Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    // --- Filter events for selected date ---
    private fun filterEventsByDate() {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val selectedKey = sdf.format(Date(selectedDateMillis))

        val filtered = events.filter {
            sdf.format(Date(it.dateMillis)) == selectedKey
        }

        eventAdapter.updateList(filtered)

        // Show empty state if no events
        if (filtered.isEmpty()) {
            textEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            textEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // --- Highlight dates that have events ---
    private fun highlightDatesWithEvents() {
        val eventDays = mutableListOf<EventDay>()

        // Get unique dates that have events
        val uniqueDates = events.map { event ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = event.dateMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }.distinct()

        uniqueDates.forEach { dateMillis ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = dateMillis
            }
            val eventDay = EventDay(calendar, R.drawable.event_dot_indicator)
            eventDays.add(eventDay)
        }

        calendarView.setEvents(eventDays)
    }

    // --- Update highlight when events change ---
    private fun updateCalendarHighlights() {
        highlightDatesWithEvents()
    }

    // --- Update widget when events change ---
    private fun updateWidget() {
        try {
            EventWidget::class.java
            EventWidget.updateAllWidgets(this)
        } catch (e: Exception) {
            println("Widget not available: ${e.message}")
        }
    }

    // --- Combined method for event changes ---
    private fun onEventsChanged() {
        saveEvents()
        filterEventsByDate()
        updateCalendarHighlights()
        updateWidget()
    }

    // --- Save events ---
    private fun saveEvents() {
        val sharedPref = getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val json = Gson().toJson(events)
        editor.putString("event_list", json)
        editor.apply()
    }

    // --- Load events ---
    private fun loadEvents() {
        val sharedPref = getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("event_list", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Event>>() {}.type
            val savedList: MutableList<Event> = Gson().fromJson(json, type)
            events.clear()
            events.addAll(savedList)
        }
    }
}