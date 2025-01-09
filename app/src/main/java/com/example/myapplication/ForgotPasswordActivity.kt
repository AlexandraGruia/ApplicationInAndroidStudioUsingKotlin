package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgotpassword)

        val emailInput: EditText = findViewById(R.id.emailInput)
        val doneButton: Button = findViewById(R.id.doneButton)

        doneButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isNotEmpty()) {
                val intent = Intent(this, ForgotPasswordMessage::class.java)
                startActivity(intent)
                finish()
            } else {
                // Show error if any field is missing
                Toast.makeText(this, "Please type a valid email.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

