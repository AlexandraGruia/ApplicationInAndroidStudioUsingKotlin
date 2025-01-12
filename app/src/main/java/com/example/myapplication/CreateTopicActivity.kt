package com.example.myapplication

import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class CreateTopicActivity : AppCompatActivity() {

    private lateinit var topicNameEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var peopleCountEditText: EditText
    private lateinit var doneButton: Button

    private lateinit var dbHelper: TopicDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_topic)

        dbHelper = TopicDatabase(this)

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

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        topicNameEditText = findViewById(R.id.topicNameEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        peopleCountEditText = findViewById(R.id.peopleCountEditText)
        doneButton = findViewById(R.id.doneButton)

        val categories = resources.getStringArray(R.array.category_list)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        doneButton.setOnClickListener {
            val topicName = topicNameEditText.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val peopleCount = peopleCountEditText.text.toString().toIntOrNull() ?: 0

            if (topicName.isEmpty()) {
                Toast.makeText(this, "Topic name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (peopleCount <= 0) {
                Toast.makeText(this, "People count must be greater than zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (topicName.isNotEmpty() && peopleCount > 0) {
                saveTopic(topicName, category, peopleCount)
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTopic(topicName: String, category: String, peopleCount: Int) {
        val trimmedTopicName = topicName.trim()

        val existingTopics = dbHelper.getAllTopics()
        if (existingTopics.any { it.name.equals(trimmedTopicName, ignoreCase = true) }) {
            Toast.makeText(this, "Topic with this name already exists", Toast.LENGTH_SHORT).show()
            return
        }

        val topicId = UUID.randomUUID().toString()
        val newTopic = Topic(id = topicId, name = trimmedTopicName, category = category.trim(), peopleCount = peopleCount, timestamp = System.currentTimeMillis())

        val result = dbHelper.insertTopic(newTopic)
        if (result != -1L) {
            Toast.makeText(this, "Topic created successfully", Toast.LENGTH_SHORT).show()
            Log.d("CreateTopicActivity", "Topic saved: $newTopic")

            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)

            finish()

        } else {
            Toast.makeText(this, "Failed to create topic", Toast.LENGTH_SHORT).show()
        }
    }

}
