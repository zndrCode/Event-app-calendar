package com.example.event

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme before setting content view
        applySavedTheme()

        setContentView(R.layout.activity_login)

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

            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle("Before You Continue")
                .setMessage(
                    "You don't need to enter real information.\n" +
                            "Dummy data is fine since this is for a college project."
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

    private fun applySavedTheme() {
        val sharedPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val savedTheme = sharedPrefs.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedTheme)
    }
}