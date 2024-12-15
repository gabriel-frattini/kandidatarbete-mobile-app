package com.example.dat068_tentamina.model.serializable

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Serializable

@Serializable
data class SerializableTextbox (
    var position: Offset,
    var text: String,
    var color: String,
    var fontSize: Float
) : SerializableCanvasObject()