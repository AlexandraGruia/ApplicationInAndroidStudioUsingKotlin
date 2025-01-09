package com.example.myapplication


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class AlbumActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_album)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_album
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_album -> return@setOnItemSelectedListener true
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

        val photoGrid = findViewById<GridLayout>(R.id.photoGrid)

        // Retrieve the saved photo metadata (file paths and dates) from SharedPreferences
        val sharedPreferences = getSharedPreferences("AlbumPreferences", Context.MODE_PRIVATE)
        val photosData = sharedPreferences.getString("photosList", null)

        if (photosData != null) {
            // Split the stored data into individual entries
            val photosList = photosData.split(";")

            // Loop through the stored photos and display them
            for (photoEntry in photosList) {
                val parts = photoEntry.split("|")
                if (parts.size == 2) {
                    val filePath = parts[0]
                    val date = parts[1]

                    // Load the photo from the file path
                    val photoFile = File(filePath)
                    if (photoFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                        // Create a frame layout to hold the image and date
                        val frameLayout = FrameLayout(this)

                        // Create ImageView for the photo
                        val imageView = ImageView(this)
                        imageView.layoutParams = GridLayout.LayoutParams().apply {
                            width = 300  // Set width of image
                            height = 300  // Set height of image
                            setMargins(10, 10, 10, 10)  // Add margins for spacing
                        }
                        imageView.setImageBitmap(bitmap) // Set the photo bitmap
                        frameLayout.addView(imageView)

                        // Create TextView for the date
                        val dateTextView = TextView(this)
                        dateTextView.text = date
                        dateTextView.setTextColor(resources.getColor(android.R.color.white))
                        dateTextView.setTextSize(12f)  // Adjust font size
                        dateTextView.setBackgroundColor(resources.getColor(android.R.color.black))  // Background for date
                        dateTextView.setPadding(8, 4, 8, 4)  // Padding around date text

                        // Position the date at the bottom-right corner
                        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                        params.gravity = Gravity.BOTTOM or Gravity.END
                        dateTextView.layoutParams = params
                        frameLayout.addView(dateTextView)

                        // Add the frame layout to the photo grid
                        photoGrid.addView(frameLayout)
                    }
                }
            }
        }
    }
}


