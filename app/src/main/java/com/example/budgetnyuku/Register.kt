package com.example.budgetnyuku

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


class Register : AppCompatActivity() {


    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            when {
                name.isEmpty() -> Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                email.isEmpty() -> Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                password != confirmPassword -> Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                else -> {
                    val success = db.registerUser(name, email, password)
                    if (success) {
                        Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, LogIn::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Email already exists or registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}