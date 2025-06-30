package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.ui.database.PostDatabaseHelper
import com.example.myapplication.ui.models.Post
import com.example.myapplication.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.File

class AlbumDetailActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var albumId: String
    private lateinit var albumName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)

        // Get data from intent
        albumId = intent.getStringExtra("albumId") ?: run {
            finish()
            return
        }
        albumName = intent.getStringExtra("albumName") ?: "Unnamed Album"
        userId = intent.getStringExtra("userId") ?: ""

        Log.d("AlbumDetail", "Received albumId=$albumId, albumName=$albumName, userId=$userId")

        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<TextView>(R.id.titleText).text = albumName

        setupBottomNavigation()
        loadAlbumPhotos()
    }

    private fun setupBottomNavigation() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.bottom_album
        nav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_album -> true
                R.id.bottom_home -> switchTo(HomeActivity::class.java)
                R.id.bottom_newnote -> switchTo(NewNoteActivity::class.java)
                R.id.bottom_chat -> switchTo(ChatActivity::class.java)
                R.id.bottom_advice -> switchTo(AdviceActivity::class.java)
                else -> false
            }
        }
    }

    private fun switchTo(activityClass: Class<*>): Boolean {
        startActivity(Intent(this, activityClass))
        finish()
        return true
    }

    private fun loadAlbumPhotos() {
        lifecycleScope.launch {
            try {
                val postsInAlbum = PostDatabaseHelper.instance
                    .getPostsForAlbum(albumId)
                    .filter { it.userId.equals(userId, ignoreCase = true) }

                runOnUiThread {
                    val gridLayout = findViewById<GridLayout>(R.id.photoGrid)
                    gridLayout.removeAllViews()
                    gridLayout.columnCount = 3

                    val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        windowManager.currentWindowMetrics.bounds.width()
                    } else {
                        val metrics = DisplayMetrics()
                        @Suppress("DEPRECATION")
                        windowManager.defaultDisplay.getMetrics(metrics)
                        metrics.widthPixels
                    }

                    val margin = (8 * resources.displayMetrics.density + 0.5f).toInt()
                    val totalMargin = margin * 4
                    val itemSize = (screenWidth - totalMargin) / 3

                    postsInAlbum.forEachIndexed { index, post ->
                        val container = FrameLayout(this@AlbumDetailActivity).apply {
                            layoutParams = GridLayout.LayoutParams().apply {
                                width = itemSize
                                height = itemSize
                                columnSpec = GridLayout.spec(index % 3)
                                rowSpec = GridLayout.spec(index / 3)
                                setMargins(margin, margin, margin, margin)
                            }
                            setOnClickListener {
                                val intent = Intent(this@AlbumDetailActivity, NoteDetailActivity::class.java)
                                intent.putExtra("postId", post.id)
                                startActivity(intent)
                            }
                        }

                        val imageView = ImageView(this@AlbumDetailActivity).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        }

                        if (!post.imagePath.isNullOrEmpty()) {
                            when {
                                post.imagePath.startsWith("http") -> {
                                    Glide.with(this@AlbumDetailActivity)
                                        .load(post.imagePath)
                                        .placeholder(R.drawable.default_image)
                                        .into(imageView)
                                }
                                File(post.imagePath).exists() -> {
                                    Glide.with(this@AlbumDetailActivity)
                                        .load(File(post.imagePath))
                                        .placeholder(R.drawable.default_image)
                                        .into(imageView)
                                }
                                else -> {
                                    try {
                                        val decodedBytes = Base64.decode(post.imagePath, Base64.DEFAULT)
                                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                        imageView.setImageBitmap(bitmap)
                                    } catch (e: Exception) {
                                        imageView.setImageResource(R.drawable.default_image)
                                        Log.e("AlbumDetail", "Base64 decode error: ${e.message}")
                                    }
                                }
                            }
                        } else {
                            imageView.setImageResource(R.drawable.default_image)
                        }

                        val dateLabel = TextView(this@AlbumDetailActivity).apply {
                            text = formatDateShort(post.date)
                            setTextColor(Color.WHITE)
                            textSize = 14f
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            setBackgroundResource(R.drawable.date_label_background)
                            elevation = 10f
                            setSingleLine(true)
                            ellipsize = TextUtils.TruncateAt.END
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.BOTTOM or Gravity.START
                                setMargins(margin / 2, 0, margin / 2, margin / 2)
                            }
                        }

                        container.addView(imageView)
                        container.addView(dateLabel)
                        dateLabel.bringToFront()

                        gridLayout.addView(container)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlbumDetail", "Eroare la încărcarea postărilor: ${e.message}")
                toast("A apărut o eroare la încărcarea pozelor.")
            }
        }
    }

    private fun formatDateShort(date: String): String {
        if (date.isBlank()) return "No date"
        val parts = date.split(" ", ",").map { it.trim() }.filter { it.isNotEmpty() }
        return if (parts.size >= 2) "${parts[0]} ${parts[1]}" else date
    }

    private fun toast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
