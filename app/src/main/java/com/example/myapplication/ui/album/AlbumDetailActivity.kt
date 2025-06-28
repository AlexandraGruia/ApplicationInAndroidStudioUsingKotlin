package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.ui.database.PostDatabaseHelper
import com.example.myapplication.ui.models.Post
import kotlinx.coroutines.launch

class AlbumDetailActivity : ComponentActivity() {

    private lateinit var albumNameTextView: TextView
    private lateinit var photoGrid: GridLayout

    private lateinit var albumId: String
    private lateinit var albumName: String
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)

        albumNameTextView = findViewById(R.id.titleText)
        photoGrid = findViewById(R.id.photoGrid)

        albumId = intent.getStringExtra("albumId") ?: ""
        albumName = intent.getStringExtra("albumName") ?: "Unknown Album"
        userEmail = intent.getStringExtra("userEmail") ?: ""

        albumNameTextView.text = albumName

        photoGrid.columnCount = 3

        loadAlbumPhotos()
    }

    private fun loadAlbumPhotos() {
        lifecycleScope.launch {
            val postDb = PostDatabaseHelper.instance
            val allPosts = postDb.getAllPosts()

            val postsInAlbum: List<Post> = allPosts.filter { it.albumId == albumId }

            runOnUiThread {
                photoGrid.removeAllViews()

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

                postsInAlbum.forEachIndexed { index, post ->
                    val container = FrameLayout(this@AlbumDetailActivity).apply {
                        setBackgroundColor(Color.TRANSPARENT)
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = itemSize
                            height = itemSize
                            columnSpec = GridLayout.spec(index % 3)
                            rowSpec = GridLayout.spec(index / 3)
                            setMargins(margin, margin, margin, margin)
                        }
                        setOnClickListener {
                            startActivity(Intent(this@AlbumDetailActivity, PostDetailActivity::class.java).apply {
                                putExtra("postId", post.id)
                                putExtra("filePath", post.imagePath)
                                putExtra("date", post.date)
                                putExtra("text", post.text)
                            })
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
                        if (post.imagePath.startsWith("http")) {
                            Glide.with(this@AlbumDetailActivity)
                                .load(post.imagePath)
                                .placeholder(R.drawable.default_image)
                                .centerCrop()
                                .into(imageView)
                        } else {
                            try {
                                val decodedBytes = android.util.Base64.decode(post.imagePath, android.util.Base64.DEFAULT)
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                imageView.setImageBitmap(bitmap)
                            } catch (e: Exception) {
                                imageView.setImageResource(R.drawable.default_image)
                            }
                        }
                    } else {
                        imageView.setImageResource(R.drawable.default_image)
                    }

                    container.addView(imageView)
                    photoGrid.addView(container)
                }
            }
        }
    }
}
