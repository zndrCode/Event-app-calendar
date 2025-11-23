package com.example.event

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import java.util.*

class SettingsFragment : Fragment() {

    private var logoutCard: LinearLayout? = null
    private var txtCurrentLanguage: TextView? = null
    private var txtCurrentTheme: TextView? = null
    private var languageCard: LinearLayout? = null
    private var themeCard: LinearLayout? = null
    private var switchNotifications: Switch? = null

    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME = "app_theme"
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
            txtCurrentLanguage = view.findViewById(R.id.txtCurrentLanguage)
            txtCurrentTheme = view.findViewById(R.id.txtCurrentTheme)
            languageCard = view.findViewById(R.id.languageCard)
            themeCard = view.findViewById(R.id.themeCard)
            switchNotifications = view.findViewById(R.id.switchNotifications)

            logoutCard != null && txtCurrentLanguage != null &&
                    txtCurrentTheme != null && languageCard != null &&
                    themeCard != null && switchNotifications != null
        } catch (e: Exception) {
            false
        }
    }

    private fun setupClickListeners() {
        // Notifications toggle
        switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            setNotificationsEnabled(isChecked)
        }

        // Language selection
        languageCard?.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // Theme selection
        themeCard?.setOnClickListener {
            showThemeSelectionDialog()
        }

        // Logout
        logoutCard?.setOnClickListener {
            performLogout()
        }
    }

    private fun loadCurrentSettings() {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Load notifications setting
        val notificationsEnabled = sharedPrefs.getBoolean(KEY_NOTIFICATIONS, true)
        switchNotifications?.isChecked = notificationsEnabled

        // Load current theme
        val savedTheme = sharedPrefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val currentTheme = when (savedTheme) {
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            else -> "System Default"
        }
        txtCurrentTheme?.text = currentTheme

        // Load current language
        val savedLanguage = sharedPrefs.getString(KEY_LANGUAGE, "en") ?: "en"
        val currentLanguage = when (savedLanguage) {
            "en" -> "English"
            "es" -> "Spanish"
            "fr" -> "French"
            "de" -> "German"
            "ja" -> "Japanese"
            else -> "English"
        }
        txtCurrentLanguage?.text = currentLanguage
    }

    private fun setNotificationsEnabled(enabled: Boolean) {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()

        if (enabled) {
            // Reschedule all notifications when enabled
            rescheduleAllNotifications()
            android.widget.Toast.makeText(requireContext(), "Notifications enabled", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            // Cancel all notifications when disabled
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

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "Spanish", "French", "German", "Japanese")
        val languageCodes = arrayOf("en", "es", "fr", "de", "ja")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                val selectedLanguage = languages[which]
                val selectedLanguageCode = languageCodes[which]
                setAppLanguage(selectedLanguageCode, selectedLanguage)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val themeModes = arrayOf(
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setItems(themes) { _, which ->
                setAppTheme(themeModes[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setAppLanguage(languageCode: String, languageName: String) {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_LANGUAGE, languageCode).apply()

        txtCurrentLanguage?.text = languageName

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Language Changed")
            .setMessage("Please restart the app to apply the language changes.")
            .setPositiveButton("OK") { _, _ ->
                restartApp()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun setAppTheme(themeMode: Int) {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putInt(KEY_THEME, themeMode).apply()

        AppCompatDelegate.setDefaultNightMode(themeMode)

        txtCurrentTheme?.text = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            else -> "System Default"
        }

        requireActivity().recreate()
    }

    private fun showHomeContent() {
        try {
            requireActivity().findViewById<LinearLayout>(R.id.homeContent)?.visibility = View.VISIBLE
            requireActivity().findViewById<FrameLayout>(R.id.fragmentContainer)?.visibility = View.GONE
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }

    private fun restartApp() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun performLogout() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()

        android.widget.Toast.makeText(requireContext(), "Logged out successfully", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logoutCard = null
        txtCurrentLanguage = null
        txtCurrentTheme = null
        languageCard = null
        themeCard = null
        switchNotifications = null
    }
}