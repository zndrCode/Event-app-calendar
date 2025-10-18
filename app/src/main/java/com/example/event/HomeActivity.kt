package com.example.event

import android.view.*
import android.widget.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding

class HomeActivity : ComponentActivity() {

    private lateinit var homeContent: FrameLayout
    private lateinit var textEmptyState: TextView
    private var popupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val fabAdd = findViewById<ImageButton>(R.id.fabAdd)
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navBookmarks = findViewById<LinearLayout>(R.id.navBookmarks)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)
        homeContent = findViewById(R.id.homeContent)
        textEmptyState = findViewById(R.id.textEmptyState)

        fabAdd.setOnClickListener {
            showAddEventPopup(it)
        }

        navHome.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }
        navBookmarks.setOnClickListener {
            Toast.makeText(this, "Bookmarks", Toast.LENGTH_SHORT).show()
        }
        navProfile.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddEventPopup(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_add_event, null)
        val etEventName = popupView.findViewById<EditText>(R.id.etEventName)
        val btnAddEvent = popupView.findViewById<Button>(R.id.btnAddEvent)

        popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow?.elevation = 10f
        popupWindow?.isOutsideTouchable = true
        popupWindow?.showAtLocation(anchorView, Gravity.CENTER, 0, 0)

        btnAddEvent.setOnClickListener {
            val eventName = etEventName.text.toString().trim()
            if (eventName.isNotEmpty()) {
                addEventCard(eventName)
                popupWindow?.dismiss()
            } else {
                Toast.makeText(this, "Please enter an event name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addEventCard(eventName: String) {
        textEmptyState.visibility = TextView.GONE

        val eventCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32)
            background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.event_card_bg)
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(32, 32, 32, 0)
            layoutParams = params
        }

        val title = TextView(this).apply {
            text = eventName
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@HomeActivity, android.R.color.black))
            setPadding(8)
        }

        eventCard.addView(title)
        homeContent.addView(eventCard)
    }
}
