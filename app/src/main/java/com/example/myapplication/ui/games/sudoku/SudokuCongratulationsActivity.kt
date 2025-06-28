package com.example.myapplication.ui.games.sudoku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.ui.games.GamesActivity

class SudokuCongratulationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku_congratulations)

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            val intent = Intent(this, SudokuActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.exitButton).setOnClickListener {
            val intent = Intent(this, GamesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}