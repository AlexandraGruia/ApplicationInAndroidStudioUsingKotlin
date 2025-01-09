package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Reference to views
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val forgotPassword: TextView = findViewById(R.id.forgotPassword)
        val signupText: TextView = findViewById(R.id.signupText)
        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)

        passwordInput.requestFocus()

        // Listen for the Enter key press (IME_ACTION_DONE or KeyEvent.KEYCODE_ENTER)
        passwordInput.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val password = passwordInput.text.toString()

                if (password.isNotEmpty()) {
                    // Navigate to the next activity (e.g., HomeActivity)
                    val intent = Intent(this, CinematicActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // You can add a message or logic here to show an error if the password is empty
                    passwordInput.error = "Please enter a password"
                }
                true // Returning true indicates the action is handled
            } else {
                false
            }
        }
        // Handle clicks
        forgotPassword.setOnClickListener {
            // Add your logic for "Forgot password?"
            // Navigate to ForgotPasswordActivity
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        signupText.setOnClickListener {
            // Add your logic for "Don’t have an account?"
            // Navigate to SignUpActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Set a click listener to navigate to SettingsActivity when clicked
        settingsIcon.setOnClickListener {
            // Create an Intent to navigate to the SettingsActivity
            val intent = Intent(this, SettingsActivity::class.java)

            // Start the activity
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