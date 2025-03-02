package com.example.dat068_tentamina.model.serializable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextLayoutResult
import com.example.dat068_tentamina.model.TextBox
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.serialization.Serializable

@Serializable
data class SerializableTextbox(
    var position: SerializableOffset,
    var text: String,
    var color: String,
    var fontSize: Float
) : SerializableCanvasObject()