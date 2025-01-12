package com.example.myapplication;

import android.annotation.SuppressLint
import android.content.Context
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
    private lateinit var adviceTitle1: TextView
    private lateinit var adviceTitle2: TextView
    private lateinit var adviceTitle3: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        postsContainer = findViewById(R.id.postsContainer)
        morePostsContainer = findViewById(R.id.morePostsContainer)

        val recentAdvice1 = findViewById<TextView>(R.id.recentAdvice1)
        val recentAdvice2 = findViewById<TextView>(R.id.recentAdvice2)
        val recentAdvice3 = findViewById<TextView>(R.id.recentAdvice3)


        val sharedPrefs = getSharedPreferences("AdviceApp", Context.MODE_PRIVATE)
        val allEntries = sharedPrefs.all
        val adviceList = mutableListOf<Pair<String, String>>() // (id, title)

        for ((key, value) in allEntries) {
            if (key.endsWith("_title")) {
                val title = value.toString()
                val adviceId = key.removeSuffix("_title")
                adviceList.add(adviceId to title)
            }
        }

        adviceList.sortByDescending { it.first }

        if (adviceList.isNotEmpty()) {
            recentAdvice1.text = adviceList[0].second
            recentAdvice1.visibility = TextView.VISIBLE
        }
        if (adviceList.size > 1) {
            recentAdvice2.text = adviceList[1].second
            recentAdvice2.visibility = TextView.VISIBLE
        }
        if (adviceList.size > 2) {
            recentAdvice3.text = adviceList[2].second
            recentAdvice3.visibility = TextView.VISIBLE
        }


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


