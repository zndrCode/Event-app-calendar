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
    private lateinit var etLocation: EditText
    private lateinit var switchAllDay: Switch
    private lateinit var timeContainer: LinearLayout
    private lateinit var btnStartDate: Button
    private lateinit var btnStartTime: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnEndTime: Button
    private lateinit var btnAddEvent: Button
    private lateinit var btnCancel: Button
    private lateinit var btnBack: ImageButton

    private var startDateMillis: Long = 0L
    private var startTimeMillis: Long = 0L
    private var endDateMillis: Long = 0L
    private var endTimeMillis: Long = 0L
    private var selectedDateMillis: Long = 0L
    private var editEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_add_event_enhanced)

        // Get selected date from intent
        selectedDateMillis = intent.getLongExtra("selected_date", System.currentTimeMillis())
        editEvent = intent.getSerializableExtra("edit_event") as? Event

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEventName = findViewById(R.id.etEventName)
        etLocation = findViewById(R.id.etLocation)
        switchAllDay = findViewById(R.id.switchAllDay)
        timeContainer = findViewById(R.id.timeContainer)
        btnStartDate = findViewById(R.id.btnStartDate)
        btnStartTime = findViewById(R.id.btnStartTime)
        btnEndDate = findViewById(R.id.btnEndDate)
        btnEndTime = findViewById(R.id.btnEndTime)
        btnAddEvent = findViewById(R.id.btnAddEvent)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)

        // Check if we're editing an existing event
        if (editEvent != null) {
            populateEditData()
            btnAddEvent.text = "Update Event"
        } else {
            // Initialize with current date/time for new event
            val now = Calendar.getInstance()
            startDateMillis = selectedDateMillis
            startTimeMillis = now.timeInMillis
            endDateMillis = selectedDateMillis
            endTimeMillis = now.timeInMillis + 3600000 // 1 hour later
        }

        updateDateTimeButtons()
    }

    private fun populateEditData() {
        editEvent?.let { event ->
            etEventName.setText(event.title)
            etLocation.setText(event.location)
            switchAllDay.isChecked = event.isAllDay

            startDateMillis = event.dateMillis
            startTimeMillis = event.timeMillis
            endDateMillis = if (event.endTimeMillis > 0) event.endTimeMillis else event.timeMillis + 3600000
            endTimeMillis = if (event.endTimeMillis > 0) event.endTimeMillis else event.timeMillis + 3600000

            timeContainer.visibility = if (event.isAllDay) View.GONE else View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Show/hide time container based on all-day switch
        switchAllDay.setOnCheckedChangeListener { _, isChecked ->
            timeContainer.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        // Date/Time picker handlers
        btnStartDate.setOnClickListener { showDatePicker(true) }
        btnStartTime.setOnClickListener { showTimePicker(true) }
        btnEndDate.setOnClickListener { showDatePicker(false) }
        btnEndTime.setOnClickListener { showTimePicker(false) }

        // Cancel button - close activity
        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Add/Update Event button
        btnAddEvent.setOnClickListener {
            if (editEvent != null) {
                updateEvent()
            } else {
                addEvent()
            }
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (isStart) startDateMillis else endDateMillis

        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (isStart) {
                startDateMillis = selectedCalendar.timeInMillis
                if (startDateMillis > endDateMillis) {
                    endDateMillis = startDateMillis
                }
            } else {
                endDateMillis = selectedCalendar.timeInMillis
                if (endDateMillis < startDateMillis) {
                    startDateMillis = endDateMillis
                }
            }
            updateDateTimeButtons()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (isStart) startTimeMillis else endTimeMillis

        val timePicker = TimePickerDialog(this, { _, hour, minute ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (isStart) {
                startTimeMillis = selectedCalendar.timeInMillis
                if (startDateMillis == endDateMillis && startTimeMillis > endTimeMillis) {
                    endTimeMillis = startTimeMillis + 3600000
                }
            } else {
                endTimeMillis = selectedCalendar.timeInMillis
                if (startDateMillis == endDateMillis && endTimeMillis < startTimeMillis) {
                    startTimeMillis = endTimeMillis - 3600000
                }
            }
            updateDateTimeButtons()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)

        timePicker.show()
    }

    private fun updateDateTimeButtons() {
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        btnStartDate.text = dateFormat.format(Date(startDateMillis))
        btnStartTime.text = timeFormat.format(Date(startTimeMillis))
        btnEndDate.text = dateFormat.format(Date(endDateMillis))
        btnEndTime.text = timeFormat.format(Date(endTimeMillis))
    }

    private fun addEvent() {
        val name = etEventName.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (name.isEmpty()) {
            etEventName.error = "Please enter an event name"
            return
        }

        val event = createEvent(name, location)

        // Return the event to HomeActivity
        val resultIntent = Intent().apply {
            putExtra("new_event", event)
        }
        setResult(RESULT_OK, resultIntent)
        finish()

        Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
    }

    private fun updateEvent() {
        val name = etEventName.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (name.isEmpty()) {
            etEventName.error = "Please enter an event name"
            return
        }

        editEvent?.let { event ->
            val updatedEvent = createEvent(name, location)

            // Update the existing event properties
            event.title = updatedEvent.title
            event.location = updatedEvent.location
            event.dateMillis = updatedEvent.dateMillis
            event.timeMillis = updatedEvent.timeMillis
            event.isAllDay = updatedEvent.isAllDay
            event.endTimeMillis = updatedEvent.endTimeMillis

            // Return success
            setResult(RESULT_OK)
            finish()

            Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createEvent(name: String, location: String): Event {
        // Combine date and time for start
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = startDateMillis
            val timeCalendar = Calendar.getInstance().apply { timeInMillis = startTimeMillis }
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Combine date and time for end
        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = endDateMillis
            val timeCalendar = Calendar.getInstance().apply { timeInMillis = endTimeMillis }
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return Event(
            id = editEvent?.id ?: System.currentTimeMillis(),
            title = name,
            dateMillis = startCalendar.timeInMillis,
            timeMillis = startCalendar.timeInMillis,
            location = location,
            isAllDay = switchAllDay.isChecked,
            endTimeMillis = if (!switchAllDay.isChecked) endCalendar.timeInMillis else 0L
        )
    }
}