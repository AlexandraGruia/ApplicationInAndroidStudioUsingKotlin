package com.example.myapplication

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.MenuItem
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.database.PostDatabaseHelper
import com.example.myapplication.ui.settings.SettingsActivity
import com.example.myapplication.ui.utils.DrawingActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

class NewNoteActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_GALLERY = 100
        private const val REQUEST_CAMERA = 101
        private const val REQUEST_DRAWING = 102
    }

    private lateinit var postInput: EditText
    private lateinit var micIcon: ImageView
    private lateinit var currentDate: LocalDate
    private var selectedBitmap: Bitmap? = null

    private lateinit var sharedPrefs: android.content.SharedPreferences
    private var currentUserEmail: String? = null

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                insertRecognizedText(recognizedText)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = handleGalleryImage(it)
            bitmap?.let {
                selectedBitmap = resizeBitmap(it, 800, 800)
                insertImageIntoEditText(postInput, selectedBitmap!!)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            selectedBitmap = resizeBitmap(it, 800, 800)
            insertImageIntoEditText(postInput, selectedBitmap!!)
        }
    }

    private val drawingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val byteArray = result.data?.getByteArrayExtra("drawingBitmap")
            byteArray?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                selectedBitmap = resizeBitmap(bitmap, 800, 800)
                insertImageIntoEditText(postInput, selectedBitmap!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newnote)

        sharedPrefs = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        currentUserEmail = sharedPrefs.getString("email", null)

        initializeUI()
        setupBottomNavigation()
        setupListeners()
    }

    private fun initializeUI() {
        postInput = findViewById(R.id.postInput)
        micIcon = findViewById(R.id.micIcon)

        val dateText = findViewById<TextView>(R.id.dateText)
        currentDate = LocalDate.now()
        dateText.text = currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.bottom_newnote
        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_newnote -> true
                R.id.bottom_home -> navigateTo(HomeActivity::class.java)
                R.id.bottom_album -> navigateTo(AlbumActivity::class.java)
                R.id.bottom_chat -> navigateTo(ChatActivity::class.java)
                R.id.bottom_advice -> navigateTo(AdviceActivity::class.java)
                else -> false
            }
        }
    }

    private fun setupListeners() {
        micIcon.setOnClickListener { startVoiceRecognition() }

        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageButton>(R.id.stickersButton).setOnClickListener {
            Toast.makeText(this, "Stickers feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.drawButton).setOnClickListener {
            drawingLauncher.launch(Intent(this, DrawingActivity::class.java))
        }

        findViewById<ImageButton>(R.id.photoButton).setOnClickListener {
            showPhotoOptions()
        }

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            submitPost()
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Speech recognition not supported on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun insertRecognizedText(text: String) {
        val start = postInput.selectionStart.coerceAtLeast(0)
        postInput.text.insert(start, "$text ")
    }

    private fun navigateTo(target: Class<*>): Boolean {
        startActivity(Intent(this, target))
        finish()
        return true
    }

    private fun showPhotoOptions() {
        AlertDialog.Builder(this)
            .setTitle("Choose an option")
            .setItems(arrayOf("Add Photo from Gallery", "Take Photo")) { _, which ->
                if (which == 0) openGallery() else openCamera()
            }.show()
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private fun handleGalleryImage(uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val scale = minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true
        )
    }

    private fun insertImageIntoEditText(editText: EditText, bitmap: Bitmap) {
        val spannable = SpannableStringBuilder(editText.text)
        val start = editText.selectionStart.coerceAtLeast(0)
        spannable.insert(start, " ") // Placeholder for image
        spannable.setSpan(
            ImageSpan(this, bitmap),
            start,
            start + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.insert(start + 1, "\n")

        editText.text = spannable
        editText.setSelection(start + 2)
    }

    private fun submitPost() {
        val postText = postInput.text.toString()
        val userEmail = currentUserEmail

        if (userEmail == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        if (postText.isBlank() && selectedBitmap == null) {
            Toast.makeText(this, "Postarea nu poate fi goală!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val formattedDate = currentDate.toString()
            var imageBase64 = ""

            selectedBitmap?.let { bitmap ->
                imageBase64 = bitmapToBase64(bitmap)
            }

            val success = PostDatabaseHelper.instance.addPost(
                text = postText,
                date = formattedDate,
                imageUrl = imageBase64,
                userId = userEmail
            )

            runOnUiThread {
                if (success) {
                    Toast.makeText(this@NewNoteActivity, "Postare salvată!", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this@NewNoteActivity, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@NewNoteActivity, "Eroare la salvare!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun saveImageToFile(bitmap: Bitmap): String? {
        val file = File(cacheDir, "IMG_${System.currentTimeMillis()}.png")
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

}