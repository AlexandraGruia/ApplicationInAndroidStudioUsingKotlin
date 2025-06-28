package com.example.myapplication.ui.games

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.example.myapplication.AdviceActivity
import com.example.myapplication.AlbumActivity
import com.example.myapplication.ChatActivity
import com.example.myapplication.HomeActivity
import com.example.myapplication.NewNoteActivity
import com.example.myapplication.R
import com.example.myapplication.ui.games.colormatch.ColorMatchActivity
import com.example.myapplication.ui.games.sudoku.SudokuActivity
import com.example.myapplication.ui.games.sudoku.SudokuPlay
import com.example.myapplication.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class GamesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)


        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val exitIcon = findViewById<ImageView>(R.id.exitIcon)
        exitIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val sudokuButton = findViewById<Button>(R.id.sudokuButton)
        sudokuButton.setOnClickListener {
            val intent = Intent(this, SudokuPlay::class.java)
            startActivity(intent)
        }

        val colorMatchButton = findViewById<Button>(R.id.colorMatchButton)
         colorMatchButton.setOnClickListener {
            val intent = Intent(this, ColorMatchActivity::class.java)
            startActivity(intent)
       }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {

                R.id.bottom_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }

                R.id.bottom_album -> {
                    startActivity(Intent(this, AlbumActivity::class.java))
                    finish()
                    true
                }

                R.id.bottom_newnote -> {
                    startActivity(Intent(this, NewNoteActivity::class.java))
                    finish()
                    true
                }

                R.id.bottom_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    finish()
                    true
                }

                R.id.bottom_advice -> {
                    startActivity(Intent(this, AdviceActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }

    }
}