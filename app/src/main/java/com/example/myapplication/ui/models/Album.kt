package com.example.myapplication.ui.models

data class Album(
    val id: String = "",
    val name: String = "",
    val photoPaths: List<String> = emptyList(),
    val userId: String = ""
)
