package com.example.myapplication.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.AdviceActivity
import com.example.myapplication.AlbumActivity
import com.example.myapplication.ChatActivity
import com.example.myapplication.HomeActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.NewNoteActivity
import com.example.myapplication.R
import com.example.myapplication.ui.database.UserDatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val dbHelper = UserDatabaseHelper.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_chat -> {
                    startActivity(Intent(applicationContext, ChatActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_newnote -> {
                    startActivity(Intent(applicationContext, NewNoteActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_album -> {
                    startActivity(Intent(applicationContext, AlbumActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_advice -> {
                    startActivity(Intent(applicationContext, AdviceActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        nameInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val username = nameInput.text.toString()
                val email = sharedPreferences.getString("email", null)
                if (email != null) {
                    lifecycleScope.launch {
                        val userUpdated = dbHelper.updateUsername(email, username)
                        runOnUiThread {
                            if (userUpdated) {
                                sharedPreferences.edit().putString("username", username).apply()
                                Toast.makeText(this@SettingsActivity, "Username updated in database.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SettingsActivity, "Failed to update username.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = passwordInput.text.toString()
                val email = sharedPreferences.getString("email", null)
                if (email != null) {
                    if (password.isNotEmpty()) {
                        lifecycleScope.launch {
                            val userUpdated = dbHelper.updatePassword(email, password)
                            runOnUiThread {
                                if (userUpdated) {
                                    Toast.makeText(this@SettingsActivity, "Password updated in database.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@SettingsActivity, "Failed to update password.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val emailInput = findViewById<EditText>(R.id.emailInput)
        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newEmail = emailInput.text.toString()
                val oldEmail = sharedPreferences.getString("email", null)
                if (!newEmail.isNullOrEmpty() && !oldEmail.isNullOrEmpty() && newEmail != oldEmail) {
                    lifecycleScope.launch {
                        val userUpdated = dbHelper.updateEmail(oldEmail, newEmail)
                        runOnUiThread {
                            if (userUpdated) {
                                sharedPreferences.edit().putString("email", newEmail).apply()
                                Toast.makeText(this@SettingsActivity, "Email updated in database.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SettingsActivity, "Failed to update email.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else if (newEmail.isEmpty()) {
                    Toast.makeText(this, "Email cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val notificationsSwitch = findViewById<Switch>(R.id.notificationsSwitch)
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Notifications enabled" else "Notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            sharedPreferences.edit().remove("email").apply()

            Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}


