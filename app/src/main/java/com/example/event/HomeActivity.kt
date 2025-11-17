package com.example.event

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
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
    private lateinit var fabAdd: LinearLayout
    private lateinit var navHome: LinearLayout
    private lateinit var navSettings: LinearLayout
    private lateinit var textEmpty: TextView

    private lateinit var eventAdapter: EventAdapter
    private val events = mutableListOf<Event>()
    private var selectedDateMillis: Long = 0L

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

        // When user selects a date - CORRECTED VERSION
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
    }

    // --- Exit confirmation when pressing BACK ---
    override fun onBackPressed() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit the application?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity() // closes the entire app
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }

    // --- Show popup dialog to add event ---
    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup_add_event, null)
        val etEventName = dialogView.findViewById<EditText>(R.id.etEventName)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnAddEvent = dialogView.findViewById<Button>(R.id.btnAddEvent)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnAddEvent.setOnClickListener {
            val name = etEventName.text.toString().trim()
            if (name.isEmpty()) {
                etEventName.error = "Please enter an event name"
                return@setOnClickListener
            }

            val hour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                timePicker.hour else timePicker.currentHour
            val minute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                timePicker.minute else timePicker.currentMinute

            val cal = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val newEvent = Event(
                id = System.currentTimeMillis(),
                title = name,
                dateMillis = cal.timeInMillis,
                timeMillis = cal.timeInMillis
            )

            events.add(newEvent)
            onEventsChanged() // This saves, filters, and updates highlights
            dialog.dismiss()

            val timeString = String.format("%02d:%02d", hour, minute)
            Toast.makeText(this, "Event added at $timeString", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    // --- Edit existing event ---
    private fun editEvent(event: Event) {
        val dialogView = layoutInflater.inflate(R.layout.popup_add_event, null)
        val etEventName = dialogView.findViewById<EditText>(R.id.etEventName)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnAddEvent = dialogView.findViewById<Button>(R.id.btnAddEvent)

        etEventName.setText(event.title)

        val cal = Calendar.getInstance().apply { timeInMillis = event.timeMillis }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            timePicker.hour = cal.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = cal.get(Calendar.MINUTE)
        } else {
            timePicker.currentHour = cal.get(Calendar.HOUR_OF_DAY)
            timePicker.currentMinute = cal.get(Calendar.MINUTE)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnAddEvent.text = "Update Event"
        btnAddEvent.setOnClickListener {
            val name = etEventName.text.toString().trim()
            if (name.isEmpty()) {
                etEventName.error = "Please enter an event name"
                return@setOnClickListener
            }

            val hour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                timePicker.hour else timePicker.currentHour
            val minute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                timePicker.minute else timePicker.currentMinute

            val calUpdated = Calendar.getInstance().apply {
                timeInMillis = event.dateMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            event.title = name
            event.timeMillis = calUpdated.timeInMillis
            onEventsChanged() // This saves, filters, and updates highlights
            dialog.dismiss()

            Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    // --- Delete Event ---
    private fun deleteEvent(event: Event) {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Yes") { _, _ ->
                events.remove(event)
                onEventsChanged() // This saves, filters, and updates highlights
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

            // Create EventDay with dot indicator
            val eventDay = EventDay(calendar, R.drawable.event_dot_indicator)
            eventDays.add(eventDay)
        }

        calendarView.setEvents(eventDays)
    }

    // --- Update highlight when events change ---
    private fun updateCalendarHighlights() {
        highlightDatesWithEvents()
    }

    // --- Combined method for event changes ---
    private fun onEventsChanged() {
        saveEvents()
        filterEventsByDate()
        updateCalendarHighlights()
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