package com.example.myapplication.ui.models

data class Post(
    val id: String = "",
    val text: String = "",
    val date: String = "",
    val imagePath: String = "",
    val albumId: String? = null,
    val userId: String = ""
)
