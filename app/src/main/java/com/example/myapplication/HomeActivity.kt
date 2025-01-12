package com.example.myapplication;

import android.annotation.SuppressLint
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
    private lateinit var morePostsContainer: LinearLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        postsContainer = findViewById(R.id.postsContainer)
        morePostsContainer = findViewById(R.id.morePostsContainer)

        val recentPostText = intent.getStringExtra("postText")
        val recentPostDate = intent.getStringExtra("postDate")
        val recentPostImageResId = intent.getIntExtra("postImage", R.drawable.default_image)


        if (recentPostText != null && recentPostDate != null) {
            displayPost(recentPostText, recentPostDate, recentPostImageResId)
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

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)

            startActivity(intent)
        }
    }

    private fun displayPost(text: String, date: String, imageResId: Int) {
        val postView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.drawable.post_background)
        }

        val postImage = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
            )
            setImageResource(imageResId)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val postText = TextView(this).apply {
            this.text = text
            textSize = 18f
            setPadding(8, 8, 8, 8)
        }

        val postDate = TextView(this).apply {
            this.text = date
            textSize = 14f
            setPadding(8, 8, 8, 8)
        }

        postView.addView(postImage)
        postView.addView(postText)
        postView.addView(postDate)

        postsContainer.addView(postView)
    }
}


