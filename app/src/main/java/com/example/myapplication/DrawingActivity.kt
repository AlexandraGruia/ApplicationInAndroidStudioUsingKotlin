package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import java.io.ByteArrayOutputStream


class DrawingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        val drawingView = findViewById<DrawingView>(R.id.drawingView)

        val clearButton = findViewById<Button>(R.id.clearButton)
        clearButton.setOnClickListener {
            drawingView.clearCanvas()
        }

        val colorButton = findViewById<Button>(R.id.colorButton)
        colorButton.setOnClickListener {
            drawingView.setColor(Color.BLACK)
        }

        clearButton.setOnClickListener {
            Log.d("DrawingActivity", "Clear button clicked")
            drawingView.clearCanvas()
        }

        colorButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.color_picker_dialog, null)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Pick a Color")
                .setView(dialogView)
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create()

            dialogView.findViewById<Button>(R.id.colorRed).setOnClickListener {
                drawingView.setColor(Color.RED)
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.colorGreen).setOnClickListener {
                drawingView.setColor(Color.GREEN)
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.colorBlue).setOnClickListener {
                drawingView.setColor(Color.BLUE)
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.colorYellow).setOnClickListener {
                drawingView.setColor(Color.YELLOW)
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.colorBlack).setOnClickListener {
                drawingView.setColor(Color.BLACK)
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.colorWhite).setOnClickListener {
                drawingView.setColor(Color.WHITE)
                dialog.dismiss()
            }

            dialog.show()
        }

        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            val drawingBitmap = drawingView.getDrawingBitmap()

            val stream = ByteArrayOutputStream()
            drawingBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            val resultIntent = Intent().apply {
                putExtra("drawingBitmap", byteArray)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    }
}
