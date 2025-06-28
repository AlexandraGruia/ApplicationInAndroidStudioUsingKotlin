package com.example.myapplication.ui.games.sudoku

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.ui.games.GamesActivity

class SudokuPlay : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)

        val playButton: MaterialButton = findViewById(R.id.playButton)
        playButton.setBackgroundResource(R.drawable.round_for_sudoku)
        playButton.setOnClickListener {
            startActivity(Intent(this, SudokuActivity::class.java))
        }

        val exitIcon = findViewById<ImageView>(R.id.exitIcon)
        exitIcon.setOnClickListener {
            val intent = Intent(this, GamesActivity::class.java)
            startActivity(intent)
        }
    }
}
