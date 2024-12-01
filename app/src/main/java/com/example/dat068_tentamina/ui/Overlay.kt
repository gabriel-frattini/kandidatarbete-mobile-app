package com.example.dat068_tentamina.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.R
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.dat068_tentamina.model.CanvasObject
import java.io.File

import androidx.compose.ui.platform.LocalContext
import PdfConverter
import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.dat068_tentamina.MainActivity
import com.example.dat068_tentamina.utilities.ServerHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Overlay(viewModel: TentaViewModel, activity: MainActivity) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                //The content of the menu
                MenuScreen(modifier = Modifier,viewModel, activity)

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
                        IconButton(onClick = {
                            viewModel.pop()
                        }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                        IconButton(onClick = { /* doSomething() */ }) {
                            Icon(
                                Icons.Filled.ArrowForward,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                )
            },
        )
        { contentPadding ->
            ExamScreen(modifier = Modifier.padding(contentPadding), viewModel)
        }
    }
}
@Composable
fun ExamScreen(modifier: Modifier = Modifier, viewModel: TentaViewModel ) {
    Column {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxSize()) {
            DrawingScreen(viewModel)
        }
    }
}
@Composable
fun MenuScreen(modifier: Modifier = Modifier, viewModel: TentaViewModel, activity: MainActivity) {
    val scrollState = rememberScrollState()
    var showDialog by remember { mutableStateOf(false)}

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Submit exam") },
            text = { Text("Are you sure you want to submit the exam?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveHistory()
                    val answers = viewModel.getAnswers()
                    val pdfFile = PdfConverter.createPdfFromAnswers(answers, 1920, 1920, activity) // Adjust dimensions as needed
                    ServerHandler.sendPdfToServer(pdfFile, "Math 101", "student_username")
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
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
                onClick = { println("Hejsan") /*TODO: An actual information page with user info and user guide???*/ },
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
                        println("Switched to question $key")
                        viewModel.changeQuestion(key)
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
                    showDialog = true
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


