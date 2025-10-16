package com.example.event

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity

class SignUpActivity : ComponentActivity() {

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

        // sa Sign Up button
        btnSignUp.setOnClickListener {
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
                    Toast.makeText(this, "Account created successfully 🎉", Toast.LENGTH_SHORT).show()
                    // TODO: Save account to database or backend

                    // pag human ug signup, ma balik ka sa login screen
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }

        // para sa "Already have an account? Login"
        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}