package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.util.Calendar
import java.util.regex.Pattern

class SignUpActivity : ComponentActivity() {

    private lateinit var dbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        dbHelper = UserDatabaseHelper(this)

        val nameInput: EditText = findViewById(R.id.nameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val dateOfBirthInput: EditText = findViewById(R.id.dateofbirthInput)
        val signUpButton: Button = findViewById(R.id.signUpButton)

        // Deschidem un dialog pentru selectarea datei de naștere
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
                Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Validăm email-ul
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Verificăm dacă email-ul este deja folosit
            val cursor = dbHelper.getUserByEmail(email)
            cursor?.let {
                if (it.count > 0) {
                    Toast.makeText(
                        this,
                        getString(R.string.error_email_already_used),
                        Toast.LENGTH_SHORT
                    ).show()
                    it.close()
                    return@setOnClickListener
                }
                it.close()
            }

            // Validăm parola
            if (!isValidPassword(password)) {
                Toast.makeText(this, getString(R.string.error_invalid_password), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Adăugăm utilizatorul în baza de date
            dbHelper.addUser(name, email, password)

            Toast.makeText(this, getString(R.string.success_account_created), Toast.LENGTH_SHORT)
                .show()

            // Navigăm înapoi la pagina de login (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
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
        val passwordPattern =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!])(?=\\S+\$).{8,}\$"
        return Pattern.compile(passwordPattern).matcher(password).matches()
    }
}
