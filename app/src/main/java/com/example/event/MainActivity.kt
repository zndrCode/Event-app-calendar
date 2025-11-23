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

        // Apply saved theme before setting content view
        applySavedTheme()

        setContentView(R.layout.activity_login)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        val etUsername = findViewById<EditText>(R.id.editTextUsername)
        val etPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnLogin = findViewById<Button>(R.id.buttonLogin)
        val tvCreate = findViewById<TextView>(R.id.textCreate)
        val imgToggle = findViewById<ImageView>(R.id.imageTogglePassword)

        // Toggle Password Visibility
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

        // ---------------------------------------------------
        // SHOW DIALOG WHEN PRESSING "Create an Account"
        // ---------------------------------------------------
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Only request if we haven't already asked or if user hasn't denied permanently
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    // Show explanation why we need this permission
                    showNotificationPermissionExplanation()
                } else {
                    // Request the permission directly
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    private fun showNotificationPermissionExplanation() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("This app needs notification permission to remind you about your events. You'll receive alerts before your events start.")
            .setPositiveButton("Allow") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
                // Continue without notification permission
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
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission denied
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                            // User selected "Don't ask again" - show message about manual enabling
                            Toast.makeText(
                                this,
                                "To enable notifications later, go to Settings > Apps > Eventra > Notifications",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // User simply denied
                            Toast.makeText(
                                this,
                                "Notifications disabled. You can enable them in app settings.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun applySavedTheme() {
        val sharedPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val savedTheme = sharedPrefs.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedTheme)
    }
}