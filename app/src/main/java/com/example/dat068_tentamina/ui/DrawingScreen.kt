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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.viewmodel.ExamInfo

import com.example.dat068_tentamina.viewmodel.TentaViewModel
import androidx.compose.material3.TextField as TextField1

@SuppressLint("RememberReturnType")
@Composable
fun DrawingScreen(viewModel: TentaViewModel,examInfo: ExamInfo, recoveryMode: Boolean) {
    var textValue by remember { mutableStateOf("") }
    var textOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    val textMeasurer = rememberTextMeasurer()

    if (recoveryMode) {
        examInfo.continueAlreadyStartedExam(textMeasurer)
        viewModel.changeQuestion(1)
        viewModel.disableRecoveryMode()
    }

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
                    if (viewModel.textMode.value) {
                        viewModel.saveHistory()
                        textOffset = Offset(offset.x / density, offset.y / density)
                    }
                }
            )
        }
    ) {
        viewModel.objects.forEach { obj ->
            obj.draw(this)

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