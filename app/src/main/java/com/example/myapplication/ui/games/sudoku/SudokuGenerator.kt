package com.example.myapplication.ui.games.sudoku

import kotlin.random.Random

object SudokuGenerator {
    private val solution = Array(9) { IntArray(9) }

    fun generate(board: Array<IntArray>, fixed: Array<BooleanArray>) {
        val solved = Array(9) { IntArray(9) }
        solve(solved)

        for (i in 0..8) {
            for (j in 0..8) {
                solution[i][j] = solved[i][j]
            }
        }

        for (i in 0..8) {
            for (j in 0..8) {
                board[i][j] = solved[i][j]
                fixed[i][j] = true
            }
        }

        val blanks = Random.nextInt(30, 50)
        repeat(blanks) {
            val x = Random.nextInt(9)
            val y = Random.nextInt(9)
            board[x][y] = 0
            fixed[x][y] = false
        }
    }

    fun copySolutionTo(dest: Array<IntArray>) {
        for (i in 0..8) {
            for (j in 0..8) {
                dest[i][j] = solution[i][j]
            }
        }
    }

    fun solve(board: Array<IntArray>): Boolean {
        for (i in 0..8)
            for (j in 0..8)
                if (board[i][j] == 0) {
                    for (n in 1..9) {
                        if (isValid(board, i, j, n)) {
                            board[i][j] = n
                            if (solve(board)) return true
                            board[i][j] = 0
                        }
                    }
                    return false
                }
        return true
    }

    private fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (i in 0..8)
            if (board[row][i] == num || board[i][col] == num ||
                board[row / 3 * 3 + i / 3][col / 3 * 3 + i % 3] == num)
                return false
        return true
    }
}