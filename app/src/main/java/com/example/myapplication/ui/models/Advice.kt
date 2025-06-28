package com.example.myapplication.ui.models

data class Advice(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUri: String? = null,
    val category: String = "",
    val isFavorite: Boolean = false
)
