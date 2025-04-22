package com.example.dat068_tentamina.ui

import com.example.dat068_tentamina.ui.CustomAlertDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dat068_tentamina.R
import java.time.LocalDate
import ExamInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(examInfo: ExamInfo, onNavigateToExam: () -> Unit) {
    val isRecoveryMode = remember { mutableStateOf(false) }

    if (isRecoveryMode.value) {
        RecoveryView(onBackToLogin = { isRecoveryMode.value = false }, onNavigateToExam = onNavigateToExam, examInfo = examInfo)
    } else {
        LoginView(examInfo = examInfo, onNavigateToExam = onNavigateToExam, onNavigateToRecovery = { isRecoveryMode.value = true })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(examInfo: ExamInfo, onNavigateToExam: () -> Unit, onNavigateToRecovery: () -> Unit) {
    val examId = remember { mutableStateOf(TextFieldValue("")) }
    val anonymousCode = remember { mutableStateOf(TextFieldValue("")) }
    val showAlertDialog = remember { mutableStateOf(false) }
    val errorTitle = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // setOnError callback that takes a lambda function with a status parameter
        examInfo.setOnError { status ->
            if (status == 404) {
                errorTitle.value = "Invalid course code or anonymous code"
                errorMessage.value = "Double check your course code and anonymous code"
            } else if (status == 409) {
                errorTitle.value = "Exam already submitted"
                errorMessage.value = "You have already submitted this exam"
            } else {
                errorTitle.value = "Unexpected error"
                errorMessage.value = "An unexpected error occurred. Please contact administratator."
            }
            showAlertDialog.value = true
        }
    }

Box(

    modifier = Modifier
        .fillMaxSize()
        .background(
            Brush.linearGradient(
                colors = listOf(Color(0xFF49546C),Color(0xFFBEC6D9), Color.White),
                start = Offset(0f, 0f),
                end = Offset(2000f, 2000f)
            )
        )
) {
    Text(
        text = LocalDate.now().toString(),
        fontSize = 25.sp,
        lineHeight = 25.sp,
        textAlign = TextAlign.Right,
        modifier = Modifier
            .padding(top = 20.dp, end = 20.dp)
            .align(Alignment.TopEnd)
    )
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {

        Text(
            text = "Exam Check-in",
            fontSize = 64.sp,
            lineHeight = 80.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF071D4F),
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Please enter the following information:",
            fontSize = 25.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF30436E),
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally)
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val maxCharExamId = 15
            OutlinedTextField(
                value = examId.value,
                onValueChange = { if (it.text.length <= maxCharExamId) examId.value = it },
                label = { Text("Exam id") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                modifier = Modifier.padding(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray
                )
            )

            val maxCharAnonymousCode = 30
            OutlinedTextField(
                value = anonymousCode.value,
                onValueChange = {
                    if (it.text.length <= maxCharAnonymousCode) anonymousCode.value = it
                },
                label = { Text("Anonymous Code") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                modifier = Modifier.padding(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray
                )
            )
        }
        OutlinedButton(
            onClick = {
                // Endast trigga fetchData, callbacken i MainActivity hanterar resten
                examInfo.fetchData(
                    courseCode = examId.value.text,
                    anonymousCode = anonymousCode.value.text,
                    onSuccess = {
                        examInfo.startBackUp(context)
                    },
                )
            },
            colors = ButtonColors(Color(0xFF49546C), Color.White, Color.LightGray, Color.LightGray),
            border = BorderStroke(2.dp, Color(0xFF071D4F)),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
                .requiredHeight(75.dp)
                .requiredWidth(250.dp)

        ) {
            Text("Check in", fontSize = 25.sp)
        }
        ElevatedButton(
            onClick = onNavigateToRecovery,
            colors = ButtonColors(Color.White, Color(0xFF30436E), Color.LightGray, Color.LightGray),
            border = BorderStroke(2.dp, Color(0xFF30436E)),
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(10.dp)
                .requiredHeight(75.dp)
                .requiredWidth(250.dp)
        ) {
            Text("Recover exam", fontSize = 25.sp)
        }
    }
    Image(
        painter = painterResource(id = R.drawable.chalmers_logo),
        contentDescription = "The Chalmers logo",
        modifier = Modifier
            .size(300.dp)
            .align(Alignment.BottomEnd)
            .padding(20.dp)
    )
}

CustomAlertDialog(
    showDialog = showAlertDialog.value,
    title = errorTitle.value,
    message = errorMessage.value,
    onConfirm = {
        showAlertDialog.value = false

    },
    onDismissRequest = { showAlertDialog.value = false }
)
}

