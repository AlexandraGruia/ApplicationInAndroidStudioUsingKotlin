 package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


 class TopicActivity : AppCompatActivity() {

     private lateinit var messageAdapter: MessageAdapter
     private val messages = mutableListOf<Message>()
     private lateinit var topic: Topic
     private lateinit var topicNameTextView: TextView

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_topic)


         val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
         bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
             when (item.itemId) {
                 R.id.bottom_chat -> {
                     startActivity(
                         Intent(
                             applicationContext,
                             ChatActivity::class.java
                         )
                     )
                     finish()
                     return@setOnItemSelectedListener true
                 }

                 R.id.bottom_home -> {
                     startActivity(
                         Intent(
                             applicationContext,
                             HomeActivity::class.java
                         )
                     )
                     finish()
                     return@setOnItemSelectedListener true
                 }

                 R.id.bottom_newnote -> {
                     startActivity(
                         Intent(
                             applicationContext,
                             NewNoteActivity::class.java
                         )
                     )
                     finish()
                     return@setOnItemSelectedListener true
                 }

                 R.id.bottom_album -> {
                     startActivity(
                         Intent(
                             applicationContext,
                             AlbumActivity::class.java
                         )
                     )
                     finish()
                     return@setOnItemSelectedListener true
                 }

                 R.id.bottom_advice -> {
                     startActivity(
                         Intent(
                             applicationContext,
                             AdviceActivity::class.java
                         )
                     )
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


         val topicId = intent.getStringExtra("TOPIC_ID") ?: "default_topic_id"
         val topicName = intent.getStringExtra("TOPIC_NAME") ?: "Unnamed Topic"

         if (topicId == "default_topic_id") {
             Toast.makeText(this, "Running in test mode with default topic.", Toast.LENGTH_SHORT).show()
         }


         topicNameTextView = findViewById(R.id.topicNameTextView)
         topicNameTextView.text = topicName


         val messagesRecyclerView: RecyclerView = findViewById(R.id.messagesRecyclerView)
         val messageInput: EditText = findViewById(R.id.messageInput)
         val sendButton: ImageButton = findViewById(R.id.sendButton)


         messages.addAll(loadMessages(topicId))
         messageAdapter = MessageAdapter(messages)
         messagesRecyclerView.adapter = messageAdapter
         messagesRecyclerView.layoutManager = LinearLayoutManager(this)

         sendButton.setOnClickListener {
             val messageText = messageInput.text.toString()
             if (messageText.isNotEmpty()) {
                 val userMessage = Message(messageText, isUser = true)
                 messages.add(userMessage)
                 messageAdapter.notifyItemInserted(messages.size - 1)

                 saveMessage(topicId, userMessage)
                 messageInput.text.clear()
                 messagesRecyclerView.scrollToPosition(messages.size - 1)
             }
         }

     }
     private fun loadMessages(topicId: String): List<Message> {
         val sharedPreferences = getSharedPreferences("messages", MODE_PRIVATE)
         val gson = Gson()
         val json = sharedPreferences.getString(topicId, "[]") ?: "[]"
         val type = object : TypeToken<List<Message>>() {}.type
         return gson.fromJson(json, type)
     }

     private fun saveMessage(topicId: String, message: Message) {
         val sharedPreferences = getSharedPreferences("messages", MODE_PRIVATE)
         val editor = sharedPreferences.edit()
         val gson = Gson()

         val json = sharedPreferences.getString(topicId, "[]") ?: "[]"
         val type = object : TypeToken<List<Message>>() {}.type
         val messages: MutableList<Message> = gson.fromJson(json, type)

         messages.add(message)

         val updatedJson = gson.toJson(messages)
         editor.putString(topicId, updatedJson).apply()
     }

     private fun navigateTo(activity: Class<*>) {
         startActivity(Intent(applicationContext, activity))
         finish()
     }
 }


