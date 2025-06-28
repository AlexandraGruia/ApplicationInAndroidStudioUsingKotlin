package com.example.myapplication

import Message
import MessageAdapter
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.database.TopicDatabase
import com.example.myapplication.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TopicActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var topicNameTextView: TextView
    private lateinit var favoriteButton: ImageButton
    private lateinit var topicId: String
    private lateinit var currentUserId: String
    private var isFavorite = false

    private val dbHelper = TopicDatabase.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic)

        topicId = intent.getStringExtra("TOPIC_ID") ?: "default_topic_id"
        val topicName = intent.getStringExtra("TOPIC_NAME") ?: "Unnamed Topic"

        if (topicId == "default_topic_id") {
            Toast.makeText(this, "Running in test mode with default topic.", Toast.LENGTH_SHORT).show()
        }

        currentUserId = getSharedPreferences("user_data", MODE_PRIVATE).getString("email", "") ?: ""

        setupViews(topicName)
        setupRecyclerView()
        setupBottomNavigation()

        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageView>(R.id.exitIcon).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun setupViews(topicName: String) {
        favoriteButton = findViewById(R.id.favoriteButton)
        topicNameTextView = findViewById(R.id.topicNameTextView)
        topicNameTextView.text = topicName

        lifecycleScope.launch {
            isFavorite = checkIfFavorite(currentUserId, topicId)
            updateFavoriteIcon()
        }

        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon()
            saveFavoriteStatus(topicId, isFavorite)
        }
    }

    private fun setupRecyclerView() {
        val messagesRecyclerView: RecyclerView = findViewById(R.id.messagesRecyclerView)
        val messageInput: EditText = findViewById(R.id.messageInput)
        val sendButton: ImageButton = findViewById(R.id.sendButton)

        messages.addAll(loadMessages(topicId))

        messageAdapter = MessageAdapter(messages, currentUserId, topicId, this)
        messagesRecyclerView.adapter = messageAdapter
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message(text = messageText, idUser = currentUserId)
                messageAdapter.addMessage(message)
                messageInput.text.clear()
                messagesRecyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_chat -> navigateTo(ChatActivity::class.java)
                R.id.bottom_home -> navigateTo(HomeActivity::class.java)
                R.id.bottom_newnote -> navigateTo(NewNoteActivity::class.java)
                R.id.bottom_album -> navigateTo(AlbumActivity::class.java)
                R.id.bottom_advice -> navigateTo(AdviceActivity::class.java)
                else -> false
            }
        }
    }

    private fun updateFavoriteIcon() {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_red else R.drawable.ic_favorite
        )
    }

    private fun saveFavoriteStatus(topicId: String, isFav: Boolean) {
        lifecycleScope.launch {
            dbHelper.setFavoriteForUser(currentUserId, topicId, isFav)
        }
    }


    private fun loadFavoriteStatus(topicId: String): Boolean {
        return getSharedPreferences("favorites", MODE_PRIVATE)
            .getBoolean(topicId, false)
    }

    private fun loadMessages(topicId: String): List<Message> {
        val json = getSharedPreferences("messages", MODE_PRIVATE).getString(topicId, "[]") ?: "[]"
        val type = object : TypeToken<List<Message>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun navigateTo(activity: Class<*>): Boolean {
        startActivity(Intent(this, activity))
        finish()
        return true
    }

    private suspend fun checkIfFavorite(email: String, topicId: String): Boolean {
        return try {
            val favDoc = dbHelper.firestore.collection("users")
                .document(email.trim().lowercase())
                .collection("favorites")
                .document(topicId)
                .get()
                .await()
            favDoc.exists()
        } catch (e: Exception) {
            false
        }
    }


}
