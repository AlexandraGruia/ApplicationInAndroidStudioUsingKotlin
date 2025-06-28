package com.example.myapplication.ui.games.colormatch

import com.example.myapplication.R
import kotlin.random.Random
import java.util.ArrayDeque

enum class GameColor {
    RED, GREEN, BLUE, YELLOW;

    fun toAndroidColor(): Int {
        return when (this) {
            RED -> R.color.game_red
            GREEN -> R.color.game_green
            BLUE -> R.color.game_blue
            YELLOW -> R.color.game_yellow
        }
    }

    companion object {
        fun random(): GameColor = values().random()

        fun randomExcluding(exclude: GameColor): GameColor {
            val options = values().filter { it != exclude }
            return options.random()
        }

        fun distSq(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
            val dx = a.first - b.first
            val dy = a.second - b.second
            return dx * dx + dy * dy
        }

        fun generateSolvableGrid(size: Int, moves: Int = 3): ColorMatchGame {
            while (true) {
                val game = ColorMatchGame(size)
                val targetColor = random()
                game.setTargetColor(targetColor)

                for (i in 0 until size) {
                    for (j in 0 until size) {
                        game.setColor(i, j, targetColor)
                    }
                }

                val otherColors = values().filter { it != targetColor }
                val usedPositions = mutableListOf<Pair<Int, Int>>()

                fun isTooCloseToOthers(pos: Pair<Int, Int>): Boolean {
                    val minDist = 4
                    return usedPositions.any { distSq(it, pos) < minDist * minDist }
                }

                fun generateComplexRegion(color: GameColor) {
                    val nucleusCount = 2 + Random.nextInt(2)
                    val nuclei = mutableListOf<Pair<Int, Int>>()
                    var attempts = 0

                    while (nuclei.size < nucleusCount && attempts < 1000) {
                        val candidate = Pair(Random.nextInt(size), Random.nextInt(size))
                        if (!isTooCloseToOthers(candidate) && nuclei.none { distSq(it, candidate) < 4 }) {
                            nuclei.add(candidate)
                            usedPositions.add(candidate)
                        }
                        attempts++
                    }

                    val cells = nuclei.toMutableSet()
                    val frontier = ArrayDeque<Pair<Int, Int>>()
                    nuclei.forEach { frontier.add(it) }

                    val maxCells = 7 + Random.nextInt(7)

                    while (cells.size < maxCells && frontier.isNotEmpty()) {
                        val (x, y) = frontier.removeFirst()
                        listOf(
                            x + 1 to y,
                            x - 1 to y,
                            x to y + 1,
                            x to y - 1
                        ).shuffled().forEach { (nx, ny) ->
                            if (nx in 0 until size && ny in 0 until size && (nx to ny) !in cells) {
                                if (Random.nextDouble() < 0.5) {
                                    cells.add(nx to ny)
                                    frontier.add(nx to ny)
                                }
                            }
                        }
                    }

                    cells.forEach { (x, y) -> game.setColor(x, y, color) }
                }

                val minColors = 3
                val maxColors = 4
                val availableColorsCount = values().size

                val actualMinColors = minColors.coerceAtMost(availableColorsCount)
                val actualMaxColors = maxColors.coerceAtMost(availableColorsCount)

                val colorsCount = Random.nextInt(actualMinColors, actualMaxColors + 1)

                val otherColorsCount = colorsCount - 1

                val selectedColors = otherColors.shuffled().take(otherColorsCount)


                selectedColors.forEach { color -> generateComplexRegion(color) }

                if (game.estimateMinimumMoves() <= moves) {
                    return game
                }
            }
        }
    }
}

class ColorMatchGame(private val size: Int) {

    var grid: Array<Array<GameColor>> = Array(size) { Array(size) { GameColor.random() } }
        private set

    var targetColor: GameColor = GameColor.random()
        private set

    fun getColor(x: Int, y: Int): GameColor = grid[x][y]

    fun setTargetColor(color: GameColor) {
        targetColor = color
    }

    fun applyMoveFrom(x: Int, y: Int, newColor: GameColor) {
        val originalColor = grid[x][y]
        if (originalColor != newColor) {
            floodFillIterative(x, y, originalColor, newColor)
        }
    }

    private fun floodFillIterative(x: Int, y: Int, original: GameColor, target: GameColor) {
        if (original == target) return

        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(Pair(x, y))

        while (queue.isNotEmpty()) {
            val (cx, cy) = queue.removeFirst()

            if (cx !in 0 until size || cy !in 0 until size) continue
            if (grid[cx][cy] != original) continue

            grid[cx][cy] = target

            queue.add(Pair(cx + 1, cy))
            queue.add(Pair(cx - 1, cy))
            queue.add(Pair(cx, cy + 1))
            queue.add(Pair(cx, cy - 1))
        }
    }

    fun isSolved(): Boolean = grid.all { row -> row.all { it == targetColor } }

    fun reset(moves: Int = 3) {
        val newGame = GameColor.generateSolvableGrid(size, moves)
        resetTo(newGame.grid, newGame.targetColor)
    }

    fun setColor(x: Int, y: Int, newColor: GameColor) {
        if (x !in 0 until size || y !in 0 until size) return
        grid[x][y] = newColor
    }

    fun estimateMinimumMoves(): Int {
        val visited = Array(size) { BooleanArray(size) }
        var groups = 0

        for (x in 0 until size) {
            for (y in 0 until size) {
                if (!visited[x][y] && grid[x][y] != targetColor) {
                    groups++
                    markGroup(x, y, grid[x][y], visited)
                }
            }
        }
        return groups
    }

    private fun markGroup(x: Int, y: Int, color: GameColor, visited: Array<BooleanArray>) {
        if (x !in 0 until size || y !in 0 until size) return
        if (visited[x][y]) return
        if (grid[x][y] != color) return

        visited[x][y] = true
        markGroup(x + 1, y, color, visited)
        markGroup(x - 1, y, color, visited)
        markGroup(x, y + 1, color, visited)
        markGroup(x, y - 1, color, visited)
    }

    fun resetTo(savedGrid: Array<Array<GameColor>>, savedTargetColor: GameColor) {
        for (i in savedGrid.indices) {
            for (j in savedGrid[i].indices) {
                grid[i][j] = savedGrid[i][j]
            }
        }
        setTargetColor(savedTargetColor)
    }
}
