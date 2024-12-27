package com.example.dat068_tentamina.model.serializable

import kotlinx.serialization.Serializable


@Serializable
data class Answer(
    val questionId: Int,
    val canvasObjects: List<SerializableCanvasObject>
)