package com.example.myapplication

import android.util.Base64
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ui.database.AdviceDatabase
import com.example.myapplication.ui.database.PostDatabaseHelper
import com.example.myapplication.ui.games.GamesActivity
import com.example.myapplication.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "MoodPrefs"
        private const val KEY_MOOD_ID = "savedMoodId"
        private const val KEY_TIMESTAMP = "savedMoodTimestamp"
        private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
    }

    private lateinit var adviceDatabase: AdviceDatabase
    private lateinit var adviceAdapter: AdviceAdapter
    private lateinit var adviceRecyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        adviceDatabase = AdviceDatabase.instance
        lifecycleScope.launch {
            setupPosts()
            setupAdviceRecyclerView()
        }

        setupMood()
        setupBottomNavigation()
        setupIcons()
    }

    private suspend fun setupPosts() {
        val container = findViewById<LinearLayout>(R.id.postsContainer)

        val sharedPrefs = getSharedPreferences("user_data", MODE_PRIVATE)
        val currentUserEmail = sharedPrefs.getString("email", null)

        if (currentUserEmail == null) {
            runOnUiThread {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val userPosts = PostDatabaseHelper.instance.getPostsForUser(currentUserEmail)
        val lastThreePosts = userPosts.takeLast(3).reversed()

        runOnUiThread { container.removeAllViews() }

        lastThreePosts.forEach { post ->
            runOnUiThread {
                val noteView = layoutInflater.inflate(R.layout.note_item, container, false)

                val noteText = noteView.findViewById<TextView>(R.id.noteText)
                noteText.text = post.text

                val noteDate = noteView.findViewById<TextView>(R.id.noteDate)
                noteDate.text = try {
                    LocalDate.parse(post.date).format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
                } catch (e: Exception) {
                    post.date
                }

                val imageView = noteView.findViewById<ImageView>(R.id.noteImage)

                if (!post.imagePath.isNullOrEmpty()) {
                    imageView.visibility = View.VISIBLE
                    if (post.imagePath.startsWith("http")) {
                        Glide.with(this)
                            .load(post.imagePath)
                            .centerCrop()
                            .placeholder(R.drawable.default_image)
                            .into(imageView)
                    } else {
                        try {
                            val decodedBytes = Base64.decode(post.imagePath, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            imageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            imageView.visibility = View.GONE
                            Log.e("HomeActivity", "Failed to decode Base64 image: ${e.message}")
                        }
                    }
                } else {
                    imageView.visibility = View.GONE
                }

                val openNoteDetail = View.OnClickListener {
                    val intent = Intent(this, NoteDetailActivity::class.java)
                    intent.putExtra("postId", post.id)
                    startActivity(intent)
                }

                noteView.setOnClickListener(openNoteDetail)
                noteText.setOnClickListener(openNoteDetail)
                noteDate.setOnClickListener(openNoteDetail)
                imageView.setOnClickListener(openNoteDetail)

                container.addView(noteView)
            }
        }
    }

    private suspend fun setupAdviceRecyclerView() {
        val adviceList = adviceDatabase.getAllAdvices()
        runOnUiThread {
            adviceRecyclerView = findViewById(R.id.adviceRecyclerView)
            val arrowLeft = findViewById<ImageView>(R.id.arrowLeft)
            val arrowRight = findViewById<ImageView>(R.id.arrowRight)

            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adviceRecyclerView.layoutManager = layoutManager

            adviceAdapter = AdviceAdapter(
                isAdmin = false,
                onItemClicked = { advice ->
                    val intent = Intent(this@HomeActivity, AdviceDetailActivity::class.java)
                    intent.putExtra("advice_id", advice.id)
                    startActivity(intent)
                },
                onEditClicked = {  }
            )

            adviceRecyclerView.adapter = adviceAdapter

            arrowLeft.setOnClickListener {
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                if (firstVisible > 0) {
                    adviceRecyclerView.smoothScrollToPosition(firstVisible - 1)
                }
            }

            arrowRight.setOnClickListener {
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val itemCount = adviceAdapter.itemCount
                if (lastVisible in 0 until itemCount - 1) {
                    adviceRecyclerView.smoothScrollToPosition(lastVisible + 1)
                }
            }
        }
    }


    private fun setupMood() {
        val moodGroup = findViewById<android.widget.RadioGroup>(R.id.moodGroup)
        val moodStatus = findViewById<TextView>(R.id.moodStatus)

        val userEmail = getSharedPreferences("user_data", MODE_PRIVATE).getString("email", null)
        if (userEmail == null) {
            moodGroup.clearCheck()
            moodStatus.text = ""
            return
        }

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val moodKey = "$KEY_MOOD_ID-$userEmail"
        val timestampKey = "$KEY_TIMESTAMP-$userEmail"

        val savedMoodId = prefs.getInt(moodKey, -1)
        val savedTimestamp = prefs.getLong(timestampKey, 0L)
        val currentTime = System.currentTimeMillis()

        if (savedMoodId != -1 && currentTime - savedTimestamp < ONE_DAY_MILLIS) {
            moodGroup.check(savedMoodId)
            moodStatus.text = getMessageForMood(savedMoodId)
        } else {
            moodGroup.clearCheck()
            prefs.edit().remove(moodKey).remove(timestampKey).apply()
        }

        moodGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                prefs.edit()
                    .putInt(moodKey, checkedId)
                    .putLong(timestampKey, System.currentTimeMillis())
                    .apply()
                moodStatus.text = getMessageForMood(checkedId)
            }
        }
    }

    private fun getMessageForMood(checkedId: Int): String {
        return when (checkedId) {
            R.id.moodHappy -> "I'm glad you're happy! 😊"
            R.id.moodSad -> "I'm sorry you're sad. 😔"
            R.id.moodAngry -> "Take a deep breath. Everything will be okay. 😠"
            R.id.moodNeutral -> "A neutral day, sometimes it's good to be quiet. 😐"
            else -> ""
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.bottom_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> true
                R.id.bottom_album -> {
                    startActivity(Intent(applicationContext, AlbumActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_newnote -> {
                    startActivity(Intent(applicationContext, NewNoteActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_chat -> {
                    startActivity(Intent(applicationContext, ChatActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_advice -> {
                    startActivity(Intent(applicationContext, AdviceActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupIcons() {
        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageView>(R.id.gamesIcon).setOnClickListener {
            startActivity(Intent(this, GamesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            setupPosts()
            val updatedAdviceList = adviceDatabase.getAllAdvices()
            adviceAdapter.submitList(updatedAdviceList)
        }
    }
}
