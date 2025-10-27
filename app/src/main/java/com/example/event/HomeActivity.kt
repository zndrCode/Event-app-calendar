package com.example.event

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible

class HomeActivity : ComponentActivity() {

    private lateinit var eventContainer: LinearLayout
    private lateinit var emptyText: TextView
    private val eventList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        eventContainer = findViewById(R.id.eventContainer)
        emptyText = findViewById(R.id.textEmpty)
        val fabAdd = findViewById<ImageButton>(R.id.fabAdd)

        updateEmptyState()

        fabAdd.setOnClickListener {
            showAddEventDialog()
        }
    }

    // 🪶 Show add-event dialog
    private fun showAddEventDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Event")

        val input = EditText(this)
        input.hint = "Event subject name"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val eventName = input.text.toString().trim()
            if (eventName.isNotEmpty()) {
                eventList.add(eventName)
                addEventCard(eventName)
                updateEmptyState()
            } else {
                Toast.makeText(this, "Please enter an event name", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // 🧱 Add event card to the container
    private fun addEventCard(eventName: String) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.HORIZONTAL
        card.setPadding(16, 16, 16, 16)
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
        card.setPadding(24, 16, 24, 16)
        card.setBackgroundColor(0xFFFFFFFF.toInt())

        val txtEvent = TextView(this)
        txtEvent.text = eventName
        txtEvent.textSize = 18f
        txtEvent.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val btnEdit = ImageButton(this)
        btnEdit.setImageResource(R.drawable.dots_edit)
        btnEdit.setBackgroundColor(0)
        btnEdit.setOnClickListener { showEditEventDialog(eventName, txtEvent) }

        val btnDelete = ImageButton(this)
        btnDelete.setImageResource(R.drawable.trash_event)
        btnDelete.setBackgroundColor(0)
        btnDelete.setOnClickListener {
            eventContainer.removeView(card)
            eventList.remove(eventName)
            updateEmptyState()
        }

        card.addView(txtEvent)
        card.addView(btnEdit)
        card.addView(btnDelete)
        eventContainer.addView(card)
    }

    // ✏️ Edit event name
    private fun showEditEventDialog(oldName: String, textView: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Event")

        val input = EditText(this)
        input.setText(oldName)
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                val index = eventList.indexOf(oldName)
                if (index != -1) eventList[index] = newName
                textView.text = newName
                Toast.makeText(this, "Event renamed", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // ⚙️ Show or hide empty text
    private fun updateEmptyState() {
        emptyText.isVisible = eventList.isEmpty()
    }
}
