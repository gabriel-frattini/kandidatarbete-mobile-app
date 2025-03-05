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
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import java.time.LocalDate
import ExamInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(examInfo: ExamInfo,onNavigateToExam: () -> Unit) {
    val examId = remember { mutableStateOf(TextFieldValue("")) }
    val anonymousCode = remember { mutableStateOf(TextFieldValue("")) }
    var context = LocalContext.current

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
            text = "Exam Check-in",
            fontSize = 64.sp,
            lineHeight = 80.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold, // Gör texten fetstil
            color = Color(0xFF071D4F),
            modifier = Modifier
                .padding(16.dp)
                .align(alignment = Alignment.CenterHorizontally)
        )
        // Underrubrik
        Text(
            text = "Please enter the following information:",
            fontSize = 25.sp, // Mindre än huvudrubriken
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF30436E),
            modifier = Modifier
                .padding(bottom = 20.dp) // Avstånd till nästa komponent
                .align(alignment = Alignment.CenterHorizontally)
        )
        Row(
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
                    .padding(20.dp),
                /*colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White, // Insidan av textfält
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray
                )*/
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
                    .padding(20.dp),
                /*colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray
                )*/
            )
        }
        OutlinedButton(
            onClick = {
                examInfo.fetchData(
                    courseCode = examId.component1().text,
                    anonymousCode = anonymousCode.component1().text
                )
                onNavigateToExam()
                examInfo.startBackUp(context)
            },
            colors = ButtonColors(Color(0xFF49546C), Color.White, Color.LightGray, Color.LightGray),
            border = BorderStroke(2.dp, Color(0xFF071D4F)),
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
                if ((examInfo.verifyBackupCredentials(
                        exId = examId.component1().text,
                        aCode = anonymousCode.component1().text,
                        context = context
                    ))
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