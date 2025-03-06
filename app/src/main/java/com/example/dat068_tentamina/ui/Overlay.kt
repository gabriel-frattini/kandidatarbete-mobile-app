package com.example.dat068_tentamina.ui

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
import kotlin.math.sign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Overlay(viewModel: TentaViewModel, examInfo: ExamInfo, recoveryMode : Boolean , activity: MainActivity, signout: () -> Unit) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                //The content of the menu
                MenuScreen(modifier = Modifier,viewModel, activity, examInfo, signout)
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Question ${viewModel.currentQuestion.intValue} ") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        } }) {
                            Icon(Icons.Filled.Menu, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            // (Gabbe) Button to create textbox.
                            viewModel.textMode.value = true
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.text),
                                contentDescription = "text",
                                modifier = Modifier.size(25.dp)
                            )
                        }
                        IconButton(onClick = {
                        }) {
                            SizePicker(viewModel, "eraser")
                        }
                        IconButton(onClick = {}) {
                            SizePicker(viewModel)
                        }

                        IconButton(onClick = {}) {
                            BackgroundPicker(viewModel) //Junyi
                        }
                        // TODO: (Gabbe) Button to undo changes, add another button below to redo changes
                        IconButton(onClick = {
                            viewModel.undo()
                        }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                        IconButton(onClick = {
                            viewModel.redo()
                        }) {
                            Icon(
                                Icons.Filled.ArrowForward,
                                contentDescription = "Localized description"
                            )
                        }
                        // TODO: (Gabbe) Another button for drawing geometric shapes
                    }
                )
            },
        )
        { contentPadding ->
            ExamScreen(modifier = Modifier.padding(contentPadding), examInfo, viewModel, recoveryMode)
        }
    }
}
@Composable
fun ExamScreen(modifier: Modifier = Modifier, examInfo: ExamInfo, viewModel: TentaViewModel, recoveryMode: Boolean ) {
    Column {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxSize()) {
            DrawingScreen(viewModel, examInfo , recoveryMode )
        }
    }
}
@Composable
fun MenuScreen(modifier: Modifier = Modifier, viewModel: TentaViewModel, activity: MainActivity, examInfo: ExamInfo, signout: () -> Unit) {
    val scrollState = rememberScrollState()
    var submitDialog by remember { mutableStateOf(false)}
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        // TODO: (Gabbe) Use `AlertDialog` to display error messages if something goes wrong
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {Text("Student Information")},
            text = {Text("Course: ${examInfo.course}\nAnonymous Code: ${examInfo.user} \nBirth ID: ${examInfo.personalID}")},
            confirmButton = {},
            dismissButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("Back")
                }
            }
        )

    }

    if (submitDialog) {
        AlertDialog(
            onDismissRequest = { submitDialog = false },
            title = { Text("Submit exam") },
            text = { Text("Are you sure you want to submit the exam?") },
            confirmButton = {
                Button(onClick = {
                    val answers = viewModel.getAnswers()
                    val pdfFile = PdfConverter.createPdfFromAnswers(answers, 2560, 1700, activity) // Adjust dimensions as needed
                    examInfo.sendPdf(pdfFile)
                    signout()
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

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .background(Color.LightGray)
            .fillMaxHeight()
            .verticalScroll(scrollState)
            .requiredWidth(500.dp)
    ) {
        // Information card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(100.dp)
                .align(alignment = Alignment.CenterHorizontally)
        ) {
            IconButton(
                onClick = { showInfoDialog = true},
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize()
                    .align(alignment = Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Filled.Info, contentDescription = "Information",
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                )
            }
        }

        // List of questions
        for ((key, value) in viewModel.questions) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(100.dp)
                    .align(alignment = Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = {
                        viewModel.changeQuestion(
                            qNr = key,
                            newObjects = viewModel.objects.toList(),
                            canvasHeight = 2400.dp
                        )
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                        .align(alignment = Alignment.CenterHorizontally)

                ) {
                    Text(
                        text = "Question $key",
                        modifier = Modifier
                            .padding(10.dp)
                            .align(alignment = Alignment.CenterVertically)
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(100.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .padding(top = 10.dp)

        ) {
            Button(
                onClick = {
                    submitDialog = true
                },

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),

                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize()
                    .align(alignment = Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Submit",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(alignment = Alignment.CenterVertically)
                )
            }
        }
    }

}


