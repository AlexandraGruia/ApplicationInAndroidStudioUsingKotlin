package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_chat -> {
                    startActivity(Intent(applicationContext, ChatActivity::class.java))
                    finish()
                    return@setOnItemSelectedListener true
                }

                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                    return@setOnItemSelectedListener true
                }

                R.id.bottom_newnote -> {
                    startActivity(Intent(applicationContext, NewNoteActivity::class.java))
                    finish()
                    return@setOnItemSelectedListener true
                }

                R.id.bottom_album -> {
                    startActivity(Intent(applicationContext, AlbumActivity::class.java))
                    finish()
                    return@setOnItemSelectedListener true
                }

                R.id.bottom_advice -> {
                    startActivity(Intent(applicationContext, AdviceActivity::class.java))
                    finish()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        val nameInput = findViewById<EditText>(R.id.nameInput)
        nameInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val username = nameInput.text.toString()
                Toast.makeText(this, "Username changed to: $username", Toast.LENGTH_SHORT).show()
            }
        }

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = passwordInput.text.toString()
                Toast.makeText(this, "Password updated.", Toast.LENGTH_SHORT).show()
            }
        }

        val emailInput = findViewById<EditText>(R.id.emailInput)
        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = emailInput.text.toString()
                Toast.makeText(this, "Email updated to: $email", Toast.LENGTH_SHORT).show()
            }
        }

        val notificationsSwitch = findViewById<Switch>(R.id.notificationsSwitch)
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Notifications enabled" else "Notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
