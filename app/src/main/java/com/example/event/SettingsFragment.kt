package com.example.event

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private var logoutCard: LinearLayout? = null
    private var switchNotifications: Switch? = null

    private var aboutCard: LinearLayout? = null
    private var termsCard: LinearLayout? = null

    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!initViews(view)) {
            showHomeContent()
            return
        }

        setupClickListeners()
        loadCurrentSettings()
    }

    private fun initViews(view: View): Boolean {
        return try {
            logoutCard = view.findViewById(R.id.logoutCard)
            switchNotifications = view.findViewById(R.id.switchNotifications)

            aboutCard = view.findViewById(R.id.aboutCard)
            termsCard = view.findViewById(R.id.termsCard)

            logoutCard != null &&
                    switchNotifications != null &&
                    aboutCard != null &&
                    termsCard != null
        } catch (e: Exception) {
            false
        }
    }

    private fun setupClickListeners() {

        // Notifications toggle
        switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            setNotificationsEnabled(isChecked)
        }

        // About Us
        aboutCard?.setOnClickListener {
            showAboutDialog()
        }

        // Terms of Service
        termsCard?.setOnClickListener {
            showTermsDialog()
        }

        // Logout
        logoutCard?.setOnClickListener {
            performLogout()
        }
    }

    private fun loadCurrentSettings() {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPrefs.getBoolean(KEY_NOTIFICATIONS, true)
        switchNotifications?.isChecked = notificationsEnabled
    }

    private fun setNotificationsEnabled(enabled: Boolean) {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()

        if (enabled) {
            rescheduleAllNotifications()
            android.widget.Toast.makeText(requireContext(), "Notifications enabled", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            cancelAllNotifications()
            android.widget.Toast.makeText(requireContext(), "Notifications disabled", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun rescheduleAllNotifications() {
        try {
            val homeActivity = requireActivity() as? HomeActivity
            homeActivity?.rescheduleAllNotifications()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelAllNotifications() {
        try {
            val homeActivity = requireActivity() as? HomeActivity
            homeActivity?.cancelAllNotifications()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ---------------------------
    // ABOUT US DIALOG
    // ---------------------------
    private fun showAboutDialog() {
        val aboutMessage = """
            Eventra is a calendar-based event reminder application developed as a college project.
            Its purpose is to provide users with a simple and clean way to add, view, and manage
            daily events.

            This project was created to demonstrate Android development skills, UI/UX design,
            and event-handling features for academic purposes.

            Version 1.0.0
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("About Us")
            .setMessage(aboutMessage)
            .setPositiveButton("OK", null)
            .show()
    }

    // ---------------------------
    // TERMS OF SERVICE DIALOG
    // ---------------------------
    private fun showTermsDialog() {
        val termsMessage = """
            Eventra is a college project developed solely for academic purposes.
            By using this application, you acknowledge and agree to the following:

            1. Eventra is a prototype created to demonstrate Android development concepts such as
            event scheduling, notifications, and UI design.

            2. The app may store event details, but users should avoid entering sensitive data such
            as passwords, financial information, medical details, or confidential personal data.

            3. All features are provided “as is.” The app may contain bugs, incomplete features,
            or limitations. There is no guarantee of constant availability or accuracy.

            By continuing to use Eventra, you acknowledge that it is a student-created project and
            accept all risks and limitations associated with its use.
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Terms of Service")
            .setMessage(termsMessage)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showHomeContent() {
        try {
            requireActivity().findViewById<LinearLayout>(R.id.homeContent)?.visibility =
                View.VISIBLE
            requireActivity().findViewById<FrameLayout>(R.id.fragmentContainer)?.visibility =
                View.GONE
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }

    private fun performLogout() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()

        android.widget.Toast.makeText(
            requireContext(),
            "Logged out successfully",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logoutCard = null
        switchNotifications = null
        aboutCard = null
        termsCard = null
    }
}
