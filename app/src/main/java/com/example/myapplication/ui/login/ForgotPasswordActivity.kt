package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.database.UserDatabaseHelper
import kotlinx.coroutines.launch

class ForgotPasswordActivity : ComponentActivity() {

    private val dbHelper = UserDatabaseHelper.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)

        val emailInput: EditText = findViewById(R.id.emailInput)
        val doneButton: Button = findViewById(R.id.doneButton)

        doneButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = dbHelper.getUserByEmail(email)
                runOnUiThread {
                    if (user != null) {
                        val intent = Intent(this@ForgotPasswordActivity, ForgotPasswordMessage::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, "Email not found in the system.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}