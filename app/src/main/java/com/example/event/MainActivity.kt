package com.example.event

import android.content.Context
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

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val savedUsername = sharedPref.getString("username", null)
            val savedPassword = sharedPref.getString("password", null)

            when {
                username.isEmpty() || password.isEmpty() ->
                    Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()

                username == savedUsername && password == savedPassword -> {
                    Toast.makeText(this, "Login successful 🎉", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }

                else ->
                    Toast.makeText(this, "Invalid credentials ❌", Toast.LENGTH_SHORT).show()
            }
        }

        tvCreate.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
