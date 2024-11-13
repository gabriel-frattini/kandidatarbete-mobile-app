package com.example.dat068_tentamina

import android.R.attr
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dat068_tentamina.ui.theme.DAT068TentaminaTheme
import java.time.format.TextStyle


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DAT068TentaminaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainLayout("LEU432","Grundläggande dator teknink", "20030923", modifier = Modifier )
                }
            }
        }
    }
}
@Composable
fun MainLayout(courseCode: String, courseName: String, date : String, modifier: Modifier = Modifier) {

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .background(Color.LightGray)
            .fillMaxSize()
    ) {
        Text(
            text = date,
            fontSize = 25.sp,
            lineHeight = 25.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .padding(10.dp)
                .align(alignment = Alignment.End)
        )
        Text(
            text = courseCode,
            fontSize = 100.sp,
            lineHeight = 116.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp)
                .align(alignment = Alignment.CenterHorizontally)
        )
        Row (

            horizontalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxWidth()
            )
        {

                val examId = remember { mutableStateOf(TextFieldValue("")) }
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
                val anonymousCode = remember { mutableStateOf(TextFieldValue("")) }
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
            onClick = { ready() },
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(20.dp)
        ) {
            Text("Check in")
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


@Preview(showBackground = true)
@Composable
fun LayoutPreview() {
    DAT068TentaminaTheme {
        MainLayout("DAT068", "Projekt", "20241113")
    }
}

fun ready()
{
    println("Button pressed")
}