package com.example.dat068_tentamina.utilities

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.serializable.SerializableCanvasObject
import com.example.dat068_tentamina.model.serializable.SerializableTextbox
import com.example.dat068_tentamina.model.serializable.SerializableLine
import com.example.dat068_tentamina.model.serializable.SerializableOffset
import androidx.compose.ui.text.TextLayoutResult
import com.mohamedrejeb.richeditor.model.RichTextState


data class SerializableTextLayoutResult(
    val text: String // Extract only the text part, or other relevant fields
)

// Extension function to serialize TextLayoutResult
fun TextLayoutResult.toSerializable(): SerializableTextLayoutResult {
    return SerializableTextLayoutResult(
        text = this.layoutInput.text.toString() // Assuming layoutInput contains the original text
    )
}

object CanvasObjectSerializationUtils {
    // Converts CanvasObject to SerializableCanvasObject
    fun CanvasObject.toSerializable(): SerializableCanvasObject {
        return when (this) {
            is TextBox -> SerializableTextbox(
                position = this.position.toSerializable(), // Convert Offset
                text = this.text,
                color = this.color.toString(),
                fontSize = this.fontSize.value,
                richText = this.richText?.toMarkdown(),
                richTextContent = this.richTextContent,
            )
            is Line -> SerializableLine(
                start = this.start.toSerializable(),
                end = this.end.toSerializable(),
                color = this.color.toSerializable(),
                strokeWidth = this.strokeWidth.value,
                cap = this.cap.toString()
            )
            else -> throw IllegalArgumentException("Unknown CanvasObject type")
        }
    }


    // Extension function to convert Offset to SerializableOffset
    fun Offset.toSerializable(): SerializableOffset {
        return SerializableOffset(x = this.x, y = this.y)
    }

    // Extension function to convert SerializableOffset back to Offset
    fun SerializableOffset.toOffset(): Offset {
        return Offset(x, y)
    }

    fun Color.toSerializable(): String {
        // Serialize the color as a string in RGBA format
        return "rgba(${red},${green},${blue},${alpha})"
    }

    fun String.toColor(): Color {
        // Deserialize the color from the RGBA string
        val components = this.removePrefix("rgba(").removeSuffix(")").split(",").map { it.toFloat() }
        if (components.size != 4) {
            throw IllegalArgumentException("Invalid color format")
        }
        return Color(components[0], components[1], components[2], components[3])
    }

    fun String.toRichTextState(): RichTextState {
        return RichTextState().setMarkdown(this)
    }

}
