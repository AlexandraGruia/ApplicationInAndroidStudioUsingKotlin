package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.ComponentActivity

class CinematicActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cinematic)

        // Find the TextView where you will show the cinematic text
        val cinematicText: TextView = findViewById(R.id.cinematicText)

        // Set your cinematic text here
        cinematicText.text = "How was your \nday?"

        // Delay the transition to HomeActivity by 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            // Optionally, finish this activity so the user can't go back to it
            finish()
        }, 2000) // 2 seconds
    }
}
