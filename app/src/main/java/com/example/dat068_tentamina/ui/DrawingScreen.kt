package com.example.dat068_tentamina.ui


import ExamInfo
import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.viewmodel.BackgroundType
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import kotlin.math.max
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlin.math.abs
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip


@SuppressLint("RememberReturnType")
@Composable
fun DrawingScreen(viewModel: TentaViewModel, examInfo : ExamInfo, recoveryMode : Boolean) {
    var isScrollMode by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf("") }
    var textOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    val textMeasurer = rememberTextMeasurer()
    // Scroll state for vertical scrolling
    val verticalScrollState = remember(viewModel.currentQuestion.intValue) {
        ScrollState(initial = viewModel.scrollPositions[viewModel.currentQuestion.intValue] ?: 0)
    }
    val density = LocalDensity.current.density
    var context = LocalContext.current

    var canvasHeight by remember { mutableStateOf(viewModel.currentCanvasHeight.value) }

    val isMarkAreaMode = viewModel.mark;
    var isMoveMode by remember { mutableStateOf(false) }
    var markAreaStart by remember { mutableStateOf<Offset?>(null) }
    var markAreaEnd by remember { mutableStateOf<Offset?>(null) }
    var saveInMoveMode = true


    // Trigger recovery mode only once
    LaunchedEffect(recoveryMode) {
        if (recoveryMode) {
            val success = examInfo.continueAlreadyStartedExam(textMeasurer,context)
            if (success) {
                examInfo.startBackUp(context)
                viewModel.changeQuestion(
                    qNr = 1,
                    newObjects = viewModel.objects.toList(),
                    canvasHeight = 2400.dp
                )}
        }
    }

    // Save scroll position when switching questions
    DisposableEffect(viewModel.currentQuestion.intValue) {
        val questionToSave = viewModel.currentQuestion.intValue // Capture the current question
        onDispose {
            val currentScroll = verticalScrollState.value
            viewModel.saveScrollPosition(
                questionNr = questionToSave, // Save for the captured question
                scrollValue = currentScroll
            )
        }
    }

    // Reset Mark Area when switching questions
    DisposableEffect(viewModel.currentQuestion.intValue) {
        onDispose {
            viewModel.mark = false
            isMoveMode = false;
            markAreaStart = null;
            markAreaEnd = null;
            saveInMoveMode = true;
        }
    }

    LaunchedEffect(viewModel.currentQuestion.intValue) {
        val savedScroll = viewModel.getScrollPosition(viewModel.currentQuestion.intValue)
        if (verticalScrollState.value != savedScroll) {
            verticalScrollState.scrollTo(savedScroll)
        }
    }
    LaunchedEffect(viewModel.objects) {
        val newHeight = calculateCanvasHeight(viewModel.objects, density)
        if (newHeight > canvasHeight) {
            canvasHeight = newHeight
            viewModel.updateCanvasHeight(newHeight)
        }
    }

    DisposableEffect(viewModel.copy.value) {
        onDispose {
            if (viewModel.copy.value && viewModel.elementIndexes.isNotEmpty()) {
                // move the marked area to the top-left corner to clarify copy was done
                val left = minOf(markAreaStart!!.x, markAreaEnd!!.x)
                val top = minOf(markAreaStart!!.y, markAreaEnd!!.y)
                val topLeft = Offset(left, top)

                viewModel.saveHistory()
                viewModel.copyObjects(topLeft, markAreaStart!!, markAreaEnd!!)

                markAreaStart = markAreaStart!! - topLeft
                markAreaEnd = markAreaEnd!! - topLeft
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    verticalScrollState,
                    enabled = isScrollMode
                ) // Enables vertical scrolling
                .height(canvasHeight)
            //.background(Color.White) --> Junyi

        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (!isScrollMode) {
                            Modifier
                                .pointerInput(isMarkAreaMode, isMoveMode) {
                                    if (isMarkAreaMode && !isMoveMode) {
                                        detectDragGestures(
                                            onDragStart = { start ->
                                                markAreaStart = start
                                                markAreaEnd = start
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                markAreaEnd = change.position
                                            },
                                            onDragEnd = {
                                                markAreaStart?.let {
                                                    markAreaEnd?.let { it1 ->
                                                        viewModel.findObjectsInsideArea(
                                                            it,
                                                            it1
                                                        )
                                                    }
                                                }
                                                isMoveMode = true;
                                                viewModel.copyModeAvailable = true
                                            }
                                        )
                                    } else if (isMoveMode) {
                                        detectDragGestures(
                                            onDragStart = {
                                                if (saveInMoveMode && !viewModel.copy.value) {
                                                    viewModel.saveHistory()
                                                    saveInMoveMode = false
                                                }
                                            },
                                            onDrag = { _, dragAmount ->
                                                markAreaStart = Offset(
                                                    x = markAreaStart!!.x + dragAmount.x,
                                                    y = markAreaStart!!.y + dragAmount.y
                                                );
                                                markAreaEnd = Offset(
                                                    x = markAreaEnd!!.x + dragAmount.x,
                                                    y = markAreaEnd!!.y + dragAmount.y
                                                );

                                                viewModel.moveObjects(dragAmount)
                                            }
                                        )
                                    } else {
                                        detectDragGestures(
                                            onDragStart = {
                                                if (!viewModel.textMode.value) viewModel.saveHistory()
                                            },
                                            onDrag = { change, dragAmount ->
                                                if (!viewModel.textMode.value) {
                                                    change.consume()
                                                    val start = change.position - dragAmount
                                                    val end = change.position

                                                    if (isInBounds(start, size) && isInBounds(
                                                            end,
                                                            size
                                                        )
                                                    ) {
                                                        val newLine = Line(
                                                            start = start,
                                                            end = end,
                                                            strokeWidth = if (viewModel.eraser) viewModel.eraserWidth else viewModel.strokeWidth,
                                                            color = if (viewModel.eraser) Color.White else Color.Black
                                                        ).apply {
                                                            if (viewModel.eraser) cap =
                                                                StrokeCap.Square
                                                        }

                                                        viewModel.addObject(newLine)
                                                        expandCanvasIfNeeded(
                                                            newLine,
                                                            density,
                                                            canvasHeight
                                                        ) {
                                                            canvasHeight = it
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                                .pointerInput(isMoveMode) {
                                    if (!isMoveMode) {
                                        detectTapGestures(
                                            onTap = { offset ->
                                                if (viewModel.textMode.value) {
                                                    val tappedTextBox =
                                                        findTappedTextBox(viewModel, offset)
                                                    if (tappedTextBox != null) {
                                                        viewModel.removeObject(tappedTextBox)
                                                        viewModel.saveHistory()
                                                        textOffset = tappedTextBox.position
                                                        textValue = tappedTextBox.text
                                                    } else if (textValue.isEmpty()) {
                                                        textOffset = offset
                                                    } else {
                                                        createTextBox(
                                                            viewModel,
                                                            textValue,
                                                            textOffset,
                                                            textMeasurer
                                                        )
                                                        textValue = ""
                                                        textOffset = Offset.Zero
                                                        viewModel.textMode.value = false
                                                        viewModel.eraser = false
                                                    }
                                                } else {
                                                    val tappedTextBox =
                                                        findTappedTextBox(viewModel, offset)
                                                    if (tappedTextBox == null) {
                                                        val dotLine = Line(
                                                            start = offset,
                                                            end = offset,
                                                            strokeWidth = if (viewModel.eraser) viewModel.eraserWidth else viewModel.strokeWidth,
                                                            color = if (viewModel.eraser) Color.White else Color.Black
                                                        ).apply {
                                                            if (viewModel.eraser) cap =
                                                                StrokeCap.Square
                                                        }

                                                        viewModel.saveHistory()
                                                        viewModel.addObject(dotLine)
                                                        expandCanvasIfNeeded(
                                                            dotLine,
                                                            density,
                                                            canvasHeight
                                                        ) {
                                                            canvasHeight = it
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                        } else Modifier
                    )
            ) {
                // 1. Base white
                drawRect(Color.White)

                // 2. Background (under strokes)
                when (viewModel.currentBackgroundType) {
                    BackgroundType.GRAPH -> drawGraphPaperPattern(size)
                    BackgroundType.LINED -> drawLinedPaperPattern(size)
                    BackgroundType.DOTTED -> drawDottedPattern(size)
                    else -> {}
                }

                // 3. User content
                viewModel.objects.forEach { it.draw(this) }

                if (isMarkAreaMode && markAreaStart != null && markAreaEnd != null) {
                    drawRect(
                        color = Color.Gray.copy(alpha = 0.3f),
                        topLeft = Offset(
                            x = minOf(markAreaStart!!.x, markAreaEnd!!.x),
                            y = minOf(markAreaStart!!.y, markAreaEnd!!.y)
                        ),
                        size = Size(
                            width = abs(markAreaEnd!!.x - markAreaStart!!.x),
                            height = abs(markAreaEnd!!.y - markAreaStart!!.y)
                        )
                    )
                } else if (!viewModel.mark) {
                    // reset start & end to initial 'null' values for the blue rectangle
                    // thus it will not be drawn next time tool is selected
                    isMoveMode = false;
                    markAreaStart = null;
                    markAreaEnd = null;
                }

                // 4. Redraw background on top to restore erased areas
                when (viewModel.currentBackgroundType) {
                    BackgroundType.GRAPH -> drawGraphPaperPattern(size)
                    BackgroundType.LINED -> drawLinedPaperPattern(size)
                    BackgroundType.DOTTED -> drawDottedPattern(size)
                    else -> {}
                }
            }
        }
//Junyi

        if (viewModel.textMode.value && textOffset != Offset.Zero) {
            var adjustedOffset = Offset(
                textOffset.x / density,
                (textOffset.y - verticalScrollState.value) / density
            )
            EditableTextField(
                initialText = textValue,
                offset = adjustedOffset,
                label = "Enter text",
                onTextChange = { textValue = it },
                onFocusLost = {
                    if (textValue.isNotEmpty()) {
                        val newTextBox = TextBox(
                            position = adjustedOffset,
                            textLayout = textMeasurer.measure(AnnotatedString(textValue)),
                            text = textValue
                        )
                        viewModel.addObject(newTextBox)
                        expandCanvasIfNeeded(newTextBox, density, canvasHeight) {
                            canvasHeight = it
                        }
                    }
                    textValue = ""
                    adjustedOffset = Offset.Zero
                    viewModel.textMode.value = false
                    viewModel.eraser = false
                }
            )
        }

        CustomScrollIndicator(scrollState = verticalScrollState)

        // Toggle between scroll and draw modes
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE3E2E9)) // Bakgrund på hela raden
        ) {
            Text(
                text = "Scroll",
                modifier = Modifier
                    .clickable { isScrollMode = true }
                    .padding(horizontal = if (isScrollMode) 20.dp else 12.dp, vertical = 8.dp) // <-- MER padding om aktiv
                    .background(
                        if (isScrollMode) Color(0xFF2070B1) else Color.Transparent,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isScrollMode) Color.White else Color.DarkGray
            )
            Text(
                text = "Draw",
                modifier = Modifier
                    .clickable { isScrollMode = false }
                    .padding(horizontal = if (!isScrollMode) 20.dp else 12.dp, vertical = 8.dp) // <-- MER padding om aktiv
                    .background(
                        if (!isScrollMode) Color(0xFF2070B1) else Color.Transparent,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (!isScrollMode) Color.White else Color.DarkGray
            )
        }
    }
}

@Composable
fun CustomScrollIndicator(scrollState: ScrollState) {
    // Calculate scroll thumb position and size
    val topBarHeight = 90f
    val adjustedHeight = 800f - topBarHeight
    // value between 0 and 1, how much space the scroll will take, can change it so its based on the current height, but not necessary rn i think
    val thumbHeightRatio = 0.1f
    // normalized scroll progress value between 0.0 and 1.0 that indicates how far its scrolled
    val scrollProgress = scrollState.value.toFloat() / max(scrollState.maxValue, 1)
    val thumbOffset = scrollProgress * (1f - thumbHeightRatio)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = topBarHeight.dp, end = 4.dp)
    ) {
        // Scrollbar thumb
        Box(
            modifier = Modifier
                .width(8.dp)
                .fillMaxHeight(thumbHeightRatio)
                .offset(y = (thumbOffset * adjustedHeight).dp)
                .background(Color(0xFF49546C))
                .align(Alignment.TopEnd) // Align the thumb to the top-right
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .absoluteOffset(x = offset.x.dp, y = offset.y.dp) //removed density, caused problems here.
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
    val measuredText = textMeasurer.measure(AnnotatedString(textValue), softWrap = true)
    val richTextState = RichTextState().apply {
        setMarkdown(textValue)
    }
    viewModel.addObject(
        TextBox(
            position = textOffset,
            text = textValue,
            textLayout = measuredText,
            richText = richTextState,
            richTextContent = textValue
        )
    )
}

private fun findTappedTextBox(viewModel: TentaViewModel, offset: Offset): TextBox? {
    return viewModel.objects
        .filterIsInstance<TextBox>()
        .find { textBox ->
            val topLeft = textBox.position
            val sizePx = textBox.textLayout.size
            val bottomRight = Offset(topLeft.x + sizePx.width, topLeft.y + sizePx.height)
            offset.x in topLeft.x..bottomRight.x && offset.y in topLeft.y..bottomRight.y
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
    val thresholdPx = 400f
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


