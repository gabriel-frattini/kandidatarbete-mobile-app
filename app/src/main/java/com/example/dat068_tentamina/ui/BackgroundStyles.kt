package com.example.dat068_tentamina.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

//Grid Paper Background
@Composable
fun DrawGraphPaperBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val stepSize = 50f
        val canvasWidth = size.width
        val canvasHeight = size.height
        val gridColor = Color.LightGray

        // Draw vertical lines
        for (x in 0..canvasWidth.toInt() step stepSize.toInt()) {
            drawLine(color = gridColor, start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), canvasHeight), strokeWidth = 1f)
        }
        // Draw horizontal lines
        for (y in 0..canvasHeight.toInt() step stepSize.toInt()) {
            drawLine(color = gridColor, start = Offset(0f, y.toFloat()), end = Offset(canvasWidth, y.toFloat()), strokeWidth = 1f)
        }
    }
}

//Lined Paper Background
@Composable
fun DrawLinedPaperBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val stepSize = 50f
        val canvasWidth = size.width
        val canvasHeight = size.height
        val lineColor = Color.LightGray

        for (y in 0..canvasHeight.toInt() step stepSize.toInt()) {
            drawLine(color = lineColor, start = Offset(0f, y.toFloat()), end = Offset(canvasWidth, y.toFloat()), strokeWidth = 1f)
        }
    }
}

//Dotted Paper Background
@Composable
fun DrawDottedBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val stepSize = 50f
        val canvasWidth = size.width
        val canvasHeight = size.height
        val dotColor = Color.LightGray
        val dotSize = 3f

        for (x in 0..canvasWidth.toInt() step stepSize.toInt()) {
            for (y in 0..canvasHeight.toInt() step stepSize.toInt()) {
                drawCircle(color = dotColor, center = Offset(x.toFloat(), y.toFloat()), radius = dotSize)
            }
        }
    }
}
