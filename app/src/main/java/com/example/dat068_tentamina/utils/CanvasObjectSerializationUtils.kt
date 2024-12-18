package com.example.dat068_tentamina.utils

import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.serializable.SerializableCanvasObject
import com.example.dat068_tentamina.model.serializable.SerializableTextbox
import com.example.dat068_tentamina.model.serializable.SerializableLine
import com.example.dat068_tentamina.model.serializable.SerializableOffset
import androidx.compose.ui.text.TextLayoutResult


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
                text = this.text.toSerializable().text,
                color = this.color.toString(),
                fontSize = this.fontSize.value
            )
            is Line -> SerializableLine(
                start = this.start.toSerializable(), // Convert Offset
                end = this.end.toSerializable(),    // Convert Offset
                color = this.color.toString(),
                strokeWidth = this.strokeWidth.value,
                cap = this.cap.toString()
            )
            else -> throw IllegalArgumentException("Unknown CanvasObject type")
        }
    }


    // Extension function to convert Offset to SerializableOffset
    fun androidx.compose.ui.geometry.Offset.toSerializable(): SerializableOffset {
        return SerializableOffset(x = this.x, y = this.y)
    }

    // Extension function to convert SerializableOffset back to Offset
    fun SerializableOffset.toOffset(): androidx.compose.ui.geometry.Offset {
        return androidx.compose.ui.geometry.Offset(x, y)
    }

}