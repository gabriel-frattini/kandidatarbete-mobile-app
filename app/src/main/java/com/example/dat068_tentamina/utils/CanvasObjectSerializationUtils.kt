package com.example.dat068_tentamina.utils

import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.serializable.SerializableCanvasObject
import com.example.dat068_tentamina.model.serializable.SerializableTextbox
import com.example.dat068_tentamina.model.serializable.SerializableLine


object CanvasObjectSerializationUtils {
    // Converts CanvasObject to SerializableCanvasObject
    fun CanvasObject.toSerializable(): SerializableCanvasObject {
        return when (this) {
            is TextBox -> SerializableTextbox(
                position = this.position,
                text = this.text.toString(), // Convert TextLayoutResult to String
                color = this.color.toString(),
                fontSize = this.fontSize.value
            )
            is Line -> SerializableLine(
                start = this.start,
                end = this.end,
                color = this.color.toString(),
                strokeWidth = this.strokeWidth.value,
                cap = this.cap.toString()
            )
            else -> throw IllegalArgumentException("Unknown CanvasObject type")
        }
    }
}