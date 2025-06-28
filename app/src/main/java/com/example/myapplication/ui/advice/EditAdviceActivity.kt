package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.ui.database.AdviceDatabase
import com.example.myapplication.ui.models.Advice
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.HttpURLConnection
import java.net.URL

class EditAdviceActivity : ComponentActivity() {

    private var selectedImageUri: Uri? = null
    private var originalImageUri: Uri? = null
    private lateinit var adviceId: String
    private lateinit var db: AdviceDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_advice)

        db = AdviceDatabase.instance

        adviceId = intent.getStringExtra("adviceId") ?: run {
            showToast(R.string.invalid_advice_id)
            Log.e("EditAdviceActivity", "AdviceId missing in intent extras")
            finish()
            return
        }

        val titleInput = findViewById<EditText>(R.id.addAdviceTitle)
        val descriptionInput = findViewById<EditText>(R.id.adviceInput)
        val selectedImageView = findViewById<ImageView>(R.id.selectedImageView)
        val saveButton = findViewById<Button>(R.id.submitAdviceButton)
        val deleteButton = findViewById<Button>(R.id.deleteAdviceButton)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)

        setupCategorySpinner(categorySpinner)
        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                try {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.w("EditAdviceActivity", "Permission error: ${e.message}")
                }
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.default_advice_image)
                    .error(R.drawable.default_advice_image)
                    .into(selectedImageView)
            }
        }

        findViewById<Button>(R.id.selectImageButton).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        loadAdviceData(adviceId, titleInput, descriptionInput, categorySpinner, selectedImageView)

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val selectedCategory = categorySpinner.selectedItem.toString()

            if (title.isEmpty() || description.isEmpty()) {
                showToast(R.string.fill_all_fields)
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val imageUrl = selectedImageUri?.let {
                    uploadImageToCloudinary(it)
                } ?: originalImageUri?.toString()

                val advice = Advice(
                    id = adviceId,
                    title = title,
                    description = description,
                    category = selectedCategory,
                    imageUri = imageUrl
                )

                val success = db.insertOrUpdateAdvice(advice)
                if (success) {
                    showToast(R.string.advice_updated)
                    setResult(RESULT_OK, Intent().putExtra("updatedAdviceId", adviceId))
                    finish()
                } else {

                }
            }
        }

        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_deletion)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    lifecycleScope.launch {
                        val success = db.deleteAdvice(adviceId)
                        if (success) {
                            Log.i("EditAdviceActivity", "Advice deleted successfully")
                            setResult(RESULT_OK, Intent().putExtra("deletedAdviceId", adviceId))
                            showToast(R.string.advice_deleted)
                            finish()
                        } else {

                        }
                    }
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
    }

    private fun loadAdviceData(
        id: String,
        titleInput: EditText,
        descriptionInput: EditText,
        categorySpinner: Spinner,
        imageView: ImageView
    ) {
        lifecycleScope.launch {
            val advice = db.getAdviceById(id)
            if (advice != null) {
                titleInput.setText(advice.title)
                descriptionInput.setText(advice.description)

                val categories = resources.getStringArray(R.array.categoryAdvice_list)
                val index = categories.indexOfFirst { it.equals(advice.category, ignoreCase = true) }
                categorySpinner.setSelection(index.takeIf { it >= 0 } ?: 0)

                advice.imageUri?.let {
                    originalImageUri = Uri.parse(it)
                    loadImageWithGlide(it, imageView)
                }
            } else {
                showToast(R.string.no_advice_found)
                Log.e("EditAdviceActivity", "Advice with id $id not found")
                finish()
            }
        }
    }

    private fun loadImageWithGlide(imageUriStr: String, imageView: ImageView) {
        try {
            if (imageUriStr.startsWith("http") || imageUriStr.startsWith("https")) {
                Glide.with(this)
                    .load(imageUriStr)
                    .placeholder(R.drawable.default_advice_image)
                    .error(R.drawable.default_advice_image)
                    .into(imageView)
            } else {
                val uri = Uri.parse(imageUriStr)
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.default_advice_image)
                    .error(R.drawable.default_advice_image)
                    .into(imageView)
            }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.default_advice_image)
        }
    }

    private fun setupCategorySpinner(spinner: Spinner) {
        val categories = resources.getStringArray(R.array.categoryAdvice_list)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun showToast(messageRes: Int) {
        Toast.makeText(this, getString(messageRes), Toast.LENGTH_SHORT).show()
    }

    private suspend fun uploadImageToCloudinary(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val fileStream = contentResolver.openInputStream(uri) ?: return@withContext null

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", "edited_image.jpg",
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
