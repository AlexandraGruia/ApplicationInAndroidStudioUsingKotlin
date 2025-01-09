package com.example.myapplication;

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView


class HomeActivity  : ComponentActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var postsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        postsContainer = findViewById(R.id.postsContainer)

        val postText = intent.getStringExtra("postText")
        postText?.let {
            displayPost(it)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.bottom_home
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_home -> return@setOnItemSelectedListener true
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

                R.id.bottom_chat-> {
                    startActivity(
                        Intent(
                            applicationContext,
                            ChatActivity::class.java
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
    }

    private fun displayPost(text: String) {
        val postView = TextView(this)
        postView.text = text
        postView.textSize = 18f
        postView.setPadding(8, 8, 8, 8)
        postsContainer.addView(postView)
    }
}


