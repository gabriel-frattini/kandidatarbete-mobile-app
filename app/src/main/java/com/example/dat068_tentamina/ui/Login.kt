package com.example.dat068_tentamina.ui

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable


fun Login(examInfo: ExamInfo, onNavigateToExam: () -> Unit) {
    val examId = remember { mutableStateOf(TextFieldValue("test_tenta")) }
    val anonymousCode = remember { mutableStateOf(TextFieldValue("TEST_TENTA-7615-DUT")) }
    val context = LocalContext.current

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
                .align(Alignment.End)
        )
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
            val maxCharExamId = 6
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

            val maxCharAnonymousCode = 15
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
                    anonymousCode = anonymousCode.value.text
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
            onClick = {
                if (examInfo.verifyBackupCredentials(
                        exId = examId.component1().text,
                        aCode = anonymousCode.component1().text,
                        context = context
                    )
                ) {
                    examInfo.fetchData(
                        courseCode = examId.component1().text,
                        anonymousCode = anonymousCode.component1().text
                    )
                    examInfo.enableRecoveryMode()
                    onNavigateToExam()
                }
            },
            colors = ButtonColors(Color.White, Color(0xFF30436E), Color.LightGray, Color.LightGray),
            border = BorderStroke(2.dp, Color(0xFF30436E)),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
                .requiredHeight(75.dp)
                .requiredWidth(250.dp)
        ) {
            Text("Recover exam", fontSize = 25.sp)
        }
        Image(
            painter = painterResource(id = R.drawable.chalmers_logo),
            contentDescription = "The Chalmers logo",
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
                .align(Alignment.CenterHorizontally)
        )
    }
}
