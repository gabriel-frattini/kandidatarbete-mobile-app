package com.example.dat068_tentamina.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class TextBox(
    var position: Offset,
    var text: TextLayoutResult,
    var color: Color = Color.Black,
    var fontSize: Dp = 16.dp
) : CanvasObject {
    override fun draw(drawScope: DrawScope) {
        drawScope.drawText(
            textLayoutResult = text,
            topLeft = position // Use position directly without scaling
        )
    }


}



