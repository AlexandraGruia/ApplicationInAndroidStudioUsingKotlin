package com.example.myapplication.ui.games.sudoku

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.ui.database.UserDatabaseHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class SudokuActivity : AppCompatActivity() {

    private lateinit var game: SudokuGame
    lateinit var view: SudokuView

    private val currentUserEmail: String by lazy {
        intent.getStringExtra("USER_EMAIL") ?: "defaultUser@example.com"
    }

    private lateinit var completeBtn: Button
    private lateinit var counterText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku_gameplay)

        val sudokuLayout = findViewById<FrameLayout>(R.id.sudokuContainer)
        completeBtn = findViewById(R.id.autoCompleteButton)
        counterText = findViewById(R.id.counterText)

        game = SudokuGame(this, currentUserEmail) { progress ->
            runOnUiThread {
                if (progress >= 0.8 && game.isSolvedCorrectly()) {
                    completeBtn.visibility = View.VISIBLE
                } else {
                    completeBtn.visibility = View.GONE
                }
            }
        }

        view = SudokuView(this, game)
        sudokuLayout.addView(view)

        updateCompletedCount()

        completeBtn.setOnClickListener {
            game.autoComplete()
            view.invalidate()

            if (game.isSolvedCorrectly()) {
                lifecycleScope.launch {
                    val success = incrementSudokuCompleted(currentUserEmail)
                    if (success) {
                        updateCompletedCount()
                        startCongratsActivity()
                    } else {
                        Toast.makeText(
                            this@SudokuActivity,
                            "Error updating progress. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun startCongratsActivity() {
        val intent = Intent(this, SudokuCongratulationsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateCompletedCount() {
        lifecycleScope.launch {
            val count = getSudokuCompletedCount(currentUserEmail)
            runOnUiThread {
                counterText.text = "Completed Sudoku: $count"
            }
        }
    }

    fun showNumberPicker() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val pickerView = layoutInflater.inflate(R.layout.number_picker_layout, null)

        for (num in 1..9) {
            val btnId = resources.getIdentifier("btn$num", "id", packageName)
            pickerView.findViewById<Button>(btnId).setOnClickListener {
                setNumber(num)
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog.setContentView(pickerView)
        bottomSheetDialog.show()
    }

    private fun setNumber(number: Int) {
        val x = view.selectedX
        val y = view.selectedY

        if (x in 0..8 && y in 0..8 && !game.isFixed(x, y)) {
            val current = game.get(x, y)

            if (current == number) {
                game.set(x, y, 0)
                view.wrongCells.remove(Pair(x, y))
            } else {
                game.set(x, y, number)
                if (!game.isCorrect(x, y, number)) {
                    view.wrongCells.add(Pair(x, y))
                } else {
                    view.wrongCells.remove(Pair(x, y))
                }
            }
            view.invalidate()
        }
    }

    private suspend fun incrementSudokuCompleted(email: String): Boolean {
        return UserDatabaseHelper.instance.incrementSudokuCompleted(email)
    }

    private suspend fun getSudokuCompletedCount(email: String): Int {
        return UserDatabaseHelper.instance.getSudokuCompletedCount(email)
    }

}
