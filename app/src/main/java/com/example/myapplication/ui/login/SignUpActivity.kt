package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.database.UserDatabaseHelper
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.regex.Pattern

class SignUpActivity : ComponentActivity() {

    private val dbHelper = UserDatabaseHelper.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val nameInput: EditText = findViewById(R.id.nameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val dateOfBirthInput: EditText = findViewById(R.id.dateofbirthInput)
        val signUpButton: Button = findViewById(R.id.signUpButton)

        dateOfBirthInput.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                dateOfBirthInput.setText(selectedDate)
            }
        }

        signUpButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val dateOfBirth = dateOfBirthInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || dateOfBirth.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                Toast.makeText(this, getString(R.string.error_invalid_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success = dbHelper.addUser(name, email, password, dateOfBirth)

                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@SignUpActivity, getString(R.string.success_account_created), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@SignUpActivity, getString(R.string.error_email_already_used), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                onDateSelected(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^.{6,}\$"
        return Pattern.compile(passwordPattern).matcher(password).matches()
    }
}