package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.database.AdviceDatabase
import com.example.myapplication.ui.models.Advice
import com.example.myapplication.ui.models.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.*

class AddAdviceActivity : ComponentActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var db: AdviceDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_advice)

        db = AdviceDatabase.instance

        val titleInput = findViewById<EditText>(R.id.addAdviceTitle)
        val descriptionInput = findViewById<EditText>(R.id.adviceInput)
        val selectImageButton = findViewById<Button>(R.id.selectImageButton)
        val selectedImageView = findViewById<ImageView>(R.id.selectedImageView)
        val saveButton = findViewById<Button>(R.id.submitAdviceButton)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)

        val categories = listOf("Anxiety", "Lifestyle", "Others")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        val pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    selectedImageUri = uri
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                    ImageLoader.loadImage(selectedImageView, uri.toString())
                }
            }

        selectImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val category = categorySpinner.selectedItem.toString()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill out both fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val adviceId = UUID.randomUUID().toString()

            lifecycleScope.launch(Dispatchers.IO) {
                val imageUrl: String? = selectedImageUri?.let { uploadImageToCloudinary(it) }

                val advice = Advice(
                    id = adviceId,
                    title = title,
                    description = description,
                    imageUri = imageUrl,
                    category = category,
                    isFavorite = false
                )

                val success = db.insertOrUpdateAdvice(advice)

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@AddAdviceActivity, "Advice saved!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@AddAdviceActivity, AdviceActivity::class.java).apply {
                            putExtra("adviceId", adviceId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@AddAdviceActivity, "Failed to save advice.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun uploadImageToCloudinary(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileStream = contentResolver.openInputStream(uri) ?: return@withContext null

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        "advice_image.jpg",
                        RequestBody.create("image/*".toMediaTypeOrNull(), fileStream.readBytes())
                    )
                    .addFormDataPart("upload_preset", "dreamlog_android")
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/dk4oujows/image/upload")
                    .post(requestBody)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) return@withContext null

                val json = response.body?.string()
                val secureUrl = Regex("\"secure_url\":\"(.*?)\"")
                    .find(json ?: "")?.groupValues?.get(1)?.replace("\\/", "/")

                secureUrl
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
