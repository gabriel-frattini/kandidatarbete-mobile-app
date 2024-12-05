package com.example.dat068_tentamina


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.dat068_tentamina.ui.theme.DAT068TentaminaTheme
import com.example.dat068_tentamina.ui.Overlay
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import com.example.dat068_tentamina.ui.Login
import com.example.dat068_tentamina.viewmodel.ExamInfo
import com.example.dat068_tentamina.externalStorage.ExternalStorage
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.dat068_tentamina.externalStorage.ExternalStorageManager
import com.example.dat068_tentamina.ui.externalStorage


sealed class Screen {
    object Overlay : Screen()
    object Login : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DAT068TentaminaTheme {
                val tentaViewModel = TentaViewModel()
                val externalStorageManager = ExternalStorageManager()
                val examInfo = remember {ExamInfo(tentaViewModel, externalStorageManager, this)}

                tentaViewModel.addQuestions()
                examInfo.createTestExamPeriodJSON()

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login)} // Start with Login

                when (currentScreen) {
                    Screen.Overlay -> Overlay(tentaViewModel,examInfo)
                    Screen.Login -> Login(examInfo,onNavigateToExam = { currentScreen = Screen.Overlay })
                }




        }
    }
}}





