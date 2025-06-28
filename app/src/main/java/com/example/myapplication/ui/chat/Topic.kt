package com.example.myapplication

data class Topic(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val peopleCount: Int = 0,
    val timestamp: Long = 0L,
    val messageCount: Int = 0,
    var isJoined: Boolean = false,
    var isFavorite: Boolean = false
)
