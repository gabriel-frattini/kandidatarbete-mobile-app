package com.example.dat068_tentamina.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.TextBox

import com.example.dat068_tentamina.viewmodel.TentaViewModel
import kotlin.math.max
import androidx.compose.material3.TextField as TextField1

// Changed TextBox.kt. Currently only scrolling is possible. Zooming needs to be implemented. Lags on my pc idk if i impacted performance.

@SuppressLint("RememberReturnType")
@Composable
fun DrawingScreen(viewModel: TentaViewModel, examInfo : ExamInfo, recoveryMode : Boolean) {
    var isScrollMode by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf("") }
    var textOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    val textMeasurer = rememberTextMeasurer()
    val verticalScrollState = rememberScrollState()
    val density = LocalDensity.current.density
    var context = LocalContext.current

    var canvasHeight by remember { mutableStateOf(viewModel.currentCanvasHeight.value) }

    // Trigger recovery mode only once
    LaunchedEffect(recoveryMode) {
        if (recoveryMode) {
            Log.d("Backup", "Starting recovery mode...")
            val success = examInfo.continueAlreadyStartedExam(textMeasurer,context)
            Log.d("Backup", "Recovery mode status: $success")
            if (success) {
                examInfo.startBackUp(context)
                viewModel.changeQuestion(
                    qNr = 1,
                    newObjects = viewModel.objects.toList(),
                    canvasHeight = 2400.dp
                )}
        }
    }

    LaunchedEffect(viewModel.currentQuestion.intValue) {
        canvasHeight = viewModel.currentCanvasHeight.value
    }
    LaunchedEffect(viewModel.objects) {
        val newHeight = calculateCanvasHeight(viewModel.objects, density)
        Log.d("CanvasDebug", "Calculated New Height: $newHeight, Current Height: $canvasHeight")
        if (newHeight > canvasHeight) {
            canvasHeight = newHeight
            viewModel.updateCanvasHeight(newHeight)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(verticalScrollState, enabled = isScrollMode) // Enables vertical scrolling
                .height(canvasHeight)
                .background(Color.White)
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isScrollMode) {
                        if (!isScrollMode) {
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
                                            expandCanvasIfNeeded(newLine, density, canvasHeight) {
                                                canvasHeight = it
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    .pointerInput(isScrollMode) {
                        if (!isScrollMode) {
                            detectTapGestures(
                                onTap = { offset ->
                                    if (viewModel.textMode.value) {
                                        viewModel.saveHistory()
                                        textOffset = Offset(
                                            offset.x,
                                            offset.y + verticalScrollState.value
                                        )
                                        Log.d("Debug", "Tap offset: $offset, Scroll: ${verticalScrollState.value}, TextOffset: $textOffset")
                                    }
                                }
                            )
                        }
                    }
            ) {

                viewModel.objects.forEach { obj ->
                    obj.draw(this)
                }
            }
        }

        // Text input mode
        if (viewModel.textMode.value) {
            val textOffsetDp = with(LocalDensity.current) {
                Offset(textOffset.x / density, textOffset.y / density) // Convert pixels to dp
            }

            Row(
                modifier = Modifier.absoluteOffset(
                    x = textOffsetDp.x.dp,
                    y = textOffsetDp.y.dp - (verticalScrollState.value.dp)
                )) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("Enter text") }
                )
                Button(
                    onClick = {
                        val newTextBox = TextBox(
                            position = Offset(
                                textOffset.x,
                                textOffset.y - verticalScrollState.value
                            ),
                            textLayout = textMeasurer.measure(AnnotatedString(textValue)),
                            text = textValue
                        )
                        viewModel.addObject(newTextBox)
                        expandCanvasIfNeeded(newTextBox, density, canvasHeight) {
                            canvasHeight = it
                        }
                        // Reset state
                        viewModel.textMode.value = false
                        viewModel.eraser = false
                        textValue = ""
                        textOffset = Offset(0f, 0f)
                    }
                ) {
                    Text("OK")
                }
            }
        }



        // Toggle between scroll and draw modes
        Button(
            onClick = {
                isScrollMode = !isScrollMode
                Log.d("Debug", "Switched to ${if (isScrollMode) "Scroll Mode" else "Draw Mode"}")
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(if (isScrollMode) "Enable Draw Mode" else "Enable Scroll Mode")
        }
    }
}


private fun calculateCanvasHeight(objects: List<CanvasObject>, density: Float): Dp {
    val maxY = objects.maxOfOrNull { obj ->
        when (obj) {
            is Line -> max(obj.start.y, obj.end.y)
            is TextBox -> obj.position.y + obj.textLayout.size.height
            else -> 0f
        }
    } ?: 0f

    // Convert the maximum Y position to dp and add a buffer space
    return ((maxY / density) + 200).dp
}

private fun expandCanvasIfNeeded(
    obj: CanvasObject,
    density: Float,
    currentHeight: Dp,
    onHeightUpdate: (Dp) -> Unit
) {
    val thresholdPx = 400f // Expand if the object is within 400px of the bottom
    val bottomY = when (obj) {
        is Line -> max(obj.start.y, obj.end.y)
        is TextBox -> obj.position.y + obj.textLayout.size.height
        else -> 0f
    }
    val currentHeightPx = currentHeight.value * density


    // Expand canvas if the object is close to the current height
    if (currentHeightPx - bottomY <= thresholdPx) {
        val newHeight = (currentHeightPx / density).dp + 600.dp // Add buffer space
        onHeightUpdate(newHeight)
    }
}


private fun isInBounds(point: Offset, canvasSize: IntSize): Boolean {
    return point.x in 0f..canvasSize.width.toFloat() && point.y in 0f..canvasSize.height.toFloat()
}