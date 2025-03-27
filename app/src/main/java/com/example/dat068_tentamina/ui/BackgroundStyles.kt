package com.example.dat068_tentamina.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.drawGraphPaperPattern(canvasSize: Size) {
    val stepSize = 50f
    val gridColor = Color.LightGray

    for (x in 0..canvasSize.width.toInt() step stepSize.toInt()) {
        drawLine(
            color = gridColor,
            start = Offset(x.toFloat(), 0f),
            end = Offset(x.toFloat(), canvasSize.height),
            strokeWidth = 1f
        )
    }

    for (y in 0..canvasSize.height.toInt() step stepSize.toInt()) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y.toFloat()),
            end = Offset(canvasSize.width, y.toFloat()),
            strokeWidth = 1f
        )
    }
}

fun DrawScope.drawLinedPaperPattern(canvasSize: Size) {
    val stepSize = 50f
    val lineColor = Color.LightGray

    for (y in 0..canvasSize.height.toInt() step stepSize.toInt()) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y.toFloat()),
            end = Offset(canvasSize.width, y.toFloat()),
            strokeWidth = 1f
        )
    }
}

fun DrawScope.drawDottedPattern(canvasSize: Size) {
    val stepSize = 50f
    val dotSize = 3f
    val dotColor = Color.LightGray

    for (x in 0..canvasSize.width.toInt() step stepSize.toInt()) {
        for (y in 0..canvasSize.height.toInt() step stepSize.toInt()) {
            drawCircle(
                color = dotColor,
                center = Offset(x.toFloat(), y.toFloat()),
                radius = dotSize
            )
        }
    }
}
