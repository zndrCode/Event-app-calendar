package com.example.event

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var logoutCard: LinearLayout
    private lateinit var txtCurrentLanguage: TextView
    private lateinit var txtCurrentTheme: TextView
    private lateinit var languageCard: LinearLayout
    private lateinit var themeCard: LinearLayout

    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME = "app_theme"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language and theme before setting content view
        applySavedLanguage()
        applySavedTheme()

        setContentView(R.layout.activity_settings)

        // Initialize views
        initViews()

        // Set up click listeners
        setupClickListeners()

        // Load current settings
        loadCurrentSettings()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        logoutCard = findViewById(R.id.logoutCard)
        txtCurrentLanguage = findViewById(R.id.txtCurrentLanguage)
        txtCurrentTheme = findViewById(R.id.txtCurrentTheme)
        languageCard = findViewById(R.id.languageCard)
        themeCard = findViewById(R.id.themeCard)
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Language selection
        languageCard.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // Theme selection
        themeCard.setOnClickListener {
            showThemeSelectionDialog()
        }

        // Logout
        logoutCard.setOnClickListener {
            performLogout()
        }
    }

    private fun loadCurrentSettings() {
        // Load current theme
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedTheme = sharedPrefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        val currentTheme = when (savedTheme) {
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            else -> "System Default"
        }
        txtCurrentTheme.text = currentTheme

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
        txtCurrentLanguage.text = currentLanguage
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "Spanish", "French", "German", "Japanese")
        val languageCodes = arrayOf("en", "es", "fr", "de", "ja")

        androidx.appcompat.app.AlertDialog.Builder(this)
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

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setItems(themes) { _, which ->
                setAppTheme(themeModes[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setAppLanguage(languageCode: String, languageName: String) {
        // Save language preference
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_LANGUAGE, languageCode).apply()

        // Update UI
        txtCurrentLanguage.text = languageName

        // Show restart message
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Language Changed")
            .setMessage("Please restart the app to apply the language changes.")
            .setPositiveButton("OK") { _, _ ->
                // Optional: Restart the app automatically
                restartApp()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun setAppTheme(themeMode: Int) {
        // Save theme preference
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPrefs.edit().putInt(KEY_THEME, themeMode).apply()

        // Apply theme
        AppCompatDelegate.setDefaultNightMode(themeMode)

        // Update current theme text
        txtCurrentTheme.text = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            else -> "System Default"
        }

        // Optional: Restart activity to immediately apply theme
        recreate()
    }

    private fun applySavedLanguage() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedLanguage = sharedPrefs.getString(KEY_LANGUAGE, "en") ?: "en"

        if (savedLanguage != "en") {
            val locale = Locale(savedLanguage)
            Locale.setDefault(locale)

            val resources: Resources = resources
            val configuration: Configuration = resources.configuration
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    private fun applySavedTheme() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedTheme = sharedPrefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedTheme)
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        // Optional: Add animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun performLogout() {
        // Clear any user data/sessions here
        // For example, clear SharedPreferences if you're using them for login state

        // Navigate to LoginActivity (MainActivity)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()

        // Optional: Show a toast message
        android.widget.Toast.makeText(this, "Logged out successfully", android.widget.Toast.LENGTH_SHORT).show()
    }
}