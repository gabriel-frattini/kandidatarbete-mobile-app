package com.example.dat068_tentamina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp


import ExamInfo

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
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Redo
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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.TopAppBarDefaults
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar
import java.util.TimeZone
import androidx.compose.material3.Surface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Modifier
import com.example.dat068_tentamina.viewmodel.BackgroundType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Overlay(viewModel: TentaViewModel, examInfo: ExamInfo, recoveryMode: Boolean, activity: MainActivity, signout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val questionNumbers = viewModel.questions.keys.sorted()
    val tabs = questionNumbers.map { "Question $it" }



    var examOverDialogVisible by remember { mutableStateOf(false) }

    var show15MinWarning by remember { mutableStateOf(false) }


    LaunchedEffect(examInfo.getExamEndTime()) {

        val examEndTimeStr = examInfo.getExamEndTime()
        val timeParts = examEndTimeStr.split(":")
        if (timeParts.size == 2) {
            val examEndHour = timeParts[0].toIntOrNull() ?: 0
            val examEndMinute = timeParts[1].toIntOrNull() ?: 0

            val examEndCalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm")).apply {
                set(Calendar.HOUR_OF_DAY, examEndHour)
                set(Calendar.MINUTE, examEndMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            examEndCalendar.add(Calendar.MINUTE, -15)
            val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"))
            val delayMillis = examEndCalendar.timeInMillis - now.timeInMillis
            if (delayMillis > 0) {
                delay(delayMillis)
            }
        }
        //kommentera bort ifall du använder tid. Antigen från tp eller från set metoden i main.
        //show15MinWarning = true
        //delay(5000L)
        //show15MinWarning = false
    }


    if (show15MinWarning) {
        androidx.compose.ui.window.Popup(alignment = Alignment.Center) {
            Surface(
                color = Color.Yellow,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Only 15 minutes remaining!",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Black
                )
            }
        }
    }


    LaunchedEffect(examInfo.getExamEndTime()) {

        val timeParts = examInfo.getExamEndTime().split(":")
        if (timeParts.size == 2) {
            val examEndHour = timeParts[0].toIntOrNull() ?: 0
            val examEndMinute = timeParts[1].toIntOrNull() ?: 0


            val examEndCalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm")).apply {
                set(Calendar.HOUR_OF_DAY, examEndHour)
                set(Calendar.MINUTE, examEndMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"))
            val delayMillis = examEndCalendar.timeInMillis - now.timeInMillis
            if (delayMillis > 0) {
                delay(delayMillis)
            }
        }

        //examOverDialogVisible = true kommentera bort ifall du använder tid. Antigen från tp eller från set metoden i main.
    }


    if (examOverDialogVisible) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Exam Ended") },
            text = { Text("The exam has ended. Please submit your exam.") },
            confirmButton = {
                Button(onClick = {
                    submitExam(viewModel, examInfo, activity, signout)
                    examOverDialogVisible = false
                }) {
                    Text("Submit")
                }
            }
        )
    }




    val tabWidth = 150.dp
    val selectedTabColor = Color(0xFFFAF8FF)
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
                                modifier = Modifier.background(Color(0xFFFAF8FF), shape = RoundedCornerShape(12.dp))                            ) {
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
                                            .width(tabWidth)
                                            .background(
                                                color = if (selectedTabIndex == index) selectedTabColor else Color(0xFFDFDFDF),
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
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            color = if (selectedTabIndex == index) Color.Black else Color.Black.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
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



//Automatically submit the exam at a certain time.

fun submitExam(viewModel: TentaViewModel, examInfo: ExamInfo, activity: MainActivity, signout: () -> Unit) {
    //val answers = viewModel.getAnswers()
    //val pdfFile = PdfConverter.createPdfFromAnswers(answers, 2560, 1700, activity)

    //examInfo.sendPdf(pdfFile)  // Uncomment to sen the exam

    Toast.makeText(activity, "Exam has been submitted!", Toast.LENGTH_LONG).show()

    signout()
}

@Composable
fun ExamTimer(examInfo: ExamInfo, modifier: Modifier = Modifier) {

    var remainingTime by remember { mutableStateOf(0L) }

    LaunchedEffect(examInfo.getExamDate(), examInfo.getExamEndTime()) {

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Europe/Stockholm")
        }

        val examDate = examInfo.getExamDate()
        val examEndTime = examInfo.getExamEndTime()
        val examEndDateTimeStr = "$examDate $examEndTime"


        val examEndDate: Date? = try {
            sdf.parse(examEndDateTimeStr)
        } catch (e: Exception) {
            null
        }

        while (true) {
            val now = System.currentTimeMillis()
            remainingTime = (examEndDate?.time ?: now) - now
            if (remainingTime < 0L) {
                remainingTime = 0L
                break
            }
            delay(1000L)
        }
    }

    val seconds = (remainingTime / 1000) % 60
    val minutes = (remainingTime / (1000 * 60)) % 60
    val hours = remainingTime / (1000 * 60 * 60)
    val timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(2.dp, Color(0xFF071D4F))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = timeStr, color = Color(0xFF071D4F))
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

            // Mark Area
            IconButton(onClick = {
                viewModel.mark = !viewModel.mark
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.highlight_alt),
                    contentDescription = "highlight_alt",
                    modifier = Modifier.size(30.dp)
                )
            }

            // Copy Area
            IconButton(
                enabled = viewModel.copyModeAvailable,
                onClick = {
                    viewModel.copy()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.content_copy),
                    contentDescription = "content_copy",
                    modifier = Modifier.size(30.dp)
                )
            }

            // Delete All
            IconButton(onClick = {
                viewModel.deleteAll()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = "delete",
                    modifier = Modifier.size(30.dp)
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
                    Icons.Filled.Undo,
                    contentDescription = "Undo"
                )
            }
            // Redo button
            IconButton(onClick = {
                viewModel.redo()
            }) {
                Icon(
                    Icons.Filled.Redo,
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

    Column(
        modifier = Modifier
            .background(Color(0xFFF8F9FF))
            .fillMaxHeight()
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
                    .padding(start = 40.dp, top = 16.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFAF8FF))
                    .padding(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Black
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Question Overview:",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 40.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                viewModel.questions.keys.forEach { questionNumber ->
                    val isAnswered = answeredQuestions.contains(questionNumber)
                    val textColor = if (isAnswered) Color(0xFF071D4F) else Color(0xFF7E7E7E)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(60.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "Question $questionNumber",
                                    color = textColor,
                                    fontSize = 20.sp
                                )
                            }

                            Box(
                                modifier = Modifier.padding(start = 8.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (isAnswered) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Answered",
                                        tint = Color(0xFF071D4F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

        ExamTimer(
            examInfo = examInfo,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
                .width(200.dp)
                .height(60.dp)
        )

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

    // Submit Dialog submit exam
    if (submitDialog) {
        AlertDialog(
            onDismissRequest = { submitDialog = false },
            title = { Text("Submit Exam") },
            text = { Text("Are you sure you want to submit the exam?") },
            confirmButton = {
                Button(onClick = {
                    val answersWithBackground = viewModel.getAnswers().mapValues { (questionId, objects) ->
                        val background = viewModel.backgroundTypes[questionId] ?: BackgroundType.BLANK
                        objects to background
                    }
                    val pdfFile = PdfConverter.createPdfFromAnswers(answersWithBackground, 2560, 1700, activity)

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
    //Dialog för studentinformationen
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
}