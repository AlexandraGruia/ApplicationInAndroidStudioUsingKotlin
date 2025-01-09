package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paths = mutableListOf<Pair<Path, Paint>>()  // Store paths with their paints
    private var currentPath: Path = Path()
    private var currentPaint: Paint = createPaint(Color.BLACK)  // Default paint is black

    // Bitmap to hold the background content
    private val bitmap: Bitmap = Bitmap.createBitmap(800, 1280, Bitmap.Config.ARGB_8888)
    private val canvas: Canvas = Canvas(bitmap)

    init {
        // Initialize the first path and paint
        paths.add(Pair(currentPath, currentPaint))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the bitmap
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        // Draw all saved paths with their respective paints
        for ((path, paint) in paths) {
            canvas.drawPath(path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()  // Create a new path for each stroke
                currentPath.moveTo(x, y)
                paths.add(Pair(currentPath, currentPaint))  // Add the new path with the current paint
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)  // Add lines to the current path
            }
            MotionEvent.ACTION_UP -> {
                canvas.drawPath(currentPath, currentPaint)  // Draw the path on the bitmap
            }
        }

        invalidate()  // Request a redraw
        return true
    }

    // Change the color for new paths
    fun setColor(color: Int) {
        currentPaint = createPaint(color)
    }

    // Clear the canvas
    fun clearCanvas() {
        paths.clear()  // Clear all paths
        currentPath = Path()
        currentPaint = createPaint(Color.BLACK)
        bitmap.eraseColor(Color.TRANSPARENT)  // Clear the bitmap
        invalidate()  // Request a redraw
    }

    // Helper function to create a new Paint object
    private fun createPaint(color: Int): Paint {
        return Paint().apply {
            this.color = color
            this.strokeWidth = 10f
            this.isAntiAlias = true
            this.style = Paint.Style.STROKE
            this.strokeJoin = Paint.Join.ROUND
            this.strokeCap = Paint.Cap.ROUND
        }
    }

    // Now get the bitmap of the drawing
    fun getDrawingBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)  // Draw the view's content onto the canvas
        return bitmap
    }
}
