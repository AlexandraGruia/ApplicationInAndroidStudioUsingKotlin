package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.ui.database.AlbumDatabaseHelper
import com.example.myapplication.ui.database.PostDatabaseHelper
import com.example.myapplication.ui.models.Post
import kotlinx.coroutines.launch
import java.io.File

class PostDetailActivity : ComponentActivity() {

    private lateinit var favoriteButton: ImageButton
    private lateinit var dateTextView: TextView
    private lateinit var postTextView: TextView
    private lateinit var imageUrl: String

    private var isFavorite = false
    private var currentPost: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val imageView = findViewById<ImageView>(R.id.detailImage)
        val exitIcon = findViewById<ImageView>(R.id.exitIcon)
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        val addToAlbumButton = findViewById<Button>(R.id.addToAlbumButton)
        favoriteButton = findViewById(R.id.favoriteButton)
        dateTextView = findViewById(R.id.detailDate)
        postTextView = findViewById(R.id.detailText)

        val postId = intent.getStringExtra("postId") ?: return
        imageUrl = intent.getStringExtra("filePath") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val text = intent.getStringExtra("text") ?: "No text available"

        dateTextView.text = formatDateShort(date)
        postTextView.text = text
        displayImage(imageUrl, imageView)

        val userId = getUserId()

        lifecycleScope.launch {
            currentPost = PostDatabaseHelper.instance.getPostById(postId)
            currentPost?.let {
                isFavorite = loadFavoriteStatus(userId, it.id)
                updateFavoriteIcon()
            } ?: showToast("Post not found")
        }

        favoriteButton.setOnClickListener { toggleFavorite(userId) }
        addToAlbumButton.setOnClickListener { currentPost?.let { showAddToAlbumDialog(it) } }
        deleteButton.setOnClickListener { confirmDelete() }
        exitIcon.setOnClickListener { navigateToAlbumActivity() }
    }

    private fun getUserId(): String =
        getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("email", "") ?: ""

    private fun displayImage(url: String, imageView: ImageView) {
        when {
            url.startsWith("http") -> Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(imageView)

            File(url).exists() -> Glide.with(this)
                .load(File(url))
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(imageView)

            else -> {
                try {
                    val decoded = Base64.decode(url, Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                    imageView.setImageBitmap(bitmap)
                } catch (_: Exception) {
                    imageView.setImageResource(R.drawable.default_image)
                }
            }
        }
    }

    private suspend fun loadFavoriteStatus(userId: String, postId: String): Boolean {
        return AlbumDatabaseHelper.instance.isFavorite(userId, postId)
    }

    private fun updateFavoriteIcon() {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_red else R.drawable.ic_favorite
        )
    }

    private fun toggleFavorite(userId: String) {
        lifecycleScope.launch {
            isFavorite = !isFavorite
            val postId = currentPost?.id ?: return@launch

            AlbumDatabaseHelper.instance.setFavoriteForUser(userId, postId, isFavorite)
            updateFavoriteIcon()

            val db = AlbumDatabaseHelper.instance
            val favoriteAlbum = db.getAlbums(userId).find { it.name.equals("Favorite", true) }
                ?: if (isFavorite) db.addAlbum("Favorite", userId) else null

            if (favoriteAlbum == null) {
                showToast("Favorite album not found")
                return@launch
            }

            val postsInFavorite = db.getPostsForAlbum(favoriteAlbum.id)
            val postExists = postsInFavorite.any { it.id == postId }

            val success = when {
                isFavorite && !postExists -> currentPost?.let { db.addPostToAlbum(it, favoriteAlbum.id) } ?: false
                !isFavorite && postExists -> postsInFavorite.find { it.id == postId }?.let { db.removePostFromAlbum(it) } ?: false
                else -> true
            }

            showToast(
                if (success) {
                    if (isFavorite) "Added to Favorites" else "Removed from Favorites"
                } else {
                    if (isFavorite) "Failed to add to Favorites" else "Failed to remove from Favorites"
                }
            )
        }
    }

    private fun showAddToAlbumDialog(post: Post) {
        lifecycleScope.launch {
            val userId = getUserId()
            val albums = AlbumDatabaseHelper.instance.getAlbums(userId)
                .filterNot { it.name.equals("Favorite", true) }

            if (albums.isEmpty()) {
                showToast("No albums available. Create one first.")
                return@launch
            }

            val albumNames = albums.map { it.name }.toTypedArray()
            AlertDialog.Builder(this@PostDetailActivity)
                .setTitle("Add photo to album")
                .setItems(albumNames) { _, index ->
                    lifecycleScope.launch {
                        val added = AlbumDatabaseHelper.instance.addPostToAlbum(post, albums[index].id)
                        showToast(if (added) "Added to \"${albums[index].name}\"" else "Failed to add")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Yes") { _, _ -> deletePost() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deletePost() {
        lifecycleScope.launch {
            val postId = intent.getStringExtra("postId") ?: return@launch
            val userId = getUserId()

            val success = PostDatabaseHelper.instance.deletePost(postId)
            if (success) {
                AlbumDatabaseHelper.instance.setFavoriteForUser(userId, postId, false)

                val favoriteAlbum = AlbumDatabaseHelper.instance.getAlbums(userId)
                    .find { it.name.equals("Favorite", true) }

                favoriteAlbum?.let { album ->
                    AlbumDatabaseHelper.instance.getPostsForAlbum(album.id)
                        .find { it.id == postId }
                        ?.let { AlbumDatabaseHelper.instance.removePostFromAlbum(it) }
                }

                showToast("Post deleted")
            } else {
                showToast("Failed to delete post")
            }

            navigateToAlbumActivity()
        }
    }

    private fun navigateToAlbumActivity() {
        startActivity(Intent(this, AlbumActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun formatDateShort(dateString: String?): String {
        val parts = dateString?.split("-", "/", " ", ",")?.map { it.trim() } ?: return "No date"
        return try {
            if (parts.size >= 3) {
                val day = parts[2].toInt()
                val month = parts[1].toInt()
                val months = listOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                "$day ${months[month - 1]}"
            } else {
                dateString
            }
        } catch (_: Exception) {
            dateString ?: "No date"
        }
    }
}
