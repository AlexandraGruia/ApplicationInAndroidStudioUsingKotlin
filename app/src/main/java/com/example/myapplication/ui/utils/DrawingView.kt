package com.example.myapplication.ui.utils

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

    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentPath: Path = Path()
    private var currentPaint: Paint = createPaint(Color.BLACK)

    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas

    init {
        paths.add(Pair(currentPath, currentPaint))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::bitmap.isInitialized) {
            bitmap.recycle()
        }
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap)
        bitmap.eraseColor(Color.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(bitmap, 0f, 0f, null)

        for ((path, paint) in paths) {
            canvas.drawPath(path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()
                currentPath.moveTo(x, y)
                paths.add(Pair(currentPath, currentPaint))
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                bitmapCanvas.drawPath(currentPath, currentPaint)
            }
        }

        invalidate()
        return true
    }

    fun setColor(color: Int) {
        currentPaint = createPaint(color)
        currentPath = Path()
        paths.add(Pair(currentPath, currentPaint))
    }

    fun clearCanvas() {
        paths.clear()
        currentPath = Path()
        currentPaint = createPaint(Color.BLACK)
        bitmap.eraseColor(Color.TRANSPARENT)
        invalidate()
    }

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

    fun getDrawingBitmap(): Bitmap {
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        draw(canvas)
        return resultBitmap
    }
}