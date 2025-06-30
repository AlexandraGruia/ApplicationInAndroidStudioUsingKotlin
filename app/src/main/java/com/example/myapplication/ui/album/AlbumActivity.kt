package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
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
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ui.database.AlbumDatabaseHelper
import com.example.myapplication.ui.database.PostDatabaseHelper
import com.example.myapplication.ui.database.UserDatabaseHelper
import com.example.myapplication.ui.models.Album
import com.example.myapplication.ui.models.Post
import com.example.myapplication.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.File

class AlbumActivity : ComponentActivity() {

    private var isAlbumView = false
    private lateinit var photoGrid: GridLayout
    private lateinit var albumRecyclerView: RecyclerView
    private lateinit var switchModeButton: ImageButton
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        userId = getSharedPreferences("user_data", MODE_PRIVATE).getString("email", "") ?: ""
        lifecycleScope.launch {
            val user = UserDatabaseHelper.instance.getUserByEmail(userId)
            if (user == null) finish()
        }

        photoGrid = findViewById(R.id.photoGrid)
        albumRecyclerView = findViewById(R.id.albumRecyclerView)
        switchModeButton = findViewById(R.id.switchModeButton)

        setupBottomNavigation()
        setupButtons()
        showPhotoGrid()
    }

    private fun setupButtons() {
        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        switchModeButton.setOnClickListener {
            isAlbumView = !isAlbumView
            if (isAlbumView) showAlbumList() else showPhotoGrid()
        }
        findViewById<ImageButton>(R.id.addAlbumButton).setOnClickListener {
            showCreateAlbumDialog()
        }
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

    private fun showPhotoGrid() {
        isAlbumView = false
        switchModeButton.setImageResource(R.drawable.ic_switch)
        photoGrid.visibility = View.VISIBLE
        albumRecyclerView.visibility = View.GONE
        loadPostsGrid()
    }

    private fun showAlbumList() {
        isAlbumView = true
        switchModeButton.setImageResource(R.drawable.ic_switch)
        photoGrid.visibility = View.GONE
        albumRecyclerView.visibility = View.VISIBLE
        lifecycleScope.launch { loadAlbums() }
    }

    private fun showCreateAlbumDialog() {
        val input = EditText(this).apply {
            hint = "Album name"
            setPadding(32, 16, 32, 16)
        }

        AlertDialog.Builder(this)
            .setTitle("Create New Album")
            .setView(input)
            .setPositiveButton("Create") { dialog, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch { createAlbum(name) }
                } else {
                    toast("Album name cannot be empty")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun createAlbum(name: String) {
        val db = AlbumDatabaseHelper.instance
        val albums = db.getAlbums(userId)
        if (albums.any { it.name.equals(name, ignoreCase = true) }) {
            toast("Album '$name' already exists")
            return
        }
        val newAlbum = db.addAlbum(name, userId)
        if (newAlbum != null) {
            toast("Album '$name' created successfully")
            loadAlbums()
        } else {
            toast("Failed to create album")
        }
    }

    private suspend fun loadAlbums() {
        val db = AlbumDatabaseHelper.instance
        val postDb = PostDatabaseHelper.instance
        val albums = db.getAlbums(userId)
        val allPosts = postDb.getAllPosts()

        val albumsWithPhotos = albums.map { album ->
            val postsInAlbum = allPosts.filter { it.albumId == album.id }
            val photoUrls = postsInAlbum.mapNotNull { it.imagePath }
            Album(album.id, album.name, photoUrls.toMutableList())
        }

        albumRecyclerView.layoutManager = GridLayoutManager(this, 2)
        albumRecyclerView.adapter = AlbumAdapter(this, albumsWithPhotos) { album ->
            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val currentUserEmail = sharedPref.getString("userEmail", "") ?: ""
            startActivity(Intent(this, AlbumDetailActivity::class.java).apply {
                putExtra("albumId", album.id)
                putExtra("albumName", album.name)
                putExtra("userEmail", currentUserEmail)
            })
        }
    }

    private fun loadPostsGrid() {
        lifecycleScope.launch {
            val posts = PostDatabaseHelper.instance.getAllPosts().filter { it.userId.equals(userId, ignoreCase = true) }
            runOnUiThread {
                photoGrid.removeAllViews()
                photoGrid.columnCount = 3

                val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    windowManager.currentWindowMetrics.bounds.width()
                } else {
                    DisplayMetrics().let { metrics ->
                        @Suppress("DEPRECATION")
                        windowManager.defaultDisplay.getMetrics(metrics)
                        metrics.widthPixels
                    }
                }

                val margin = (8 * resources.displayMetrics.density + 0.5f).toInt()
                val totalMargin = margin * 4
                val itemSize = (screenWidth - totalMargin) / 3

                posts.forEachIndexed { index, post ->
                    val container = FrameLayout(this@AlbumActivity).apply {
                        setBackgroundColor(Color.TRANSPARENT)
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = itemSize
                            height = itemSize
                            columnSpec = GridLayout.spec(index % 3)
                            rowSpec = GridLayout.spec(index / 3)
                            setMargins(margin, margin, margin, margin)
                        }
                        setOnClickListener {
                            val intent = Intent(this@AlbumActivity, NoteDetailActivity::class.java)
                            intent.putExtra("postId", post.id)
                            startActivity(intent)
                        }

                    }

                    val imageView = ImageView(this@AlbumActivity).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    }

                    if (!post.imagePath.isNullOrEmpty()) {
                        when {
                            post.imagePath.startsWith("http") -> {
                                Glide.with(this@AlbumActivity)
                                    .load(post.imagePath)
                                    .placeholder(R.drawable.default_image)
                                    .centerCrop()
                                    .into(imageView)
                            }
                            File(post.imagePath).exists() -> {
                                Glide.with(this@AlbumActivity)
                                    .load(File(post.imagePath))
                                    .placeholder(R.drawable.default_image)
                                    .centerCrop()
                                    .into(imageView)
                            }
                            else -> {
                                try {
                                    val decodedBytes = Base64.decode(post.imagePath, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    imageView.setImageBitmap(bitmap)
                                } catch (e: Exception) {
                                    imageView.setImageResource(R.drawable.default_image)
                                    Log.e("AlbumActivity", "Failed to decode Base64 image: ${e.message}")
                                }
                            }
                        }
                    } else {
                        imageView.setImageResource(R.drawable.default_image)
                    }

                    val dateLabel = TextView(this@AlbumActivity).apply {
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

                    container.apply {
                        addView(imageView)
                        addView(dateLabel)
                    }
                    dateLabel.bringToFront()

                    photoGrid.addView(container)
                }
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

    fun deleteAlbum(albumId: String, albumName: String) {
        lifecycleScope.launch {
            val deleted = AlbumDatabaseHelper.instance.deleteAlbum(albumId)
            if (deleted) {
                toast("Album '$albumName' deleted")
                loadAlbums()
            } else {
                toast("Failed to delete album")
            }
        }
    }



}
