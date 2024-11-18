package com.example.dat068_tentamina.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.viewmodel.TentaViewModel

@Composable
fun DrawingScreen(viewModel: TentaViewModel) {
    androidx.compose.foundation.Canvas(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
            onDragStart = { startPosition ->
                viewModel.saveHistory()

            },
            onDrag = { change, dragAmount ->
                    change.consume()
                    val startPosition = change.position - dragAmount
                    val endPosition = change.position

                    // Ensure that both start and end positions are within the Canvas bounds
                    if (isInBounds(startPosition, size) && isInBounds(endPosition, size)) {
                        var newLine = Line(
                                start = startPosition,
                                end = endPosition,
                                strokeWidth = viewModel.strokeWidth,
                            )

                        if (viewModel.eraser) {
                            newLine.cap = StrokeCap.Square
                            newLine.color = Color.White
                            newLine.strokeWidth = viewModel.eraserWidth
                        }
                        viewModel.addLine(newLine)
                    }
                }
            )
        }

    ) {
        viewModel.lines.forEach { line ->
            drawLine(
                start = line.start,
                end = line.end,
                color = line.color,
                strokeWidth = line.strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun isInBounds(point: Offset, canvasSize: IntSize): Boolean {
    return point.x in 0f..canvasSize.width.toFloat() && point.y in 0f..canvasSize.height.toFloat()
}