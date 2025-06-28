package com.example.myapplication.ui.games.sudoku

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class SudokuView(context: Context, private val game: SudokuGame) : View(context) {
    var selectedX = -1
    var selectedY = -1
    val wrongCells = mutableSetOf<Pair<Int, Int>>()
    private val paint = Paint()

    init {
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val cellSize = width / 9
                val x = (event.x / cellSize).toInt().coerceIn(0, 8)
                val y = (event.y / cellSize).toInt().coerceIn(0, 8)

                if (!game.isFixed(x, y)) {
                    selectedX = x
                    selectedY = y
                    invalidate()

                    if (context is SudokuActivity) {
                        context.showNumberPicker()
                    }
                }
            }
            true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec).coerceAtMost(MeasureSpec.getSize(heightMeasureSpec))
        val measureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(measureSpec, measureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        val size = width.coerceAtMost(height).toFloat()
        val cellSize = size / 9f

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#ffe0f0")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)


        if (selectedX in 0..8 && selectedY in 0..8) {
            paint.color = Color.parseColor("#ff99cc")
            canvas.drawRect(
                selectedX * cellSize,
                selectedY * cellSize,
                (selectedX + 1) * cellSize,
                (selectedY + 1) * cellSize,
                paint
            )
        }

        for ((x, y) in wrongCells) {
            paint.color = Color.RED
            canvas.drawRect(
                x * cellSize.toFloat(),
                y * cellSize.toFloat(),
                (x + 1) * cellSize.toFloat(),
                (y + 1) * cellSize.toFloat(),
                paint
            )
        }


        paint.color = Color.BLACK
        paint.textSize = cellSize * 0.7f
        paint.textAlign = Paint.Align.CENTER
        val fontMetrics = paint.fontMetrics
        val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom

        for (i in 0..8) {
            for (j in 0..8) {
                val num = game.get(i, j)
                if (num != 0) {
                    canvas.drawText(
                        num.toString(),
                        (i + 0.5f) * cellSize,
                        (j + 0.5f) * cellSize + yOffset,
                        paint
                    )
                }
            }
        }

        paint.color = Color.GRAY
        paint.strokeWidth = 2f
        for (i in 1..8) {
            canvas.drawLine(i * cellSize, 0f, i * cellSize, size.toFloat(), paint)
            canvas.drawLine(0f, i * cellSize, size.toFloat(), i * cellSize, paint)
        }

        paint.strokeWidth = 5f
        for (i in 0..9 step 3) {
            canvas.drawLine(i * cellSize, 0f, i * cellSize, size.toFloat(), paint)
            canvas.drawLine(0f, i * cellSize, size.toFloat(), i * cellSize, paint)
        }
    }
}