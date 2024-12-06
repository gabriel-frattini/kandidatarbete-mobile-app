package com.example.dat068_tentamina.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import androidx.compose.material3.TextField as TextField1

@SuppressLint("RememberReturnType")
@Composable
fun DrawingScreen(viewModel: TentaViewModel) {
    var textValue by remember { mutableStateOf("") }
    var textOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var editingTextBox by remember { mutableStateOf<TextBox?>(null) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    androidx.compose.foundation.Canvas(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
            onDragStart = { startPosition ->
                if (!viewModel.textMode.value)
                    viewModel.saveHistory()
            },
            onDrag = { change, dragAmount ->
                if (!viewModel.textMode.value) {
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
                        viewModel.addObject(newLine)
                    }
                }

                }
            )
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    // Handle the press event here, if needed
                },
                // In onTap, do NOT convert to dp:
                onTap = { offset ->
                    Log.d("DrawingScreen", "onTap triggered at offset: $offset")
                    Log.d("DrawingScreen", "Text mode is: ${viewModel.textMode.value}")

                    // offset is in pixels, store it as pixels
                    if (viewModel.textMode.value) {
                        viewModel.saveHistory()
                        textOffset = offset // Store directly as pixels
                    } else {
                        // Find the tapped TextBox in pixels
                        val tappedTextBox = viewModel.objects
                            .filterIsInstance<TextBox>()
                            .find { textBox ->
                                val topLeft = textBox.position // Stored in pixels
                                val sizePx = textBox.text.size // text.size is in pixels by default
                                val bottomRight = Offset(topLeft.x + sizePx.width, topLeft.y + sizePx.height)

                                offset.x in topLeft.x..bottomRight.x &&
                                        offset.y in topLeft.y..bottomRight.y
                            }

                        if (tappedTextBox != null) {
                            editingTextBox = tappedTextBox
                            textValue = tappedTextBox.text.layoutInput.text.text
                            textOffset = tappedTextBox.position // Still in pixels
                        } else {
                            Log.d("DrawingScreen", "No TextBox found at the tapped location")
                        }
                    }
                }

            )
        }
    ) {
        viewModel.objects.forEach { obj ->
            obj.draw(this)

        }
    }

    editingTextBox?.let { editing ->
        // editing.position is in pixels, text.size is in pixels
        val textBoxWidthPx = editing.text.size.width
        val textBoxHeightPx = editing.text.size.height

        // Convert pixels to dp at the very last moment when applying to layout
        val posX = with(density) { editing.position.x.toDp() }
        val posY = with(density) { editing.position.y.toDp() }
        val widthDp = with(density) { textBoxWidthPx.toDp() }
        val heightDp = with(density) { textBoxHeightPx.toDp() }

        Row(
            modifier = Modifier.absoluteOffset(
                x = posX + widthDp,
                y = posY + heightDp
            )
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Edit text") }
            )
            Button(onClick = {
                val updatedTextBox = editing.copy(
                    text = textMeasurer.measure(AnnotatedString(textValue))
                )
                viewModel.replaceObject(editing, updatedTextBox)
                editingTextBox = null
                textValue = ""
                textOffset = Offset(0f, 0f)
            }) {
                Text("OK")
            }
        }
    }





    if (viewModel.textMode.value) {
        // textOffset is in pixels; convert at layout time only
        val offsetX = with(density) { textOffset.x.toDp() }
        val offsetY = with(density) { textOffset.y.toDp() }

        Row(modifier = Modifier.absoluteOffset(x = offsetX, y = offsetY)) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Enter text") }
            )
            Button(onClick = {
                // Store new TextBox with position in pixels
                viewModel.addObject(TextBox(
                    position = textOffset, // Still pixels
                    text = textMeasurer.measure(AnnotatedString(textValue))
                ))
                viewModel.textMode.value = false
                viewModel.eraser = false
                textValue = ""
                textOffset = Offset(0f, 0f)
            }) {
                Text("OK")
            }
        }
    }


}

private fun isInBounds(point: Offset, canvasSize: IntSize): Boolean {
    return point.x in 0f..canvasSize.width.toFloat() && point.y in 0f..canvasSize.height.toFloat()
}