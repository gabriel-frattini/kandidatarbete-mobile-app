package com.example.dat068_tentamina.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Line(
    val start: Offset,
    val end: Offset,
    var color: Color = Color.Black,
    var strokeWidth: Dp = 1.dp,
    var cap: StrokeCap = StrokeCap.Round
)