package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity


class DrawingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        // Find DrawingView instance
        val drawingView = findViewById<DrawingView>(R.id.drawingView)


        // Find and set up the clear button to clear the drawing
        val clearButton = findViewById<Button>(R.id.clearButton)
        clearButton.setOnClickListener {
            drawingView.clearCanvas()  // Clear the canvas when button is clicked
        }

        // Find and set up the color button to change the drawing color
        val colorButton = findViewById<Button>(R.id.colorButton)
        colorButton.setOnClickListener {
            drawingView.setColor(Color.RED)  // Change the drawing color to red
        }

        clearButton.setOnClickListener {
            Log.d("DrawingActivity", "Clear button clicked")
            drawingView.clearCanvas()
        }

        colorButton.setOnClickListener {
            // Inflate the custom color picker layout
            val dialogView = layoutInflater.inflate(R.layout.color_picker_dialog, null)

            // Build the dialog
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Pick a Color")
                .setView(dialogView)
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create()

            // Set up color button listeners
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

            // Show the dialog
            dialog.show()
        }

        // In DrawingActivity, after the drawing is done:
        val saveButton = findViewById<Button>(R.id.saveButton)  // Assuming you have a save button
        saveButton.setOnClickListener {
            // Get the drawing bitmap
            val drawingBitmap = drawingView.getDrawingBitmap()  // This is your drawing view's bitmap


            val resultIntent = Intent(this, NewNoteActivity::class.java)
            resultIntent.putExtra("drawingBitmap", drawingBitmap)  // Pass the bitmap to NewNoteActivity

            startActivity(resultIntent)  // Start NewNoteActivity with the bitmap
            finish()  // Close the DrawingActivity
        }


    }
}
