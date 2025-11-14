package com.example.event

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etFullName = findViewById<EditText>(R.id.editTextFullName)
        val etEmail = findViewById<EditText>(R.id.editTextEmail)
        val etUsername = findViewById<EditText>(R.id.editTextUsername)
        val etPassword = findViewById<EditText>(R.id.editTextPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.buttonSignUp)
        val tvGoToLogin = findViewById<TextView>(R.id.textGoToLogin)

        // ✅ Show AlertDialog FIRST before signup validation
        btnSignUp.setOnClickListener {

            val dialog = AlertDialog.Builder(this)
                .setTitle("Before You Continue")
                .setMessage("We don't need your actual account since this is a college project.\nYou may enter any dummy information.")
                .setCancelable(false)
                .setPositiveButton("Proceed") { d, _ ->
                    d.dismiss()

                    // Continue to sign-up validation AFTER confirmation
                    handleSignUp(
                        etFullName,
                        etEmail,
                        etUsername,
                        etPassword,
                        etConfirmPassword
                    )
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.dismiss()
                }
                .create()

            dialog.show()
        }

        // 🔄 Go to Login if already have an account
        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun handleSignUp(
        etFullName: EditText,
        etEmail: EditText,
        etUsername: EditText,
        etPassword: EditText,
        etConfirmPassword: EditText
    ) {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        when {
            fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match ❌", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("fullname", fullName)
                editor.putString("email", email)
                editor.putString("username", username)
                editor.putString("password", password)
                editor.apply()

                Toast.makeText(this, "Account created successfully 🎉", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
