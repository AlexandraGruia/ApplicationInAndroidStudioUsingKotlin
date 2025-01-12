package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class ChatActivity: ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var topicList: MutableList<Topic>
    private lateinit var topicAdapter: TopicAdapter
    private lateinit var categoriesMenu: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_chat
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_chat -> return@setOnItemSelectedListener true
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

        recyclerView = findViewById(R.id.recyclerView)

        topicList = loadTopics().toMutableList()
        topicAdapter = TopicAdapter(topicList, this) { topic ->
            if (topic.name.isNotEmpty()) {
                val intent = Intent(this, TopicActivity::class.java)
                intent.putExtra("topicId", topic.id)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Topic name is empty", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.adapter = topicAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fabCreateTopic = findViewById<ImageButton>(R.id.fab_create_topic)
        fabCreateTopic.setOnClickListener {
            startActivity(Intent(this, CreateTopicActivity::class.java))
        }

        val menuIcon = findViewById<ImageView>(R.id.menuIcon)
        categoriesMenu = findViewById(R.id.categoriesMenu)

        menuIcon.setOnClickListener {
            categoriesMenu.visibility =
                if (categoriesMenu.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        val categoryC1 = findViewById<TextView>(R.id.categoryC1)
        val categoryC2 = findViewById<TextView>(R.id.categoryC2)
        val categoryC3 = findViewById<TextView>(R.id.categoryC3)
        val categoryC4 = findViewById<TextView>(R.id.categoryC4)
        val mostRecent = findViewById<TextView>(R.id.mostRecent)
        val mostPopular = findViewById<TextView>(R.id.mostPopular)

        val categories = listOf(categoryC1, categoryC2, categoryC3, categoryC4, mostRecent, mostPopular)

        categories.forEach { category ->
            category.setOnClickListener {
                categories.forEach { cat ->
                    cat.isSelected = false
                    cat.setBackgroundResource(R.drawable.default_category_background)
                }

                it.isSelected = true
                it.setBackgroundResource(R.drawable.category_selected_background)

                filterTopicsByCategory(category.text.toString())
            }
        }
    }

    private fun loadTopics(): List<Topic> {
        val sharedPreferences = getSharedPreferences("topics", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("topicListJson", "[]") ?: "[]"
        Log.d("ChatActivity", "Loaded topics JSON: $json")

        return try {
            val type = object : TypeToken<List<Topic>>() {}.type
            gson.fromJson<List<Topic>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("ChatActivity", "Failed to load topics: ${e.message}")
            emptyList()
        }
    }

    override fun onResume() {
        super.onResume()

        val newTopicList = loadTopics()
        if (newTopicList != topicList) {
            topicList.clear()
            topicList.addAll(newTopicList)
            topicAdapter.notifyDataSetChanged()
        }
    }


    private fun handleJoin(topicId: String) {
        val topicList = loadTopics()
        val topic = topicList.find { it.id == topicId }

        if (topic != null) {
            Toast.makeText(this, "You joined topic: ${topic.name}", Toast.LENGTH_SHORT).show()
            Log.d("JoinActivity", "Joined topic: $topic")

            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("TOPIC_ID", topic.id)
            startActivity(intent)
        } else {
            Log.e("JoinActivity", "Topic ID not found")
            Toast.makeText(this, "Topic not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterTopicsByCategory(category: String) {
        val filteredTopics = when (category) {
            "Most Recent" -> topicList.sortedByDescending { it.timestamp }
            "Most Popular" -> topicList.sortedByDescending { it.peopleCount }
            else -> topicList.filter { it.category == category }
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



}