@Composable
fun RecoveryView(onBackToLogin: () -> Unit, onNavigateToExam: () -> Unit, examInfo: ExamInfo) {
    val examId = remember { mutableStateOf(TextFieldValue("")) }
    val anonymousCode = remember { mutableStateOf(TextFieldValue("")) }
    val recoveryCode = remember { mutableStateOf(TextFieldValue("")) }
    val errorMessage = remember { mutableStateOf("") }
    val errorTitle = remember { mutableStateOf("") }
    val showAlertDialog = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        examInfo.setOnError { status ->
            if (status == 400) {
                errorTitle.value = "Invalid recovery code"
                errorMessage.value = "Double check that you are using the correct recovery code for this exam"
            } else if (status == 404) {
                errorTitle.value = "Invalid course code or anonymous code"
                errorMessage.value = "Double check your course code and anonymous code"
            } else if (status == 409) {
                errorTitle.value = "Exam already submitted"
                errorMessage.value = "You have already submitted this exam"
            } else {
                errorTitle.value = "Unexpected error"
                errorMessage.value = "An unexpected error occurred. Please contact administratator."
            }
            showAlertDialog.value = true
        }
    }
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(Color(0xFFBEC6D9))
            .fillMaxSize()
    ) {
        Text(
            text = LocalDate.now().toString(),
            fontSize = 25.sp,
            lineHeight = 25.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .padding(20.dp)
                .align(alignment = Alignment.End)
        )
        Text(
            text = "Exam Recovery",
            fontSize = 64.sp,
            lineHeight = 80.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF071D4F),
            modifier = Modifier
                .padding(16.dp)
                .align(alignment = Alignment.CenterHorizontally)
        )
        Text(
            text = "Please enter the following information:",
            fontSize = 25.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF30436E),
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(alignment = Alignment.CenterHorizontally)
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val maxCharExamId = 15
            OutlinedTextField(
                value = examId.value,
                onValueChange = { if (it.text.length <= maxCharExamId) examId.value = it },
                label = { Text("Exam id") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                modifier = Modifier.padding(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray
                )
            )

            val maxCharAnonymousCode = 30
            OutlinedTextField(
                value = anonymousCode.value,
                onValueChange = {
                    if (it.text.length <= maxCharAnonymousCode) anonymousCode.value = it
                },
                label = { Text("Anonymous Code") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                modifier = Modifier.padding(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray
                )
            )
            OutlinedTextField(
                value = recoveryCode.value,
                onValueChange = {
                    if (it.text.length <= maxCharAnonymousCode) recoveryCode.value = it
                },
                label = { Text("Recovery Code") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                modifier = Modifier.padding(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray
                )
            )
        }
        OutlinedButton(
            onClick = {
                    examInfo.verifyRecoveryCode(
                    courseCode = examId.value.text,
                    recoveryCode = recoveryCode.value.text,
                    onSuccess = {
                         examInfo.setOnError {
                             errorMessage.value = "You have already submitted this exam"
                             showAlertDialog.value = true
                         }
                         examInfo.fetchData(
                            courseCode = examId.value.text,
                            anonymousCode = anonymousCode.value.text,
                            onSuccess = {
                                examInfo.enableRecoveryMode()
                            },
                        )
                    },
                )

            },
            colors = ButtonColors(Color(0xFF49546C), Color.White, Color.LightGray, Color.LightGray),
            border = BorderStroke(2.dp, Color(0xFF071D4F)),
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(10.dp)
                .requiredHeight(75.dp)
                .requiredWidth(250.dp)
        ) {
            Text("Recover", fontSize = 25.sp)
        }
        ElevatedButton(
            onClick = onBackToLogin,
            colors = ButtonColors(Color.White, Color(0xFF30436E), Color.LightGray, Color.LightGray),
            border = BorderStroke(2.dp, Color(0xFF30436E)),
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(10.dp)
                .requiredHeight(75.dp)
                .requiredWidth(250.dp)
        ) {
            Text("Back to login", fontSize = 25.sp)
        }
        Image(
            painter = painterResource(id = R.drawable.chalmers_logo),
            contentDescription = "The Chalmers logo",
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
                .align(alignment = Alignment.CenterHorizontally)
        )
    }

    CustomAlertDialog(
        showDialog = showAlertDialog.value,
        title = errorTitle.value,
        message = errorMessage.value,
        onConfirm = {
            showAlertDialog.value = false

        },
        onDismissRequest = { showAlertDialog.value = false }
    )
}
