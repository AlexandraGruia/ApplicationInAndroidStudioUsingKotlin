package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView

class AddAdviceActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_advice)

        val titleInput = findViewById<EditText>(R.id.addAdviceTitle)
        val descriptionInput = findViewById<EditText>(R.id.adviceInput)
        val saveButton = findViewById<Button>(R.id.submitAdviceButton)

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill out both fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val sharedPrefs = getSharedPreferences("AdviceApp", Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            val adviceId = System.currentTimeMillis().toString()

            editor.putString("${adviceId}_title", title)
            editor.putString("${adviceId}_description", description)
            editor.apply()

            Toast.makeText(this, "Advice saved!", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, AdviceActivity::class.java))
            finish()
        }
    }
}
