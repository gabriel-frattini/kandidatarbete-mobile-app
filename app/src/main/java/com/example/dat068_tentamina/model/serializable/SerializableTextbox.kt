package com.example.dat068_tentamina.model.serializable

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Serializable

@Serializable
data class SerializableTextbox(
    var position: SerializableOffset,
    var text: String,
    var color: String,
    var fontSize: Float,
    var richText: String? = null,
    var richTextContent: String = ""
) : SerializableCanvasObject()
