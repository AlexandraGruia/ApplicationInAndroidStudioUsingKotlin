package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import java.io.ByteArrayOutputStream
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

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)

            startActivity(intent)
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
                val intent = Intent(this, HomeActivity::class.java).apply {
                    putExtra("postText", postText)
                    putExtra("postDate", currentDate)
                    putExtra("postImage", R.drawable.default_image)
                }
                startActivity(intent)
                finish()
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

    private fun handleGalleryImage(uri: Uri?): Bitmap? {
        return try {
            uri?.let {
                val inputStream = contentResolver.openInputStream(it)
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scale = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val postInput = findViewById<EditText>(R.id.postInput)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY -> {
                    val selectedImageUri = data?.data
                    val bitmap = handleGalleryImage(selectedImageUri)
                    bitmap?.let {
                        val resizedBitmap = resizeBitmap(it, 800, 800)
                        val compressedBitmap = compressBitmap(resizedBitmap)
                        insertImageInEditText(postInput, compressedBitmap)
                    }
                }
                REQUEST_CAMERA -> {
                    val photo = data?.extras?.get("data") as? Bitmap
                    photo?.let {
                        val resizedBitmap = resizeBitmap(it, 800, 800)
                        val compressedBitmap = compressBitmap(resizedBitmap)
                        insertImageInEditText(postInput, compressedBitmap)
                    }
                }
                102 -> {
                    val byteArray = data?.getByteArrayExtra("drawingBitmap")
                    if (byteArray != null) {
                        val drawingBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        insertImageInEditText(postInput, drawingBitmap)

                        val savedDrawingImageView = findViewById<ImageView>(R.id.savedDrawingImageView)
                        savedDrawingImageView.setImageBitmap(drawingBitmap)
                        savedDrawingImageView.visibility = View.VISIBLE
                    }
                }
            }
        } else {
            Toast.makeText(this, "Image selection failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int = 80): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


    private fun insertImageInEditText(editText: EditText, bitmap: Bitmap) {
        val spannable = SpannableStringBuilder(editText.text)
        val start = editText.selectionStart
        val imageSpan = ImageSpan(this, bitmap)

        spannable.setSpan(imageSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.insert(start + 1, "\n\n")

        editText.text = spannable

        editText.setSelection(start + 3)
    }


}

