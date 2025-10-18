package com.example.event

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.editTextUsername)
        val etPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnLogin = findViewById<Button>(R.id.buttonLogin)
        val tvCreate = findViewById<TextView>(R.id.textCreate)
        val imgToggle = findViewById<ImageView>(R.id.imageTogglePassword)

        // 👁️ Toggle password visibility
        imgToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imgToggle.setImageResource(android.R.drawable.ic_menu_view)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                imgToggle.setImageResource(android.R.drawable.ic_secure)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // 🔐 Login button
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            when {
                username.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                }
                username == "admin" && password == "1234" -> {
                    Toast.makeText(this, "Login successful 🎉", Toast.LENGTH_SHORT).show()

                    // ✅ Go to HomeActivity after successful login
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // prevents going back to login screen
                }
                else -> {
                    Toast.makeText(this, "Invalid credentials ❌", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ➕ Create Account
        tvCreate.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
