package com.example.dat068_tentamina.ui

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
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import java.time.LocalDate
import ExamInfo
import androidx.compose.ui.platform.LocalContext

@Composable
fun Login(examInfo: ExamInfo,onNavigateToExam: () -> Unit) {
    val examId = remember { mutableStateOf(TextFieldValue("")) }
    val anonymousCode = remember { mutableStateOf(TextFieldValue("")) }
    var context = LocalContext.current


    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(Color.LightGray)
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
            text = "Please enter the following information and check in to the exam",
            fontSize = 50.sp,
            lineHeight = 80.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp)
                .align(alignment = Alignment.CenterHorizontally)
        )
        Row (
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
        {
            val maxCharExamId = 6
            OutlinedTextField(
                value = examId.value,
                onValueChange = { if (it.text.length <= maxCharExamId) examId.value = it },
                label = { Text("Exam id") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                modifier = Modifier
                    .padding(20.dp)
            )
            val maxCharAnonymousCode = 6
            OutlinedTextField(
                value = anonymousCode.value,
                onValueChange = {
                    if (it.text.length <= maxCharAnonymousCode) anonymousCode.value = it
                },
                label = { Text("Anonymous Code") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                modifier = Modifier
                    .padding(20.dp)
            )
        }
        ElevatedButton(
            onClick = {
                examInfo.fetchData(courseCode = examId.component1().text, anonymousCode = anonymousCode.component1().text)
                onNavigateToExam()
                examInfo.startBackUp(context)
            },
            colors = ButtonColors(Color.DarkGray, Color.White, Color.LightGray, Color.LightGray),
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(10.dp)
                .requiredHeight(75.dp)
                .requiredWidth(250.dp)
        ) {
            Text("Check in", fontSize = 25.sp)
        }
        ElevatedButton(
            onClick = {
                // TODO: (Gabbe) Button for recovery mode, do we open a modal here to ask proctor to verify with code?
                if((examInfo.verifyBackupCredentials(exId = examId.component1().text, aCode = anonymousCode.component1().text , context = context)))
                {
                    examInfo.fetchData(courseCode = examId.component1().text, anonymousCode = anonymousCode.component1().text)
                    examInfo.enableRecoveryMode()
                    onNavigateToExam()
                }

            } ,
            colors = ButtonColors(Color.LightGray, Color.White, Color.LightGray, Color.LightGray),
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
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
                .align(alignment = Alignment.CenterHorizontally)
        )
    }
}