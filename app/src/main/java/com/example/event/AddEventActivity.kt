package com.example.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var etEventName: EditText
    private lateinit var etDescription: EditText
    private lateinit var switchAllDay: Switch
    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: ImageButton
    private lateinit var txtStartDate: TextView
    private lateinit var txtStartTime: TextView
    private lateinit var txtEndDate: TextView
    private lateinit var txtEndTime: TextView
    private lateinit var startDateCard: LinearLayout
    private lateinit var endDateCard: LinearLayout

    private var selectedStartDateMillis: Long = 0L // Separate start date
    private var selectedEndDateMillis: Long = 0L   // Separate end date
    private var startTimeMillis: Long = 0L
    private var endTimeMillis: Long = 0L
    private var editEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_add_event_enhanced)

        val initialDateMillis = intent.getLongExtra("selected_date", System.currentTimeMillis())
        editEvent = intent.getSerializableExtra("edit_event") as? Event

        initializeViews(initialDateMillis)
        setupClickListeners()
    }

    private fun initializeViews(initialDateMillis: Long) {
        etEventName = findViewById(R.id.etEventName)
        etDescription = findViewById(R.id.etDescription)
        switchAllDay = findViewById(R.id.switchAllDay)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        txtStartDate = findViewById(R.id.txtStartDate)
        txtStartTime = findViewById(R.id.txtStartTime)
        txtEndDate = findViewById(R.id.txtEndDate)
        txtEndTime = findViewById(R.id.txtEndTime)
        startDateCard = findViewById(R.id.startDateCard)
        endDateCard = findViewById(R.id.endDateCard)

        if (editEvent != null) {
            loadEventForEdit()
        } else {
            val now = Calendar.getInstance()
            selectedStartDateMillis = initialDateMillis
            selectedEndDateMillis = initialDateMillis
            startTimeMillis = now.timeInMillis
            endTimeMillis = now.timeInMillis + 3600000 // 1 hour later
        }

        updateDateTimeDisplays()
    }

    private fun loadEventForEdit() {
        editEvent?.let { event ->
            etEventName.setText(event.title)
            etDescription.setText(event.description)
            switchAllDay.isChecked = event.isAllDay

            selectedStartDateMillis = event.dateMillis
            selectedEndDateMillis = event.dateMillis // Initially same as start date
            startTimeMillis = event.timeMillis
            endTimeMillis = if (event.endTimeMillis > 0) event.endTimeMillis else startTimeMillis + 3600000

            // Show/hide time cards based on all-day
            startDateCard.visibility = if (event.isAllDay) View.GONE else View.VISIBLE
            endDateCard.visibility = if (event.isAllDay) View.GONE else View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btnSave.setOnClickListener {
            if (editEvent != null) updateEvent() else addEvent()
        }

        switchAllDay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Hide time selection for all-day events
                startDateCard.visibility = View.GONE
                endDateCard.visibility = View.GONE
            } else {
                // Show time selection for timed events
                startDateCard.visibility = View.VISIBLE
                endDateCard.visibility = View.VISIBLE
            }
        }

        // Start date/time selection
        startDateCard.setOnClickListener {
            showDatePicker(true) // true for start date
        }

        txtStartTime.setOnClickListener {
            showTimePicker(true) // true for start time
        }

        // End date/time selection
        endDateCard.setOnClickListener {
            showDatePicker(false) // false for end date
        }

        txtEndTime.setOnClickListener {
            showTimePicker(false) // false for end time
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (isStart) selectedStartDateMillis else selectedEndDateMillis

        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (isStart) {
                selectedStartDateMillis = selectedCalendar.timeInMillis
                // If start date is after end date, adjust end date to be same as start
                if (selectedStartDateMillis > selectedEndDateMillis) {
                    selectedEndDateMillis = selectedStartDateMillis
                }
            } else {
                selectedEndDateMillis = selectedCalendar.timeInMillis
                // If end date is before start date, adjust start date to be same as end
                if (selectedEndDateMillis < selectedStartDateMillis) {
                    selectedStartDateMillis = selectedEndDateMillis
                }
            }

            updateDateTimeDisplays()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (isStart) startTimeMillis else endTimeMillis

        val timePicker = TimePickerDialog(this, { _, hour, minute ->
            val selectedDateMillis = if (isStart) selectedStartDateMillis else selectedEndDateMillis
            val selectedCalendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (isStart) {
                startTimeMillis = selectedCalendar.timeInMillis
                // If same date and start time is after end time, adjust end time
                if (selectedStartDateMillis == selectedEndDateMillis && startTimeMillis > endTimeMillis) {
                    endTimeMillis = startTimeMillis + 3600000 // 1 hour later
                }
            } else {
                endTimeMillis = selectedCalendar.timeInMillis
                // If same date and end time is before start time, adjust start time
                if (selectedStartDateMillis == selectedEndDateMillis && endTimeMillis < startTimeMillis) {
                    startTimeMillis = endTimeMillis - 3600000 // 1 hour earlier
                }
            }

            updateDateTimeDisplays()

        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)

        timePicker.show()
    }

    private fun updateDateTimeDisplays() {
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Update start date/time
        txtStartDate.text = dateFormat.format(Date(selectedStartDateMillis))
        txtStartTime.text = timeFormat.format(Date(startTimeMillis))

        // Update end date/time
        txtEndDate.text = dateFormat.format(Date(selectedEndDateMillis))
        txtEndTime.text = timeFormat.format(Date(endTimeMillis))
    }

    private fun addEvent() {
        val name = etEventName.text.toString().trim()

        if (name.isEmpty()) {
            etEventName.error = "Please enter an event name"
            return
        }

        val event = buildEvent(name)

        setResult(RESULT_OK, Intent().apply {
            putExtra("new_event", event)
        })
        finish()
        Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
    }

    private fun updateEvent() {
        val name = etEventName.text.toString().trim()

        if (name.isEmpty()) {
            etEventName.error = "Please enter an event name"
            return
        }

        editEvent?.let { oldEvent ->
            val newEvent = buildEvent(name)

            oldEvent.title = newEvent.title
            oldEvent.description = newEvent.description
            oldEvent.dateMillis = newEvent.dateMillis
            oldEvent.timeMillis = newEvent.timeMillis
            oldEvent.endTimeMillis = newEvent.endTimeMillis
            oldEvent.isAllDay = newEvent.isAllDay
        }

        setResult(RESULT_OK)
        finish()
        Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun buildEvent(name: String): Event {
        val description = etDescription.text.toString().trim()

        return Event(
            id = editEvent?.id ?: System.currentTimeMillis(),
            title = name,
            description = description,
            dateMillis = selectedStartDateMillis, // Use start date for the event date
            timeMillis = startTimeMillis,
            isAllDay = switchAllDay.isChecked,
            endTimeMillis = if (!switchAllDay.isChecked) {
                // For multi-day events, we need to handle the end date properly
                if (selectedStartDateMillis != selectedEndDateMillis) {
                    // If different dates, use the end date with end time
                    val endCalendar = Calendar.getInstance().apply {
                        timeInMillis = selectedEndDateMillis
                        val timeCalendar = Calendar.getInstance().apply { timeInMillis = endTimeMillis }
                        set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    }
                    endCalendar.timeInMillis
                } else {
                    // Same date, just use end time
                    endTimeMillis
                }
            } else 0L
        )
    }
}