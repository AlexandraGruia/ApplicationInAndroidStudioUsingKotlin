package com.example.myapplication.ui.games.colormatch

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.ui.games.GamesActivity

class ColorMatchActivity : AppCompatActivity() {

    private lateinit var game: ColorMatchGame
    private lateinit var colorGrid: GridLayout
    private lateinit var targetText: TextView
    private lateinit var movesText: TextView
    private lateinit var paletteContainer: LinearLayout

    private var movesLeft = 3
    private val gridSize = 6
    private val cellSize = 130
    private var baseMoves = 3

    private var selectedCell: Pair<Int, Int>? = null
    private var failCount = 0

    private var initialGridState: Array<Array<GameColor>>? = null
    private var initialTargetColor: GameColor? = null

    private val currentUserId: String by lazy {
        intent.getStringExtra("USER_ID") ?: "defaultUser"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_colormatch)

        colorGrid = findViewById(R.id.colorGrid)
        targetText = findViewById(R.id.targetText)
        movesText = findViewById(R.id.movesLeftText)
        paletteContainer = findViewById(R.id.paletteContainer)

        startNewGame()

        findViewById<Button>(R.id.resetButton).setOnClickListener {
            resetGame()
        }

        createColorPalette()

        val exitIcon = findViewById<ImageView>(R.id.exitIcon)
        exitIcon.setOnClickListener {
            val intent = Intent(this, GamesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun startNewGame() {
        try {
            var newGame: ColorMatchGame
            var attempts = 0
            do {
                newGame = GameColor.generateSolvableGrid(gridSize, baseMoves)
                attempts++
                if (attempts > 100) break
            } while (
                newGame.estimateMinimumMoves() < baseMoves ||
                countDistinctColors(newGame) < 3
            )

            game = newGame
            initialGridState = game.grid.map { it.copyOf() }.toTypedArray()
            initialTargetColor = game.targetColor

            movesLeft = baseMoves
            selectedCell = null
            failCount = 0
            updateGrid()
            updateMovesText()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error starting game: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun countDistinctColors(game: ColorMatchGame): Int {
        return game.grid.flatten().toSet().size
    }

    private fun resetGame() {
        if (game.isSolved()) {
            startNewGame()
        } else {
            if (initialGridState != null && initialTargetColor != null) {
                game.resetTo(initialGridState!!, initialTargetColor!!)
            }

            movesLeft = baseMoves
            selectedCell = null
            failCount++
            updateGrid()
            updateMovesText()

            if (failCount >= 2) {
                failCount = 0
                AlertDialog.Builder(this)
                    .setTitle("Too hard?")
                    .setMessage("You have fewer moves than the estimated minimum to solve. Add 1 extra move?")
                    .setPositiveButton("Yes") { _, _ ->
                        baseMoves++
                        movesLeft = baseMoves
                        updateMovesText()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    private fun createColorPalette() {
        paletteContainer.removeAllViews()
        GameColor.values().forEachIndexed { index, gameColor ->
            val button = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(cellSize, cellSize).apply {
                    setMargins(8, 0, 8, 0)
                }
                backgroundTintList = ContextCompat.getColorStateList(this@ColorMatchActivity, gameColor.toAndroidColor())
                text = (index + 1).toString()
                setTextColor(Color.WHITE)
                setOnClickListener {
                    handleColorClick(gameColor)
                }
                background = ContextCompat.getDrawable(this@ColorMatchActivity, R.drawable.round_button)
            }
            paletteContainer.addView(button)
        }
    }

    private fun handleColorClick(color: GameColor) {
        val cell = selectedCell ?: run {
            Toast.makeText(this, "Select a cell first!", Toast.LENGTH_SHORT).show()
            return
        }

        if (movesLeft <= 0) return

        val (x, y) = cell
        if (game.getColor(x, y) == color) return

        game.applyMoveFrom(x, y, color)
        movesLeft--

        updateGrid()
        updateMovesText()

        if (game.isSolved()) {
            Toast.makeText(this, "You Win!", Toast.LENGTH_SHORT).show()
            failCount = 0
            return
        }

        if (movesLeft == 0) {
            Toast.makeText(this, "Out of moves! Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateGrid() {
        colorGrid.removeAllViews()
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val cell = View(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = cellSize
                        height = cellSize
                        setMargins(8, 8, 8, 8)
                    }
                    background = ContextCompat.getDrawable(this@ColorMatchActivity, R.drawable.grid_cell)
                    backgroundTintList = ContextCompat.getColorStateList(this@ColorMatchActivity, game.getColor(i, j).toAndroidColor())

                    setOnClickListener {
                        selectedCell = Pair(i, j)
                        updateGrid()
                    }
                }

                if (selectedCell == Pair(i, j)) {
                    animateSelection(cell)
                }

                colorGrid.addView(cell)
            }
        }

        val colorName = game.targetColor.name.lowercase().replaceFirstChar { it.uppercase() }
        targetText.text = "Turn all the blocks into: $colorName"
    }

    private fun updateMovesText() {
        movesText.text = "Remaining Moves: $movesLeft"
    }

    private fun animateSelection(view: View) {
        val originalTint = (view.backgroundTintList?.defaultColor ?: Color.TRANSPARENT)
        val accentColor = ContextCompat.getColor(this, R.color.accent_color)

        val animator = ValueAnimator.ofObject(ArgbEvaluator(), originalTint, accentColor)
        animator.duration = 300
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = 1
        animator.addUpdateListener { animation ->
            val color = animation.animatedValue as Int
            view.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        }
        animator.start()

        view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).duration = 150
            }.start()
    }
}
