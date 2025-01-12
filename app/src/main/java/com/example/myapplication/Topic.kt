package com.example.myapplication

data class Topic(
    val id: String,
    val name: String,
    val category: String,
    val peopleCount: Int,
    val timestamp: Long,
    var isJoined: Boolean = false
)
