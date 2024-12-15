package com.example.dat068_tentamina.model.serializable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlinx.serialization.Serializable

@Serializable
data class SerializableLine (
    val start: Offset,
    val end: Offset,
    var color: String = Color.Black.toString(),
    var strokeWidth: Float = 1f,
    var cap: String = StrokeCap.Round.toString()
) : SerializableCanvasObject()