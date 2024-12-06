package com.example.dat068_tentamina.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

@SuppressLint("RememberReturnType")
@Composable
fun DrawingScreen(viewModel: TentaViewModel) {
    var textValue by remember { mutableStateOf("") }
    var textOffset by remember { mutableStateOf(Offset.Zero) }
    var editingTextBox by remember { mutableStateOf<TextBox?>(null) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        if (!viewModel.textMode.value) viewModel.saveHistory()
                    },
                    onDrag = { change, dragAmount ->
                        if (!viewModel.textMode.value) {
                            change.consume()
                            val startPosition = change.position - dragAmount
                            val endPosition = change.position
                            if (isInBounds(startPosition, size) && isInBounds(endPosition, size)) {
                                val newLine = Line(
                                    start = startPosition,
                                    end = endPosition,
                                    strokeWidth = if (viewModel.eraser) viewModel.eraserWidth else viewModel.strokeWidth,
                                    color = if (viewModel.eraser) Color.White else Color.Black,
                                    cap = if (viewModel.eraser) StrokeCap.Square else StrokeCap.Round
                                )
                                viewModel.addObject(newLine)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { /* Handle press if needed */ },
                    onTap = { offset ->
                        Log.d("DrawingScreen", "onTap triggered at offset: $offset")
                        Log.d("DrawingScreen", "Text mode is: ${viewModel.textMode.value}")

                        if (viewModel.textMode.value) {
                            // If already typing and user taps again:
                            if (textValue.isNotEmpty()) {
                                // Create the TextBox right now
                                viewModel.addObject(
                                    TextBox(
                                        position = textOffset,
                                        text = textMeasurer.measure(AnnotatedString(textValue))
                                    )
                                )
                                viewModel.textMode.value = false
                                viewModel.eraser = false
                                textValue = ""
                                textOffset = Offset.Zero
                            } else {
                                // Otherwise, just change the position
                                viewModel.saveHistory()
                                textOffset = offset
                            }
                        } else {
                            // Check if a TextBox was tapped for editing
                            val tappedTextBox = viewModel.objects
                                .filterIsInstance<TextBox>()
                                .find { textBox ->
                                    val topLeft = textBox.position
                                    val sizePx = textBox.text.size
                                    val bottomRight = Offset(topLeft.x + sizePx.width, topLeft.y + sizePx.height)
                                    offset.x in topLeft.x..bottomRight.x && offset.y in topLeft.y..bottomRight.y
                                }

                            if (tappedTextBox != null) {
                                editingTextBox = tappedTextBox
                                textValue = tappedTextBox.text.layoutInput.text.text
                                textOffset = tappedTextBox.position
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

    // Creating a new TextBox
    if (viewModel.textMode.value) {
        val offsetX = with(density) { textOffset.x.toDp() }
        val offsetY = with(density) { textOffset.y.toDp() }

        var hasBeenFocused by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Row(
            modifier = Modifier.absoluteOffset(x = offsetX, y = offsetY)
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Enter text") },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        // Only handle losing focus after it has been focused at least once
                        if (!focusState.isFocused && hasBeenFocused) {
                            // Focus lost, create the TextBox
                            viewModel.addObject(
                                TextBox(
                                    position = textOffset,
                                    text = textMeasurer.measure(AnnotatedString(textValue))
                                )
                            )
                            viewModel.textMode.value = false
                            viewModel.eraser = false
                            textValue = ""
                            textOffset = Offset.Zero
                        }
                        if (focusState.isFocused) {
                            hasBeenFocused = true
                        }
                    }
            )
        }
    }

    // Editing existing TextBox
    editingTextBox?.let { editing ->
        val textBoxWidthPx = editing.text.size.width
        val textBoxHeightPx = editing.text.size.height

        val posX = with(density) { editing.position.x.toDp() }
        val posY = with(density) { editing.position.y.toDp() }
        val widthDp = with(density) { textBoxWidthPx.toDp() }
        val heightDp = with(density) { textBoxHeightPx.toDp() }

        var hasBeenFocused by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Row(
            modifier = Modifier
                .absoluteOffset(
                    x = posX + widthDp,
                    y = posY + heightDp
                )
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Edit text") },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        Log.d("focus", "Edit text field focus changed: isFocused=${focusState.isFocused}, hasBeenFocused=$hasBeenFocused, textValue=$textValue")

                        if (!focusState.isFocused && hasBeenFocused) {
                            Log.d("focus", "Focus lost on edit field; updating the TextBox...")
                            val updatedTextBox = editing.copy(
                                text = textMeasurer.measure(AnnotatedString(textValue))
                            )
                            viewModel.replaceObject(editing, updatedTextBox)
                            editingTextBox = null
                            textValue = ""
                            textOffset = Offset.Zero
                            Log.d("focus", "TextBox updated and editing ended.")
                        }

                        if (focusState.isFocused) {
                            Log.d("focus", "Edit text field gained focus.")
                            hasBeenFocused = true
                        }
                    }
            )
        }
    }
}

private fun isInBounds(point: Offset, canvasSize: IntSize): Boolean {
    return point.x in 0f..canvasSize.width.toFloat() && point.y in 0f..canvasSize.height.toFloat()
}
