package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.database.TopicDatabase
import com.example.myapplication.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.*

class CreateTopicActivity : AppCompatActivity() {

    private lateinit var topicNameEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var peopleCountEditText: EditText
    private lateinit var doneButton: Button

    private val dbHelper = TopicDatabase.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_topic)

        setupBottomNavigation()
        setupUI()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_chat -> startActivity(Intent(this, ChatActivity::class.java))
                R.id.bottom_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.bottom_newnote -> startActivity(Intent(this, NewNoteActivity::class.java))
                R.id.bottom_album -> startActivity(Intent(this, AlbumActivity::class.java))
                R.id.bottom_advice -> startActivity(Intent(this, AdviceActivity::class.java))
                else -> return@setOnItemSelectedListener false
            }
            finish()
            true
        }

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupUI() {
        topicNameEditText = findViewById(R.id.topicNameEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        peopleCountEditText = findViewById(R.id.peopleCountEditText)
        doneButton = findViewById(R.id.doneButton)

        val categories = resources.getStringArray(R.array.category_list)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        doneButton.setOnClickListener {
            val topicName = topicNameEditText.text.toString().trim()
            val category = categorySpinner.selectedItem.toString().trim()
            val peopleCount = peopleCountEditText.text.toString().toIntOrNull() ?: 0

            if (topicName.isEmpty()) {
                Toast.makeText(this, "Topic name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (peopleCount <= 0) {
                Toast.makeText(this, "People count must be greater than zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveTopic(topicName, category, peopleCount)
        }
    }

    private fun saveTopic(topicName: String, category: String, peopleCount: Int) {
        val topicId = UUID.randomUUID().toString()
        val newTopic = Topic(
            id = topicId,
            name = topicName,
            category = category,
            peopleCount = peopleCount,
            timestamp = System.currentTimeMillis()
        )

        lifecycleScope.launch {
            val existingTopics = dbHelper.getAllTopics()
            if (existingTopics.any { it.name.equals(topicName, ignoreCase = true) }) {
                Toast.makeText(this@CreateTopicActivity, "Topic with this name already exists", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val success = dbHelper.insertTopic(newTopic)
            if (success) {
                Toast.makeText(this@CreateTopicActivity, "Topic created successfully", Toast.LENGTH_SHORT).show()
                Log.d("CreateTopicActivity", "Topic saved: $newTopic")
                startActivity(Intent(this@CreateTopicActivity, ChatActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@CreateTopicActivity, "Failed to create topic", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
