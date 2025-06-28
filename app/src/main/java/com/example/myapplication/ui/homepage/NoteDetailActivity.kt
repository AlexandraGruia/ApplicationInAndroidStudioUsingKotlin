package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.ui.database.PostDatabaseHelper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NoteDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val postId = intent.getStringExtra("post_id")
        if (postId.isNullOrEmpty()) {
            Toast.makeText(this, "Postarea nu a fost găsită", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val textView = findViewById<TextView>(R.id.noteDetailText)
        val imageView = findViewById<ImageView>(R.id.noteDetailImage)
        val dateView = findViewById<TextView>(R.id.noteDetailDate)
        val exitIcon = findViewById<ImageView>(R.id.exitIcon)

        exitIcon.setOnClickListener {
            navigateToHomeActivity()
        }

        lifecycleScope.launch {
            val post = PostDatabaseHelper.instance.getPostById(postId)
            if (post == null) {
                Toast.makeText(this@NoteDetailActivity, "Post not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            textView.text = post.text

            val formattedDate = try {
                LocalDate.parse(post.date).format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
            } catch (e: Exception) {
                post.date
            }
            dateView.text = formattedDate

            if (!post.imagePath.isNullOrEmpty()) {
                imageView.visibility = View.VISIBLE

                if (post.imagePath.startsWith("http")) {
                    // Imagine URL
                    Glide.with(this@NoteDetailActivity)
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
                        Toast.makeText(this@NoteDetailActivity, "Error displaying image", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                imageView.visibility = View.GONE
            }
        }
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
