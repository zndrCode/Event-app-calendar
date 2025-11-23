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
    private var spinnerReminder: Spinner? = null

    private var selectedStartDateMillis: Long = 0L
    private var selectedEndDateMillis: Long = 0L
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
        setupReminderSpinner()
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
        spinnerReminder = findViewById(R.id.spinnerReminder)

        if (editEvent != null) {
            loadEventForEdit()
        } else {
            val now = Calendar.getInstance()
            selectedStartDateMillis = initialDateMillis
            selectedEndDateMillis = initialDateMillis
            startTimeMillis = now.timeInMillis
            endTimeMillis = now.timeInMillis + 3600000
        }

        updateDateTimeDisplays()
    }

    private fun setupReminderSpinner() {
        spinnerReminder?.let { spinner ->
            val reminderOptions = arrayOf("No reminder", "15 minutes before", "30 minutes before", "1 hour before")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reminderOptions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            editEvent?.let { event ->
                val reminderPosition = when (event.reminderMinutes) {
                    15 -> 1
                    30 -> 2
                    60 -> 3
                    else -> 0
                }
                spinner.setSelection(reminderPosition)
            }
        }
    }

    private fun loadEventForEdit() {
        editEvent?.let { event ->
            etEventName.setText(event.title)
            etDescription.setText(event.description)
            switchAllDay.isChecked = event.isAllDay

            selectedStartDateMillis = event.dateMillis
            selectedEndDateMillis = event.dateMillis
            startTimeMillis = event.timeMillis
            endTimeMillis = if (event.endTimeMillis > 0) event.endTimeMillis else startTimeMillis + 3600000

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
                startDateCard.visibility = View.GONE
                endDateCard.visibility = View.GONE
            } else {
                startDateCard.visibility = View.VISIBLE
                endDateCard.visibility = View.VISIBLE
            }
        }

        startDateCard.setOnClickListener {
            showDatePicker(true)
        }

        txtStartTime.setOnClickListener {
            showTimePicker(true)
        }

        endDateCard.setOnClickListener {
            showDatePicker(false)
        }

        txtEndTime.setOnClickListener {
            showTimePicker(false)
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
                if (selectedStartDateMillis > selectedEndDateMillis) {
                    selectedEndDateMillis = selectedStartDateMillis
                }
            } else {
                selectedEndDateMillis = selectedCalendar.timeInMillis
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
                if (selectedStartDateMillis == selectedEndDateMillis && startTimeMillis > endTimeMillis) {
                    endTimeMillis = startTimeMillis + 3600000
                }
            } else {
                endTimeMillis = selectedCalendar.timeInMillis
                if (selectedStartDateMillis == selectedEndDateMillis && endTimeMillis < startTimeMillis) {
                    startTimeMillis = endTimeMillis - 3600000
                }
            }

            updateDateTimeDisplays()

        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)

        timePicker.show()
    }

    private fun updateDateTimeDisplays() {
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        txtStartDate.text = dateFormat.format(Date(selectedStartDateMillis))
        txtStartTime.text = timeFormat.format(Date(startTimeMillis))
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

        // Schedule all notifications
        scheduleAllEventNotifications(event)

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
            // Cancel existing notifications
            AlarmReceiver.cancelAllEventNotifications(this, oldEvent)

            val newEvent = buildEvent(name)

            oldEvent.title = newEvent.title
            oldEvent.description = newEvent.description
            oldEvent.dateMillis = newEvent.dateMillis
            oldEvent.timeMillis = newEvent.timeMillis
            oldEvent.endTimeMillis = newEvent.endTimeMillis
            oldEvent.isAllDay = newEvent.isAllDay
            oldEvent.reminderMinutes = newEvent.reminderMinutes

            // Schedule new notifications
            scheduleAllEventNotifications(newEvent)
        }

        setResult(RESULT_OK)
        finish()
        Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun buildEvent(name: String): Event {
        val description = etDescription.text.toString().trim()
        val selectedReminder = when (spinnerReminder?.selectedItemPosition ?: 0) {
            1 -> 15
            2 -> 30
            3 -> 60
            else -> 0
        }

        return Event(
            id = editEvent?.id ?: System.currentTimeMillis(),
            title = name,
            description = description,
            dateMillis = selectedStartDateMillis,
            timeMillis = startTimeMillis,
            isAllDay = switchAllDay.isChecked,
            endTimeMillis = if (!switchAllDay.isChecked) {
                if (selectedStartDateMillis != selectedEndDateMillis) {
                    val endCalendar = Calendar.getInstance().apply {
                        timeInMillis = selectedEndDateMillis
                        val timeCalendar = Calendar.getInstance().apply { timeInMillis = endTimeMillis }
                        set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    }
                    endCalendar.timeInMillis
                } else {
                    endTimeMillis
                }
            } else 0L,
            reminderMinutes = selectedReminder
        )
    }

    private fun scheduleAllEventNotifications(event: Event) {
        // Schedule reminder if needed
        if (event.reminderMinutes > 0) {
            AlarmReceiver.scheduleEventReminder(this, event, event.reminderMinutes)
        }

        // Always schedule start and end notifications
        AlarmReceiver.scheduleEventStartNotification(this, event)
        AlarmReceiver.scheduleEventEndNotification(this, event)
    }
}