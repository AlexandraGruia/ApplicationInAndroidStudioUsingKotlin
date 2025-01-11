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

        recyclerView = findViewById(R.id.recyclerView)

        // Încărcăm lista de topicuri
        topicList = loadTopics().toMutableList()
        topicAdapter = TopicAdapter(topicList) { topic ->
            Toast.makeText(this, "Joining topic: ${topic.name}", Toast.LENGTH_SHORT).show()
            // Logica pentru navigare sau alte acțiuni
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
            categoriesMenu.visibility = if (categoriesMenu.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    private fun loadTopics(): List<Topic> {
        val sharedPreferences = getSharedPreferences("topics", MODE_PRIVATE)
        val gson = Gson()

        // Citim lista de topicuri din SharedPreferences (ca un string JSON)
        val existingTopicsJson = sharedPreferences.getString("topicListJson", "[]") ?: "[]"
        val type = object : TypeToken<List<Topic>>() {}.type

        // Deserializăm JSON-ul într-o listă de obiecte Topic
        return gson.fromJson(existingTopicsJson, type)
    }



    override fun onResume() {
        super.onResume()

        // Reîncarcă topicurile
        topicList.clear()  // Golește lista curentă
        topicList.addAll(loadTopics())  // Încarcă topicurile din SharedPreferences

        // Notifică adapterul să se actualizeze
        topicAdapter.notifyDataSetChanged()
    }
}
