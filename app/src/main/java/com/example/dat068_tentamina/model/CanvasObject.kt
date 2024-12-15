package com.example.dat068_tentamina.model

import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import org.json.JSONObject


interface CanvasObject {
    fun draw(drawScope: DrawScope)
}