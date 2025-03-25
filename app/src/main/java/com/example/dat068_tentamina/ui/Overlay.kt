package com.example.dat068_tentamina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.clickable
import ExamInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.R
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.dat068_tentamina.MainActivity
import PdfConverter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.TopAppBarDefaults
import kotlin.math.sign
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Overlay(viewModel: TentaViewModel, examInfo: ExamInfo, recoveryMode: Boolean, activity: MainActivity, signout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val questionNumbers = viewModel.questions.keys.sorted()
    val tabs = questionNumbers.map { "Question $it" }
    val tabWidth = if (tabs.size > 8) 140.dp else 120.dp // Ökar bredden för att del av 8:e fliken ska synas
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                MenuScreen(modifier = Modifier, viewModel, activity, examInfo, signout)
            }
        },
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.apply { if (isClosed) open() else close() }
                                    }
                                },
                                modifier = Modifier.background(Color.White, shape = RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Filled.Menu, contentDescription = null)
                            }
                        },
                        title = {
                            Row(
                                modifier = Modifier
                                    .offset(y = 10.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp, vertical = 4.dp)
                                            .width(tabWidth) // Dynamisk bredd
                                            .background(
                                                color = if (selectedTabIndex == index) Color.White else Color(0xFFDFDFDF),
                                                shape = RoundedCornerShape(
                                                    topStart = 6.dp, topEnd = 6.dp,
                                                    bottomStart = 0.dp, bottomEnd = 0.dp
                                                )
                                            )
                                            .clickable {
                                                selectedTabIndex = index
                                                viewModel.changeQuestion(
                                                    qNr = questionNumbers[index],
                                                    newObjects = viewModel.objects.toList(),
                                                    canvasHeight = 2400.dp
                                                )
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = title,
                                            color = if (selectedTabIndex == index) Color.Black else Color.Black.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = Color(0xFF49546C)
                        )
                    )
                }
            },
        )
        { contentPadding ->
            ExamScreen(modifier = Modifier.padding(contentPadding), examInfo, viewModel, recoveryMode)
        }
    }
}


@Composable
fun ExamScreen(
    modifier: Modifier = Modifier,
    examInfo: ExamInfo,
    viewModel: TentaViewModel,
    recoveryMode: Boolean
) {
    val showRichEditor = examInfo.questions[viewModel.currentQuestion.intValue - 1].type == "text"
    if (showRichEditor) {
        RichEditorScreen(viewModel, examInfo, recoveryMode)
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            DrawingScreen(
                viewModel,
                examInfo,
                recoveryMode,
            )
            ExampageToolbar(
                viewModel,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(13.dp)
            )
        }
    }
}


@Composable
fun ExampageToolbar(
    viewModel: TentaViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Text mode button
            IconButton(onClick = {
                viewModel.textMode.value = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.text),
                    contentDescription = "Text Mode",
                    modifier = Modifier.size(25.dp)
                )
            }

            // Eraser size picker
            IconButton(onClick = {}) {
                SizePicker(viewModel, "eraser")
            }

            // Pen size picker
            IconButton(onClick = {}) {
                SizePicker(viewModel)
            }

            IconButton(onClick = {}) {
                BackgroundPicker(viewModel) //Junyi
            }

            // Undo button
            IconButton(onClick = {
                viewModel.undo()
            }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Undo"
                )
            }
            // Redo button
            IconButton(onClick = {
                viewModel.redo()
            }) {
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = "Redo"
                )
            }
        }
    }
}

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    viewModel: TentaViewModel,
    activity: MainActivity,
    examInfo: ExamInfo,
    signout: () -> Unit
) {
    val scrollState = rememberScrollState()
    var submitDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val answeredQuestions by viewModel.answeredQuestions

    // Student Information Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Student Information") },
            text = {
                Text("Course: ${examInfo.course}\nAnonymous Code: ${examInfo.user} \nBirth ID: ${examInfo.personalID}")
            },
            confirmButton = {},
            dismissButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("Back")
                }
            }
        )
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .background(Color(0xFFF8F9FF))
            .fillMaxHeight()
            .verticalScroll(scrollState)
            .requiredWidth(400.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFFBEC6D9))
        ) {
            IconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 12.dp)
                    .size(50.dp)
            ) {
                Icon(
                    Icons.Filled.AccountCircle, contentDescription = "Profile",
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Text(
            text = "Question Overview:",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .padding(top = 30.dp, bottom = 8.dp)
                .align(Alignment.CenterHorizontally)
        )

        viewModel.questions.keys.forEach { questionNumber ->
            val isAnswered = answeredQuestions.contains(questionNumber)
            val textColor = if (isAnswered) Color(0xFF071D4F) else Color(0xFF9E9E9E)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(60.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Question $questionNumber",
                            color = textColor,
                            fontSize = 20.sp
                        )
                    }
                    if (isAnswered) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Answered",
                            tint = Color(0xFF071D4F),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { submitDialog = true },
            modifier = Modifier
                .padding(20.dp)
                .width(200.dp)
                .height(60.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF071D4F),
                disabledContentColor = Color.LightGray
            ),
            border = BorderStroke(2.dp, Color(0xFF071D4F))
        ) {
            Text(
                text = "Submit Exam",
                color = Color(0xFF071D4F),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }

    // Submit Dialog for confirmation
    if (submitDialog) {
        AlertDialog(
            onDismissRequest = { submitDialog = false },
            title = { Text("Submit Exam") },
            text = { Text("Are you sure you want to submit the exam?") },
            confirmButton = {
                Button(onClick = {
                    val answers = viewModel.getAnswers()
                    val pdfFile = PdfConverter.createPdfFromAnswers(answers, 2560, 1700, activity)
                    examInfo.sendPdf(pdfFile)
                    signout()
                    submitDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { submitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}