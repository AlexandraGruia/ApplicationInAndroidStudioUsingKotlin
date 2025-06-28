package com.example.myapplication.ui.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.myapplication.R
import java.io.ByteArrayOutputStream

class DrawingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        val drawingView = findViewById<DrawingView>(R.id.drawingView)
        findViewById<Button>(R.id.clearButton).setOnClickListener {
            drawingView.clearCanvas()
        }

        findViewById<Button>(R.id.colorButton).setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.color_picker_dialog, null)
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Pick a Color")
                .setView(dialogView)
                .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
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

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            val drawingBitmap: Bitmap? = drawingView.getDrawingBitmap()

            if (drawingBitmap != null) {
                val stream = ByteArrayOutputStream()
                drawingBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()

                val resultIntent = Intent().apply {
                    putExtra("drawingBitmap", byteArray)
                }

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {

            }
        }
    }
}
