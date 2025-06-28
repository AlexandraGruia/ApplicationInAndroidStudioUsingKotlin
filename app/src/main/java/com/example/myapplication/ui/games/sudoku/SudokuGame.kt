package com.example.myapplication.ui.games.sudoku

import android.content.Context
import com.example.myapplication.ui.database.UserDatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SudokuGame(
    private val context: Context,
    private val userId: String,
    val onProgress: (Double) -> Unit
) {
    val board = Array(9) { IntArray(9) }
    val fixed = Array(9) { BooleanArray(9) }
    val solution = Array(9) { IntArray(9) }

    private var isAlreadyCompleted = false
    init {
        SudokuGenerator.generate(board, fixed)
        SudokuGenerator.copySolutionTo(solution)
    }

    fun set(x: Int, y: Int, value: Int) {
        if (!fixed[x][y]) {
            board[x][y] = value
            onProgress(progress())

            if (isComplete() && !isAlreadyCompleted && isSolvedCorrectly()) {
                CoroutineScope(Dispatchers.IO).launch {
                    UserDatabaseHelper.instance.incrementSudokuCompleted(userId)
                }
                isAlreadyCompleted = true
            }
        }
    }

    fun get(x: Int, y: Int) = board[x][y]
    fun isFixed(x: Int, y: Int) = fixed[x][y]

    fun progress(): Double {
        var correct = 0
        for (x in 0 until 9) {
            for (y in 0 until 9) {
                if (board[x][y] != 0 && board[x][y] == solution[x][y]) {
                    correct++
                }
            }
        }
        return correct / 81.0
    }

    fun isComplete() = board.all { row -> row.all { it != 0 } }

    fun autoComplete() {
        SudokuGenerator.solve(board)
        onProgress(progress())

        if (!isAlreadyCompleted && isSolvedCorrectly()) {
            CoroutineScope(Dispatchers.IO).launch {
                UserDatabaseHelper.instance.incrementSudokuCompleted(userId)
            }
            isAlreadyCompleted = true
        }
    }
    fun isSolvedCorrectly(): Boolean {
        for (i in 0 until 9) {
            val rowSet = mutableSetOf<Int>()
            val colSet = mutableSetOf<Int>()
            for (j in 0 until 9) {
                val rowVal = board[i][j]
                val colVal = board[j][i]
                if (rowVal !in 1..9 || !rowSet.add(rowVal)) return false
                if (colVal !in 1..9 || !colSet.add(colVal)) return false
            }
        }
        for (startRow in 0 until 9 step 3) {
            for (startCol in 0 until 9 step 3) {
                val boxSet = mutableSetOf<Int>()
                for (i in 0 until 3) {
                    for (j in 0 until 3) {
                        val value = board[startRow + i][startCol + j]
                        if (value !in 1..9 || !boxSet.add(value)) return false
                    }
                }
            }
        }
        return true
    }

    fun isBoardValidSoFar(): Boolean {
        for (i in 0 until 9) {
            val rowSet = mutableSetOf<Int>()
            val colSet = mutableSetOf<Int>()
            for (j in 0 until 9) {
                val rowVal = board[i][j]
                val colVal = board[j][i]
                if (rowVal != 0 && !rowSet.add(rowVal)) return false
                if (colVal != 0 && !colSet.add(colVal)) return false
            }
        }
        for (blockRow in 0 until 9 step 3) {
            for (blockCol in 0 until 9 step 3) {
                val blockSet = mutableSetOf<Int>()
                for (i in 0 until 3) {
                    for (j in 0 until 3) {
                        val value = board[blockRow + i][blockCol + j]
                        if (value != 0 && !blockSet.add(value)) return false
                    }
                }
            }
        }
        return true
    }

    fun isCorrect(x: Int, y: Int, value: Int): Boolean {
        return solution[x][y] == value
    }
}
