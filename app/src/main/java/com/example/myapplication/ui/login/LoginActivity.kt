package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.database.UserDatabaseHelper
import com.example.myapplication.ui.utils.CinematicActivity
import com.example.myapplication.ui.utils.StatisticsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {

    private val dbHelper = UserDatabaseHelper.instance
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient

    private var isPasswordVisible = false

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let { handleGoogleSignIn(it) }
            } catch (e: ApiException) {
                Log.e("LoginActivity", "Google sign in failed: ${e.statusCode}")
                Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val greetingText: TextView = findViewById(R.id.greetingText)
        val forgotPassword: TextView = findViewById(R.id.forgotPassword)
        val signupText: TextView = findViewById(R.id.signupText)
        val googleSignInText: TextView = findViewById(R.id.googleSignInText)

        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInText.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        val savedEmail = sharedPreferences.getString("email", null)

        lifecycleScope.launch {
            try {
                dbHelper.addAdminIfNotExists()

                if (savedEmail != null) {
                    val user = dbHelper.getUserByEmail(savedEmail)
                    withContext(Dispatchers.Main) {
                        val username = user?.name ?: "Unknown"
                        greetingText.text = "Glad to see you again, \n $username"
                        greetingText.visibility = View.VISIBLE
                        emailInput.visibility = View.INVISIBLE
                        passwordInput.requestFocus()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        greetingText.text = "Please log in."
                        greetingText.visibility = View.INVISIBLE
                        emailInput.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error in onCreate: ${e.message}")
            }
        }

        passwordInput.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = passwordInput.compoundDrawables[drawableEnd]
                if (drawable != null) {
                    val touchX = event.x.toInt()
                    val width = passwordInput.width
                    val paddingEnd = passwordInput.paddingEnd
                    if (touchX >= width - paddingEnd - drawable.intrinsicWidth) {
                        isPasswordVisible = !isPasswordVisible
                        if (isPasswordVisible) {
                            passwordInput.inputType =
                                android.text.InputType.TYPE_CLASS_TEXT or
                                        android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.ic_show, 0
                            )
                        } else {
                            passwordInput.inputType =
                                android.text.InputType.TYPE_CLASS_TEXT or
                                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                            passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.ic_show, 0
                            )
                        }
                        passwordInput.setSelection(passwordInput.text.length)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        passwordInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER
            ) {
                val email = savedEmail ?: emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()

                if (password.isEmpty()) {
                    Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                lifecycleScope.launch {
                    try {
                        val user = dbHelper.getUserByEmail(email)
                        if (user != null) {
                            val isAuthenticated = dbHelper.authenticateUser(email, password)
                            withContext(Dispatchers.Main) {
                                if (isAuthenticated) {
                                    sharedPreferences.edit()
                                        .putString("email", user.email)
                                        .putBoolean("isAdmin", user.isAdmin)
                                        .apply()

                                    greetingText.text = "Glad to see you again, ${user.name}"

                                    val intent = if (user.isAdmin) {
                                        Intent(this@LoginActivity, StatisticsActivity::class.java)
                                    } else {
                                        Intent(this@LoginActivity, CinematicActivity::class.java).apply {
                                            putExtra("username", user.name)
                                            putExtra("isAdmin", user.isAdmin)
                                        }
                                    }

                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Incorrect password. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Email not found. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "Error during login: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@LoginActivity,
                                "An error occurred. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                true
            } else {
                false
            }
        }

        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun handleGoogleSignIn(account: GoogleSignInAccount) {
        val email = account.email ?: ""
        val name = account.displayName ?: "User"

        lifecycleScope.launch {
            try {
                var existingUser = dbHelper.getUserByEmail(email)
                if (existingUser == null) {
                    val created = dbHelper.addGoogleUser(name, email, isAdmin = false)
                    if (!created) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Failed to create account.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                    existingUser = dbHelper.getUserByEmail(email)
                }

                withContext(Dispatchers.Main) {
                    sharedPreferences.edit()
                        .putString("email", email)
                        .putBoolean("isAdmin", existingUser?.isAdmin ?: false)
                        .apply()

                    val intent = if (existingUser?.isAdmin == true) {
                        Intent(this@LoginActivity, StatisticsActivity::class.java)
                    } else {
                        Intent(this@LoginActivity, CinematicActivity::class.java).apply {
                            putExtra("username", name)
                            putExtra("isAdmin", false)
                        }
                    }
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Google sign-in processing error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "An error occurred during login.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}