package com.example.dat068_tentamina.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TextBox(
    var position: Offset,
    var layoutRes: TextLayoutResult,
    var text : String,
    var color: Color = Color.Black,
    var fontSize: TextUnit = 30.sp
) : CanvasObject {
    override fun draw(drawScope: DrawScope) {
        drawScope.drawText(
            textLayoutResult = layoutRes,
            topLeft = Offset(
                x = position.x * 2,
                y = position.y * 2
            )
        )
    }

}



