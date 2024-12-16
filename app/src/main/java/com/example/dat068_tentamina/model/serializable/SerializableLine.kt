package com.example.dat068_tentamina.model.serializable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlinx.serialization.Serializable

@Serializable
data class SerializableLine(
    val start: SerializableOffset,
    val end: SerializableOffset,
    var color: String = "Black",
    var strokeWidth: Float = 1f,
    var cap: String = "Round"
) : SerializableCanvasObject()