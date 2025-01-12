package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        val nameInput: EditText = findViewById(R.id.nameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val dateOfBirthInput: EditText = findViewById(R.id.dateofbirthInput) // Assuming DOB is an additional field
        val signUpButton: Button = findViewById(R.id.signUpButton)

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)

            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val dateOfBirth = dateOfBirthInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || dateOfBirth.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (emailAlreadyUsed(email)) {
                Toast.makeText(this, "This email is already used. Try another one.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidDateOfBirth(dateOfBirth)) {
                Toast.makeText(this, "Invalid date of birth. Please use the correct format.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

        }
    }



    private fun emailAlreadyUsed(email: String): Boolean {
        val existingEmails = listOf("test@example.com", "user@example.com")
        return existingEmails.contains(email)
    }

    private fun isValidDateOfBirth(dob: String): Boolean {
        val dateOfBirth = Regex("""\d{4}-\d{2}-\d{2}""")
        return dob.matches(dateOfBirth)
    }
}
