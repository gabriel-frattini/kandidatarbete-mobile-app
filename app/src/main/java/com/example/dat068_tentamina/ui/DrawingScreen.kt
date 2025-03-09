package com.example.dat068_tentamina.ui


import ExamInfo
import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatTextdirectionLToR
import androidx.compose.material.icons.automirrored.outlined.FormatTextdirectionRToL
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.ui.DrawGraphPaperBackground //Junyi
import com.example.dat068_tentamina.ui.DrawLinedPaperBackground //Junyi
import com.example.dat068_tentamina.ui.DrawDottedBackground //Junyi
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import com.example.dat068_tentamina.viewmodel.BackgroundType //Junyi
import com.example.dat068_tentamina.ui.BackgroundPicker //Junyi
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import kotlin.math.max

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

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(verticalScrollState, enabled = isScrollMode) // Enables vertical scrolling
                .height(canvasHeight)
                //.background(Color.White) --> Junyi

        ) {
            when (viewModel.backgroundType.value) {
                BackgroundType.BLANK -> {}
                BackgroundType.GRAPH -> DrawGraphPaperBackground(Modifier.fillMaxSize())
                BackgroundType.LINED -> DrawLinedPaperBackground(Modifier.fillMaxSize())
                BackgroundType.DOTTED -> DrawDottedBackground(Modifier.fillMaxSize())
            } //Junyi
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
                                        val tappedTextBox = findTappedTextBox(viewModel, offset)
                                        if (tappedTextBox != null) {
                                            // Edit the tapped TextBox
                                            viewModel.removeObject(tappedTextBox)
                                            viewModel.saveHistory()
                                            textOffset = tappedTextBox.position
                                            textValue = tappedTextBox.text
                                        } else if (textValue.isEmpty()) {
                                            // Enter text mode at clicked position
                                            textOffset = offset
                                        } else {
                                            // Create a new TextBox if there is text
                                            // TODO: (Gabbe) Create markdown textbox here?
                                            createTextBox(viewModel, textValue, textOffset, textMeasurer)
                                            textValue = ""
                                            textOffset = Offset.Zero
                                            viewModel.textMode.value = false
                                            viewModel.eraser = false
                                        }
                                    } else {
                                        val tappedTextBox = findTappedTextBox(viewModel, offset)
                                        if (tappedTextBox == null) {
                                            // Add a dot if no TextBox was tapped
                                            val dotLine = Line(
                                                start = offset,
                                                end = offset,
                                                strokeWidth = viewModel.strokeWidth,
                                            )
                                            if (viewModel.eraser) {
                                                dotLine.cap = StrokeCap.Square
                                                dotLine.color = Color.White
                                                dotLine.strokeWidth = viewModel.eraserWidth
                                            }
                                            viewModel.saveHistory()
                                            viewModel.addObject(dotLine)
                                            expandCanvasIfNeeded(dotLine, density, canvasHeight) {
                                                canvasHeight = it
                                            }
                                        }
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
        Button(
            onClick = {
                isScrollMode = !isScrollMode
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(60.dp)
        ) {
            Text(if (isScrollMode) "Enable Draw Mode" else "Enable Scroll Mode")
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
                .background(Color(0xFF2247FF))
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
    viewModel.addObject(
        TextBox(
            position = textOffset,
            text = textValue,
            textLayout = measuredText
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


val Blue400 = Color(0xFF4572E8)

val md_theme_light_primary = Color(0xFF2156CB)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFDBE1FF)
val md_theme_light_onPrimaryContainer = Color(0xFF00174B)
val md_theme_light_secondary = Color(0xFFB90063)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFFFD9E2)
val md_theme_light_onSecondaryContainer = Color(0xFF3E001D)
val md_theme_light_tertiary = Color(0xFF745470)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFD6F8)
val md_theme_light_onTertiaryContainer = Color(0xFF2B122B)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFEFBFF)
val md_theme_light_onBackground = Color(0xFF1B1B1F)
val md_theme_light_surface = Color(0xFFFEFBFF)
val md_theme_light_onSurface = Color(0xFF1B1B1F)
val md_theme_light_surfaceVariant = Color(0xFFE2E2EC)
val md_theme_light_onSurfaceVariant = Color(0xFF45464F)
val md_theme_light_outline = Color(0xFF757680)
val md_theme_light_inverseOnSurface = Color(0xFFF2F0F4)
val md_theme_light_inverseSurface = Color(0xFF303034)
val md_theme_light_inversePrimary = Color(0xFFB4C5FF)
val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF2156CB)
val md_theme_light_outlineVariant = Color(0xFFC5C6D0)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFFB4C5FF)
val md_theme_dark_onPrimary = Color(0xFF002A78)
val md_theme_dark_primaryContainer = Color(0xFF003EA8)
val md_theme_dark_onPrimaryContainer = Color(0xFFDBE1FF)
val md_theme_dark_secondary = Color(0xFFFFB1C8)
val md_theme_dark_onSecondary = Color(0xFF650033)
val md_theme_dark_secondaryContainer = Color(0xFF8E004A)
val md_theme_dark_onSecondaryContainer = Color(0xFFFFD9E2)
val md_theme_dark_tertiary = Color(0xFFE2BBDB)
val md_theme_dark_onTertiary = Color(0xFF422741)
val md_theme_dark_tertiaryContainer = Color(0xFF5A3D58)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFD6F8)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF1B1B1F)
val md_theme_dark_onBackground = Color(0xFFE4E2E6)
val md_theme_dark_surface = Color(0xFF1B1B1F)
val md_theme_dark_onSurface = Color(0xFFE4E2E6)
val md_theme_dark_surfaceVariant = Color(0xFF45464F)
val md_theme_dark_onSurfaceVariant = Color(0xFFC5C6D0)
val md_theme_dark_outline = Color(0xFF8F909A)
val md_theme_dark_inverseOnSurface = Color(0xFF1B1B1F)
val md_theme_dark_inverseSurface = Color(0xFFE4E2E6)
val md_theme_dark_inversePrimary = Color(0xFF2156CB)
val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFFB4C5FF)
val md_theme_dark_outlineVariant = Color(0xFF45464F)
val md_theme_dark_scrim = Color(0xFF000000)


