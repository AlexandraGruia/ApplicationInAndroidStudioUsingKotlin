package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
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
        Log.d("MainActivity", "Saved email: $savedEmail")
        if (savedEmail != null) {
            val user = dbHelper.getUserByEmail(savedEmail)
            val username = user?.get("name") ?: "Unknown"

            Log.d("MainActivity", "Username: $username")

            greetingText.text = "Glad to see you again, \n $username"
            greetingText.visibility = View.VISIBLE
            emailInput.visibility = View.INVISIBLE
            passwordInput.requestFocus()

        } else {
            greetingText.text = "Please log in."
            greetingText.visibility = View.INVISIBLE
            emailInput.visibility = View.VISIBLE
        }

        passwordInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val email = savedEmail ?: emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()

                if (password.isEmpty()) {
                    Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                val user = dbHelper.getUserByEmail(email)
                if (user != null) {
                    val isAuthenticated = dbHelper.authenticateUser(email, password)

                    if (isAuthenticated) {
                        sharedPreferences.edit().putString("email", email).apply()

                        val username = user["name"] ?: "Unknown"
                        greetingText.text = "Glad to see you again, $username"

                        val intent = Intent(this, CinematicActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Email not found. Please try again.", Toast.LENGTH_SHORT).show()
                }

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