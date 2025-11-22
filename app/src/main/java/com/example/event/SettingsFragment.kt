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

    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME = "app_theme"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        if (!initViews(view)) {
            // If views couldn't be initialized, go back to home
            showHomeContent()
            return
        }

        // Set up click listeners
        setupClickListeners()

        // Load current settings
        loadCurrentSettings()
    }

    private fun initViews(view: View): Boolean {
        return try {
            logoutCard = view.findViewById(R.id.logoutCard)
            txtCurrentLanguage = view.findViewById(R.id.txtCurrentLanguage)
            txtCurrentTheme = view.findViewById(R.id.txtCurrentTheme)
            languageCard = view.findViewById(R.id.languageCard)
            themeCard = view.findViewById(R.id.themeCard)

            // Check if all views were found
            logoutCard != null && txtCurrentLanguage != null &&
                    txtCurrentTheme != null && languageCard != null && themeCard != null
        } catch (e: Exception) {
            false
        }
    }

    private fun setupClickListeners() {
        // No back button anymore - users will use bottom navigation to go back to home

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
        // Load current theme
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
        // Save language preference
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_LANGUAGE, languageCode).apply()

        // Update UI
        txtCurrentLanguage?.text = languageName

        // Show restart message
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
        // Save theme preference
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putInt(KEY_THEME, themeMode).apply()

        // Apply theme
        AppCompatDelegate.setDefaultNightMode(themeMode)

        // Update current theme text
        txtCurrentTheme?.text = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            else -> "System Default"
        }

        // Restart activity to immediately apply theme
        requireActivity().recreate()
    }

    private fun showHomeContent() {
        try {
            // Hide settings and show home content
            requireActivity().findViewById<LinearLayout>(R.id.homeContent)?.visibility = View.VISIBLE
            requireActivity().findViewById<FrameLayout>(R.id.fragmentContainer)?.visibility = View.GONE
        } catch (e: Exception) {
            // Fallback: just finish the activity
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
        // Navigate to LoginActivity (MainActivity)
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()

        // Show a toast message
        android.widget.Toast.makeText(requireContext(), "Logged out successfully", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up references to avoid memory leaks
        logoutCard = null
        txtCurrentLanguage = null
        txtCurrentTheme = null
        languageCard = null
        themeCard = null
    }
}