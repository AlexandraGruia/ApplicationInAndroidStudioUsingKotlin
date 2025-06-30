package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NoteDetailActivity : ComponentActivity() {

    private lateinit var favoriteButton: ImageButton
    private lateinit var addToAlbumButton: Button
    private lateinit var deleteButton: Button
    private lateinit var postTextView: TextView
    private lateinit var postImageView: ImageView
    private lateinit var dateTextView: TextView
    private lateinit var exitIcon: ImageView

    private var currentPost: Post? = null
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        favoriteButton = findViewById(R.id.favoriteButton)
        addToAlbumButton = findViewById(R.id.addToAlbumButton)
        deleteButton = findViewById(R.id.deleteButton)
        postTextView = findViewById(R.id.noteDetailText)
        postImageView = findViewById(R.id.noteDetailImage)
        dateTextView = findViewById(R.id.noteDetailDate)
        exitIcon = findViewById(R.id.exitIcon)

        val postId = intent.getStringExtra("postId")
        if (postId.isNullOrEmpty()) {
            Toast.makeText(this, "Postarea nu a fost găsită", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        exitIcon.setOnClickListener {
            navigateToHomeActivity()
        }

        lifecycleScope.launch {
            val post = PostDatabaseHelper.instance.getPostById(postId)
            if (post == null) {
                Toast.makeText(this@NoteDetailActivity, "Postarea nu a fost găsită", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            currentPost = post
            postTextView.text = post.text

            val formattedDate = try {
                LocalDate.parse(post.date).format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
            } catch (e: Exception) {
                post.date
            }
            dateTextView.text = formattedDate

            if (!post.imagePath.isNullOrEmpty()) {
                postImageView.visibility = android.view.View.VISIBLE

                if (post.imagePath.startsWith("http")) {
                    Glide.with(this@NoteDetailActivity)
                        .load(post.imagePath)
                        .centerCrop()
                        .placeholder(R.drawable.default_image)
                        .into(postImageView)
                } else {
                    try {
                        val decodedBytes = Base64.decode(post.imagePath, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        postImageView.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        postImageView.visibility = android.view.View.GONE
                        Toast.makeText(this@NoteDetailActivity, "Eroare la afișarea imaginii", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                postImageView.visibility = android.view.View.GONE
            }

            val userId = getUserId()
            isFavorite = AlbumDatabaseHelper.instance.isFavorite(userId, post.id)
            updateFavoriteIcon()
        }

        favoriteButton.setOnClickListener {
            toggleFavorite()
        }

        addToAlbumButton.setOnClickListener {
            currentPost?.let { post ->
                showAddToAlbumDialog(post)
            }
        }

        deleteButton.setOnClickListener {
            confirmDelete()
        }
    }

    private fun getUserId(): String =
        getSharedPreferences("user_data", Context.MODE_PRIVATE)
            .getString("email", "")?.trim()?.lowercase() ?: ""

    private fun updateFavoriteIcon() {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_red else R.drawable.ic_favorite
        )
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            currentPost?.let { post ->
                isFavorite = !isFavorite
                val userId = getUserId()

                AlbumDatabaseHelper.instance.setFavoriteForUser(userId, post.id, isFavorite)
                updateFavoriteIcon()

                val db = AlbumDatabaseHelper.instance
                val favoriteAlbum = db.getAlbums(userId).find { it.name.equals("Favorite", true) }
                    ?: if (isFavorite) db.addAlbum("Favorite", userId) else null

                if (favoriteAlbum == null) {
                    showToast("Albumul Favorite nu a fost găsit")
                    return@launch
                }

                val postsInFavorite = db.getPostsForAlbum(favoriteAlbum.id)
                val postExists = postsInFavorite.any { it.id == post.id }

                val success = when {
                    isFavorite && !postExists -> db.addPostToAlbum(post, favoriteAlbum.id)
                    !isFavorite && postExists -> {
                        postsInFavorite.find { it.id == post.id }?.let { db.removePostFromAlbum(it) } ?: false
                    }
                    else -> true
                }

                showToast(
                    if (success) {
                        if (isFavorite) "Adăugat la Favorite" else "Eliminat din Favorite"
                    } else {
                        if (isFavorite) "Nu s-a putut adăuga la Favorite" else "Nu s-a putut elimina din Favorite"
                    }
                )
            }
        }
    }

    private fun showAddToAlbumDialog(post: Post) {
        lifecycleScope.launch {
            val userId = getUserId()
            val albums = AlbumDatabaseHelper.instance.getAlbums(userId)
                .filterNot { it.name.equals("Favorite", true) }

            if (albums.isEmpty()) {
                showToast("Nu există albume. Creează unul mai întâi.")
                return@launch
            }

            val albumNames = albums.map { it.name }.toTypedArray()
            AlertDialog.Builder(this@NoteDetailActivity)
                .setTitle("Adaugă poza în album")
                .setItems(albumNames) { _, index ->
                    lifecycleScope.launch {
                        val added = AlbumDatabaseHelper.instance.addPostToAlbum(post, albums[index].id)
                        showToast(if (added) "Adăugat în \"${albums[index].name}\"" else "Nu s-a putut adăuga")
                    }
                }
                .setNegativeButton("Anulează", null)
                .show()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Șterge postarea")
            .setMessage("Ești sigur că vrei să ștergi această postare?")
            .setPositiveButton("Da") { _, _ -> deletePost() }
            .setNegativeButton("Nu", null)
            .show()
    }

    private fun deletePost() {
        lifecycleScope.launch {
            val postId = currentPost?.id ?: return@launch
            val userId = getUserId()

            val success = PostDatabaseHelper.instance.deletePost(postId)
            if (success) {
                AlbumDatabaseHelper.instance.setFavoriteForUser(userId, postId, false)

                AlbumDatabaseHelper.instance.getAlbums(userId)
                    .find { it.name.equals("Favorite", true) }
                    ?.let { album ->
                        AlbumDatabaseHelper.instance.getPostsForAlbum(album.id)
                            .find { it.id == postId }
                            ?.let { AlbumDatabaseHelper.instance.removePostFromAlbum(it) }
                    }

                showToast("Postarea a fost ștearsă")
                navigateToHomeActivity()
            } else {
                showToast("Nu s-a putut șterge postarea")
            }
        }
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
