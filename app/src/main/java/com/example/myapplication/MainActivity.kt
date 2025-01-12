package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var dbHelper: UserDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val greetingText: TextView = findViewById(R.id.greetingText)
        val forgotPassword: TextView = findViewById(R.id.forgotPassword)
        val signupText: TextView = findViewById(R.id.signupText)

        dbHelper = UserDatabaseHelper(this)
        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)


        val savedEmail = sharedPreferences.getString("email", null)
        if (savedEmail != null) {
            // If email is saved, show greeting message and ask for password only
            greetingText.setText("Glad to see you again, $savedEmail")
            emailInput.setText(savedEmail) // Pre-fill email
            passwordInput.requestFocus()
        }

        passwordInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }else if(password.isNotEmpty() && email.isNotEmpty()){
                    val intent = Intent(this, CinematicActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                val cursor = dbHelper.readableDatabase.rawQuery(
                    "SELECT * FROM users WHERE email = ?",
                    arrayOf(email)
                )

                if (cursor.moveToFirst()) {
                    val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                    val storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))

                    if (storedPassword == dbHelper.hashPassword(password)) {
                        sharedPreferences.edit().putString("username", username).apply()

                        greetingText.text = "Glad to see you again, \n$username"
                        greetingText.visibility = TextView.VISIBLE

                        val intent = Intent(this, CinematicActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Incorrect email or password. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Email not found. Please sign up first.", Toast.LENGTH_SHORT).show()
                }
                cursor.close()
                true
            } else {
                false
            }
        }

        forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        signupText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}