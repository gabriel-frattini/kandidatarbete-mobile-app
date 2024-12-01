package com.example.dat068_tentamina.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Line (
    val start: Offset,
    val end: Offset,
    var color: Color = Color.Black,
    var strokeWidth: Dp = 1.dp,
    var cap: StrokeCap = StrokeCap.Round
) : CanvasObject {
    override fun draw(drawScope: DrawScope) {
        // Pass parameters in the correct order without named arguments
        drawScope.drawLine(
            start = start,             // Correct order and no named argument
            end = end,
            color = color,
            strokeWidth = strokeWidth.value,
            cap = cap
        )
    }
}
