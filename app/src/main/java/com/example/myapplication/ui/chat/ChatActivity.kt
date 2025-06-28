package com.example.myapplication

import Message
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.adapter.TopicAdapter
import com.example.myapplication.ui.database.TopicDatabase
import com.example.myapplication.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var topicList: MutableList<Topic>
    private lateinit var topicAdapter: TopicAdapter
    private lateinit var categoriesMenu: LinearLayout
    private var currentCategory: String? = null

    private val dbHelper = TopicDatabase.instance
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        lifecycleScope.launch { deleteInactiveTopics() }

        setupBottomNavigation()
        setupViews()

        recyclerView = findViewById(R.id.recyclerView)
        topicList = mutableListOf()

        topicAdapter = TopicAdapter(
            topicList,
            isAdmin = getSharedPreferences("user_data", MODE_PRIVATE).getBoolean("isAdmin", false),
            onItemClick = { topic -> handleJoin(topic) },
            onDeleteClick = { topic -> confirmDeleteTopic(topic) }
        )

        recyclerView.adapter = topicAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val topics = dbHelper.getAllTopics()
            topicList.addAll(topics)
            topicAdapter.notifyDataSetChanged()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_chat
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_chat -> true
                R.id.bottom_home -> navigateTo(HomeActivity::class.java)
                R.id.bottom_newnote -> navigateTo(NewNoteActivity::class.java)
                R.id.bottom_album -> navigateTo(AlbumActivity::class.java)
                R.id.bottom_advice -> navigateTo(AdviceActivity::class.java)
                else -> false
            }
        }
    }

    private fun setupViews() {
        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageButton>(R.id.fab_create_topic).setOnClickListener {
            startActivity(Intent(this, CreateTopicActivity::class.java))
        }

        categoriesMenu = findViewById(R.id.categoriesMenu)
        findViewById<ImageView>(R.id.menuIcon).setOnClickListener {
            categoriesMenu.visibility =
                if (categoriesMenu.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        val categories = listOf(
            R.id.categoryC1,
            R.id.categoryC2,
            R.id.categoryC3,
            R.id.categoryC4,
            R.id.mostRecent,
            R.id.mostPopular,
            R.id.categoryFavorite
        ).map { findViewById<TextView>(it) }

        categories.forEach { category ->
            category.setOnClickListener {
                categories.forEach {
                    it.isSelected = false
                    it.setBackgroundResource(R.drawable.default_category_background)
                }
                category.isSelected = true
                category.setBackgroundResource(R.drawable.category_selected_background)
                lifecycleScope.launch {
                    filterTopicsByCategory(category.text.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            deleteInactiveTopics()
            val updatedList = dbHelper.getAllTopics()
            currentCategory?.let {
                filterTopicsByCategory(it)
            } ?: run {
                topicList.clear()
                topicList.addAll(updatedList)
                topicAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun confirmDeleteTopic(topic: Topic) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete topic \"${topic.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    dbHelper.deleteTopicById(topic.id)
                    getSharedPreferences("messages", MODE_PRIVATE).edit().remove(topic.id).apply()
                    Toast.makeText(this@ChatActivity, "Topic deleted", Toast.LENGTH_SHORT).show()
                    val topics = dbHelper.getAllTopics()
                    topicAdapter.updateList(topics)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun deleteInactiveTopics() = withContext(Dispatchers.IO) {
        val topics = dbHelper.getAllTopics()
        val sharedPrefs = getSharedPreferences("messages", MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val now = System.currentTimeMillis()
        val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000

        for (topic in topics) {
            val json = sharedPrefs.getString(topic.id, "[]") ?: "[]"
            val messages: List<Message> = gson.fromJson(json, object : TypeToken<List<Message>>() {}.type)
            val lastTimestamp = messages.maxOfOrNull { it.timestamp } ?: topic.timestamp
            if (now - lastTimestamp > thirtyDaysMillis) {
                dbHelper.deleteTopicById(topic.id)
                editor.remove(topic.id)
            }
        }
        editor.apply()
    }

    private fun handleJoin(topic: Topic) {
        if (topic.isJoined) {
            Toast.makeText(this, "You are already part of this topic!", Toast.LENGTH_SHORT).show()
        } else {
            topic.isJoined = true
            Toast.makeText(this, "You joined topic: ${topic.name}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TopicActivity::class.java)
            intent.putExtra("TOPIC_ID", topic.id)
            intent.putExtra("TOPIC_NAME", topic.name)
            startActivity(intent)
        }
    }

    private suspend fun filterTopicsByCategory(category: String) {
        currentCategory = category

        if (category == "Favorite") {
            loadFavoriteTopicsForUser()
            return
        }

        val filteredTopics = withContext(Dispatchers.IO) {
            dbHelper.getTopicsByCategory(category)
        }

        val emptyView = findViewById<TextView>(R.id.emptyView)
        if (filteredTopics.isEmpty()) {
            emptyView.text = "No topics found for category: $category"
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        topicAdapter.updateList(filteredTopics)
    }


    private fun navigateTo(activity: Class<*>): Boolean {
        startActivity(Intent(this, activity))
        finish()
        return true
    }

    private suspend fun loadFavoriteTopicsForUser() {
        val userEmail = getSharedPreferences("user_data", MODE_PRIVATE)
            .getString("email", "") ?: ""

        val favoriteTopicIds = dbHelper.getFavoriteTopicIdsForUser(userEmail)

        val favoriteTopics = dbHelper.getTopicsByIds(favoriteTopicIds)

        runOnUiThread {
            val emptyView = findViewById<TextView>(R.id.emptyView)
            if (favoriteTopics.isEmpty()) {
                emptyView.text = "No favorite topics found."
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
            topicAdapter.updateList(favoriteTopics)
        }
    }



}
