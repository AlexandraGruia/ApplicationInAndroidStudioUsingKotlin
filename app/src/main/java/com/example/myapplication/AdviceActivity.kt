package com.example.myapplication

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

    private lateinit var recyclerView: RecyclerView
    private lateinit var adviceAdapter: AdviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advice)

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
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

            }
            false
        }

        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)

            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerViewForum)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adviceList = listOf(
            Advice("Tip 1", "Learn Kotlin!","t"),
            Advice("Tip 2", "Understand Android lifecycle.","t"),
            Advice("Tip 3", "Use RecyclerView for lists.","t")
        )

        adviceAdapter = AdviceAdapter(adviceList) { advice ->
            if (advice.title.isNotEmpty() && advice.fullContent.isNotEmpty()) {
                val intent = Intent(this, AdviceDetailActivity::class.java).apply {
                    putExtra("title", advice.title)
                    putExtra("description", advice.description)
                    putExtra("fullContent", advice.fullContent)
                }
                startActivity(intent)
            } else {
                Log.e("AdviceActivity", "Datele sunt invalide: Titlu sau conținut lipsă")
            }
        }

        recyclerView.adapter = adviceAdapter
    }
}