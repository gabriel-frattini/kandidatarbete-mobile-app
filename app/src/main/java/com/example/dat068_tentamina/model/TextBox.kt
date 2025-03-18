package com.example.dat068_tentamina.model

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.MultiParagraph
import androidx.compose.ui.text.TextLayoutInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState

data class TextBox(
    var position: Offset,
    var text : String,
    var textLayout: TextLayoutResult,
    var color: Color = Color.Black,
    var fontSize: Dp = 16.dp,
    var richText: RichTextState? = null
) : CanvasObject {
    override fun draw(drawScope: DrawScope) {
        drawScope.drawText(
            textLayoutResult = textLayout,
            topLeft = position // Use position directly without scaling
        )
    }

}



