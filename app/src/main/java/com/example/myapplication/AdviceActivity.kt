package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdviceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advice)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerAdviceView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_advice
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_advice -> return@setOnItemSelectedListener true
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

            }
            false
        }

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)

            startActivity(intent)
        }

        val sharedPrefs = getSharedPreferences("AdviceApp", Context.MODE_PRIVATE)
        val allEntries = sharedPrefs.all
        val adviceList = mutableListOf<Advice>()

        for ((key, value) in allEntries) {
            if (key.endsWith("_title")) {
                val adviceId = key.removeSuffix("_title")
                val title = value.toString()
                val fullDescription = sharedPrefs.getString("${adviceId}_description", "") ?: ""
                val shortDescription = if (fullDescription.length > 50) {
                    "${fullDescription.substring(0, 50)}..."
                } else {
                    fullDescription
                }

                adviceList.add(Advice(title, shortDescription, fullDescription))
            }
        }
        recyclerView.adapter = AdviceAdapter(adviceList) { advice ->
            val intent = Intent(this, AdviceDetailActivity::class.java)
            intent.putExtra("title", advice.title)
            intent.putExtra("description", advice.shortDescription)
            intent.putExtra("fullContent", advice.fullContent)
            startActivity(intent)
        }
    }
}
