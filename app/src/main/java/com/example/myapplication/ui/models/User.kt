package com.example.myapplication.ui.models

data class User(
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var dateOfBirth: String = "",
    var isAdmin: Boolean = false,
    var sudokuCompleted: Int = 0
)
