package com.example.dat068_tentamina

import ExamInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.dat068_tentamina.ui.theme.DAT068TentaminaTheme
import com.example.dat068_tentamina.ui.Overlay
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import com.example.dat068_tentamina.ui.Login
import org.json.JSONObject


sealed class Screen {
    object Overlay : Screen()
    object Login : Screen()
    //object InfoPage : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DAT068TentaminaTheme {
                val tentaViewModel = TentaViewModel()
                val examInfo = ExamInfo()
                tentaViewModel.addQuestions()
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) } // Start with Login

            when (currentScreen) {
                Screen.Overlay -> Overlay(tentaViewModel,this, examInfo)
                Screen.Login -> Login(examInfo,onNavigateToExam = { currentScreen = Screen.Overlay })
            }
        }
    }
}}





