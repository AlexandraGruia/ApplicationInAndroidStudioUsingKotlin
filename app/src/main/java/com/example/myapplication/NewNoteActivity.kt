package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NewNoteActivity : ComponentActivity() {

    companion object {
        const val REQUEST_GALLERY = 100
        const val REQUEST_CAMERA = 101
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newnote)

        val dateTextView = findViewById<TextView>(R.id.dateText)
        val currentDate = LocalDate.now()
        val formattedDate = currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
        dateTextView.text = formattedDate


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_newnote
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_newnote -> return@setOnItemSelectedListener true
                R.id.bottom_home -> {
                    startActivity(
                        Intent(
                            applicationContext,
                            HomeActivity::class.java
                        )
                    )
                    finish()
                    return@setOnItemSelectedListener true
                }

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

                R.id.bottom_chat -> {
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

        val photoButton = findViewById<ImageButton>(R.id.photoButton)
        photoButton.setOnClickListener {
            showPhotoOptions()
        }

        val stickersButton = findViewById<ImageButton>(R.id.stickersButton)
        stickersButton.setOnClickListener {
            Toast.makeText(this, "Stickers feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        val drawButton = findViewById<ImageButton>(R.id.drawButton)
        drawButton.setOnClickListener {
            val intent = Intent(this, DrawingActivity::class.java)
            startActivity(intent)
        }



        val postInput = findViewById<EditText>(R.id.postInput)
        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            val postText = postInput.text.toString()
            if (postText.isNotBlank()) {
                postInput.text.clear()
            } else {
                Toast.makeText(this, "Post cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
    }


            private fun showPhotoOptions() {
                val options = arrayOf("Add Photo from Gallery", "Take Photo")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Choose an option")
                builder.setItems(options) { _, which ->
                    when (which) {
                        0 -> openGallery()
                        1 -> openCamera()
                    }
                }
                builder.show()
            }

            private fun openGallery() {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_GALLERY)
            }

            private fun openCamera() {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, REQUEST_CAMERA)
            }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val postInput = findViewById<EditText>(R.id.postInput)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        insertImageInEditText(postInput, bitmap)
                    }
                }
                REQUEST_CAMERA -> {
                    val photo = data?.extras?.get("data") as Bitmap
                    insertImageInEditText(postInput, photo)
                }
            }
        }

                val savedDrawingImageView = findViewById<ImageView>(R.id.savedDrawingImageView)
                val drawingBitmap = intent.getParcelableExtra<Bitmap>("drawingBitmap")

                if (drawingBitmap != null) {
                    savedDrawingImageView.setImageBitmap(drawingBitmap)
                    savedDrawingImageView.visibility = View.VISIBLE // Show the drawing
                }

                val submitButton = findViewById<Button>(R.id.submitButton)
                submitButton.setOnClickListener {
                    val postText = postInput.text.toString()
                    if (postText.isNotBlank()) {
                        postInput.text.clear()
                    } else {
                        Toast.makeText(this, "Post cannot be empty!", Toast.LENGTH_SHORT).show()
                    }
                }
            }


    private fun insertImageInEditText(editText: EditText, bitmap: Bitmap) {
        val spannable = SpannableStringBuilder(editText.text)
        val start = editText.selectionStart

        val imageSpan = ImageSpan(this, bitmap)
        spannable.setSpan(imageSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        editText.text = spannable
        editText.setSelection(start + 1)
    }
}