val seed = Color(0xFF4572E8)


private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Composable
internal fun ComposeRichEditorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun RichTextStyleButton(
    onClick: () -> Unit,
    icon: ImageVector,
    tint: Color? = null,
    isSelected: Boolean = false,
) {
    IconButton(
        modifier = Modifier
            // Workaround to prevent the rich editor
            // from losing focus when clicking on the button
            // (Happens only on Desktop)
            .focusProperties { canFocus = false },
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        ),
    ) {
        Icon(
            icon,
            contentDescription = icon.name,
            tint = tint ?: LocalContentColor.current,
            modifier = Modifier
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape
                )
        )
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RichTextStyleRow(
    modifier: Modifier = Modifier,
    state: RichTextState,
) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        item {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Left,
                        )
                    )
                },
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Left,
                icon = Icons.AutoMirrored.Outlined.FormatTextdirectionLToR
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                },
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Center,
                icon = Icons.Outlined.FormatAlignCenter
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Right
                        )
                    )
                },
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Right,
                icon = Icons.AutoMirrored.Outlined.FormatTextdirectionRToL
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
                icon = Icons.Outlined.FormatBold
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontStyle == FontStyle.Italic,
                icon = Icons.Outlined.FormatItalic
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                },
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
                icon = Icons.Outlined.FormatUnderlined
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                },
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
                icon = Icons.Outlined.FormatStrikethrough
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 28.sp
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontSize == 28.sp,
                icon = Icons.Outlined.FormatSize
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            color = Color.Red
                        )
                    )
                },
                isSelected = state.currentSpanStyle.color == Color.Red,
                icon = Icons.Outlined.Circle,
                tint = Color.Red
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            background = Color.Yellow
                        )
                    )
                },
                isSelected = state.currentSpanStyle.background == Color.Yellow,
                icon = Icons.Outlined.Circle,
                tint = Color.Yellow
            )
        }

        item {
            Box(
                Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF393B3D))
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleUnorderedList()
                },
                isSelected = state.isUnorderedList,
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleOrderedList()
                },
                isSelected = state.isOrderedList,
                icon = Icons.Outlined.FormatListNumbered,
            )
        }

        item {
            Box(
                Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF393B3D))
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleCodeSpan()
                },
                isSelected = state.isCodeSpan,
                icon = Icons.Outlined.Code,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichEditorScreen(viewModel: TentaViewModel, navigateBack: () -> Unit) {
        val richTextState = rememberRichTextState()
    val textMeasurer = rememberTextMeasurer()

    // Sync initial content when ViewModel changes (e.g., on question change)
    LaunchedEffect(viewModel.richTextContent.value) {
        if (richTextState.toMarkdown() != viewModel.richTextContent.value) {
            richTextState.setMarkdown(viewModel.richTextContent.value)
        }
    }

    // Save changes from the editor to ViewModel
    LaunchedEffect(richTextState.annotatedString) {
        val markdown = richTextState.toMarkdown()
        if (viewModel.richTextContent.value != markdown) {
            viewModel.updateRichText(markdown)

            // Save as a CanvasObject (TextBox) just like in DrawingScreen
            val measuredText = textMeasurer.measure(AnnotatedString(markdown))
            val textBox = TextBox(
                position = Offset(50f, 50f), // Default position, can be adjusted
                text = markdown,
                textLayout = measuredText
            )
            viewModel.addObject(textBox)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        RichTextStyleRow(
            modifier = Modifier.fillMaxWidth(),
            state = richTextState
        )

        BasicRichTextEditor(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            state = richTextState,
        )
    }
}
