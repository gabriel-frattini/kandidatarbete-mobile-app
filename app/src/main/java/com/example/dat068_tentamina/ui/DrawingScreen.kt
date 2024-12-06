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
                onTap = { offset ->
                    Log.d("DrawingScreen", "onTap triggered at offset: $offset")
                    Log.d("DrawingScreen", "Text mode is: ${viewModel.textMode.value}")
                    // Convert the raw offset (in pixels) to dp
                    val tappedDpOffset = with(density) {
                        Offset(offset.x.toDp().value, offset.y.toDp().value)
                    }

                    if (viewModel.textMode.value) {
                        viewModel.saveHistory()
                        Log.d("DrawingScreen", "Saved history in text mode")

                        // Store the text offset in dp as well
                        textOffset = tappedDpOffset
                        Log.d("DrawingScreen", "Text offset set to: $textOffset")
                    } else {
                        Log.d("DrawingScreen", "Looking for a TextBox at the tapped location")

                        // When checking if the tap hits a TextBox, we assume the TextBoxes are also stored in dp
                        val tappedTextBox = viewModel.objects
                            .filterIsInstance<TextBox>()
                            .find { textBox ->
                                Log.d("DrawingScreen", "Checking TextBox at position: ${textBox.position}")

                                // TextBox.position and text measurements are assumed to be in dp
                                val topLeft = textBox.position
                                val size = Offset(
                                    textBox.text.size.width.toFloat(),
                                    textBox.text.size.height.toFloat()
                                )
                                val bottomRight = Offset(topLeft.x + size.x, topLeft.y + size.y)

                                val isWithinBounds = tappedDpOffset.x in topLeft.x..bottomRight.x &&
                                        tappedDpOffset.y in topLeft.y..bottomRight.y

                                Log.d("DrawingScreen", "TextBox bounds: $topLeft to $bottomRight, Tapped inside: $isWithinBounds")
                                isWithinBounds
                            }

                        if (tappedTextBox != null) {
                            Log.d("DrawingScreen", "Tapped TextBox found: $tappedTextBox")
                            editingTextBox = tappedTextBox
                            textValue = tappedTextBox.text.layoutInput.text.text
                            Log.d("DrawingScreen", "Editing TextBox with text: $textValue")
                            textOffset = tappedTextBox.position
                            Log.d("DrawingScreen", "TextBox offset set to: $textOffset")
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
        // Convert text size (in pixels) to dp
        val textBoxWidthDp = with(density) { editing.text.size.width.toDp() }
        val textBoxHeightDp = with(density) { editing.text.size.height.toDp() }

        // editing.position should already be in dp units
        val textBoxPositionXDp = editing.position.x.dp
        val textBoxPositionYDp = editing.position.y.dp

        Row(
            modifier = Modifier.absoluteOffset(
                x = textBoxPositionXDp + textBoxWidthDp,
                y = textBoxPositionYDp + textBoxHeightDp
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
        Row (modifier = Modifier.absoluteOffset(
            x = textOffset.x.dp,
            y = textOffset.y.dp
        )) {
            OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            label = { Text("Enter text") }
            )
            Button(onClick = {
                viewModel.addObject(TextBox(
                    position = textOffset,
                    text = textMeasurer.measure(AnnotatedString(textValue))
                ))
                viewModel.textMode.value = false
                viewModel.eraser = false
                textValue = ""
                textOffset = Offset(0f, 0f)
            }) { Text("OK") }
        }

    }

}

private fun isInBounds(point: Offset, canvasSize: IntSize): Boolean {
    return point.x in 0f..canvasSize.width.toFloat() && point.y in 0f..canvasSize.height.toFloat()
}