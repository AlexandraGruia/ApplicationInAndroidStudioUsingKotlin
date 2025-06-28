package com.example.myapplication.ui.album

import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import java.util.*

object CloudinaryUploader {

    private const val CLOUD_NAME = "dk4oujows"
    private const val UPLOAD_PRESET = "dreamlog_android"
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    fun uploadImage(uri: Uri, onResult: (String?) -> Unit) {
        val file = File(uri.path ?: return onResult(null))
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                val secureUrl = Regex("\"secure_url\":\"(.*?)\"")
                    .find(json ?: "")?.groupValues?.get(1)?.replace("\\/", "/")
                onResult(secureUrl)
            }
        })
    }
}