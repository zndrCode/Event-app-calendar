package com.example.event

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme before UI loads
        applySavedTheme()

        setContentView(R.layout.activity_login)

        // Ask for notification permission ONCE on first app launch
        requestNotificationPermissionOnce()

        val etUsername = findViewById<EditText>(R.id.editTextUsername)
        val etPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnLogin = findViewById<Button>(R.id.buttonLogin)
        val tvCreate = findViewById<TextView>(R.id.textCreate)
        val imgToggle = findViewById<ImageView>(R.id.imageTogglePassword)

        // Toggle password visibility
        imgToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imgToggle.setImageResource(android.R.drawable.ic_menu_view)
            } else {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                imgToggle.setImageResource(android.R.drawable.ic_secure)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Login Button
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val savedUsername = sharedPref.getString("username", null)
            val savedPassword = sharedPref.getString("password", null)

            when {
                username.isEmpty() || password.isEmpty() ->
                    Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()

                username == savedUsername && password == savedPassword -> {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }

                else ->
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        // "Create an Account" dialog
        tvCreate.setOnClickListener {

            val dialog = AlertDialog.Builder(this)
                .setTitle("Before You Continue")
                .setMessage(
                    "We don't need your real personal information.\n" +
                            "This is only for a college project, so dummy data is allowed."
                )
                .setCancelable(false)
                .setPositiveButton("Proceed") { d, _ ->
                    d.dismiss()
                    startActivity(Intent(this, SignUpActivity::class.java))
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.dismiss()
                }
                .create()

            dialog.show()
        }
    }

    // ------------------------------------------------------------
    // NOTIFICATION PERMISSION (ONLY ASK ON FIRST APP OPEN)
    // ------------------------------------------------------------
    private fun requestNotificationPermissionOnce() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val alreadyAsked = prefs.getBoolean("asked_notification_permission", false)

        if (!alreadyAsked) {
            prefs.edit().putBoolean("asked_notification_permission", true).apply()
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS

            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    showNotificationPermissionExplanation()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permission),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    private fun showNotificationPermissionExplanation() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("Eventra needs notification permission to remind you about your events. You'll receive alerts before your events start.")
            .setPositiveButton("Allow") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "You can enable notifications later in Settings", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    if (!shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                        Toast.makeText(
                            this,
                            "To enable notifications later, go to Settings > Apps > Eventra > Notifications",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Notifications disabled. You can enable them in Settings.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------
    // APPLY SAVED THEME
    // ------------------------------------------------------------
    private fun applySavedTheme() {
        val sharedPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val savedTheme = sharedPrefs.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedTheme)
    }
}
