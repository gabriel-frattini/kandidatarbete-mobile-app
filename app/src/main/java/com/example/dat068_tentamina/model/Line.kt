package com.example.dat068_tentamina.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


data class Line (
    var start: Offset,
    var end: Offset,
    var color: Color = Color.Black,
    var strokeWidth: Dp = 1.dp,
    var cap: StrokeCap = StrokeCap.Round
) : CanvasObject  {
    override fun draw(drawScope: DrawScope) {
        drawScope.drawLine(
            start = start,
            end = end,
            color = color,
            cap = cap ,
            strokeWidth = strokeWidth.value
        )
        if (start == end) {
            // Draw a dot
            drawScope.drawCircle(
                color = color,
                radius = strokeWidth.value,
                center = start
            )
        }
    }

    fun deepCopy(): CanvasObject {
        return Line(
            start = Offset(start.x, start.y),
            end = Offset(end.x, end.y),
            color = Color(color.value),
            strokeWidth = strokeWidth,
            cap = cap
        )
    }
}