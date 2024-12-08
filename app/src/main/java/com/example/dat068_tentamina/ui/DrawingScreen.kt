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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@SuppressLint("RememberReturnType")
@Composable
fun DrawingScreen(viewModel: TentaViewModel) {
    var textValue by remember { mutableStateOf("") }
    var textOffset by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        if (!viewModel.textMode.value) {
                            viewModel.saveHistory()
                        }
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
                    onTap = { offset ->
                        if (viewModel.textMode.value) {
                            if (textValue.isEmpty()) {
                                viewModel.saveHistory()
                                textOffset = offset
                            } else {
                                createTextBox(viewModel, textValue, textOffset, textMeasurer)
                                textValue = ""
                                textOffset = Offset.Zero
                                viewModel.textMode.value = false
                                viewModel.eraser = false
                            }
                        } else {
                            val tappedTextBox = findTappedTextBox(viewModel, offset)
                            if (tappedTextBox != null) {
                                viewModel.removeObject(tappedTextBox)
                                viewModel.saveHistory()
                                textOffset = tappedTextBox.position
                                textValue = tappedTextBox.text.layoutInput.text.text
                                viewModel.textMode.value = true
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

    if (viewModel.textMode.value && textOffset != Offset.Zero) {
        EditableTextField(
            initialText = textValue,
            offset = textOffset,
            label = "Enter text",
            onTextChange = { textValue = it },
            onFocusLost = {
                if (textValue.isNotEmpty()) {
                    createTextBox(viewModel, textValue, textOffset, textMeasurer)
                }
                textValue = ""
                textOffset = Offset.Zero
                viewModel.textMode.value = false
                viewModel.eraser = false
            }
        )
    }
}

@Composable
private fun EditableTextField(
    initialText: String,
    offset: Offset,
    label: String,
    onTextChange: (String) -> Unit,
    onFocusLost: () -> Unit
) {
    var hasBeenFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var value by remember { mutableStateOf(initialText) }

    val density = LocalDensity.current
    val offsetX = with(density) { offset.x.toDp() }
    val offsetY = with(density) { offset.y.toDp() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .absoluteOffset(x = offsetX, y = offsetY)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
                onTextChange(it)
            },
            label = { Text(label) },
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && hasBeenFocused) {
                        onFocusLost()
                    }
                    if (focusState.isFocused) {
                        hasBeenFocused = true
                    }
                }
        )
    }
}

private fun createTextBox(
    viewModel: TentaViewModel,
    textValue: String,
    textOffset: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val measuredText = textMeasurer.measure(AnnotatedString(textValue))
    viewModel.addObject(
        TextBox(
            position = textOffset,
            text = measuredText
        )
    )
}

private fun findTappedTextBox(viewModel: TentaViewModel, offset: Offset): TextBox? {
    return viewModel.objects
        .filterIsInstance<TextBox>()
        .find { textBox ->
            val topLeft = textBox.position
            val sizePx = textBox.text.size
            val bottomRight = Offset(topLeft.x + sizePx.width, topLeft.y + sizePx.height)
            offset.x in topLeft.x..bottomRight.x && offset.y in topLeft.y..bottomRight.y
        }
}

private fun isInBounds(point: Offset, canvasSize: IntSize): Boolean {
    return point.x in 0f..canvasSize.width.toFloat() && point.y in 0f..canvasSize.height.toFloat()
}
