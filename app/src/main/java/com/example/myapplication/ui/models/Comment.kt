package com.example.myapplication.ui.models


data class Comment(
    val id: String = "",
    val adviceId: String = "",
    val userId: String? = null,
    val username: String = "Anonim",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
