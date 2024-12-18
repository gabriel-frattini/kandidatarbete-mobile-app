package com.example.dat068_tentamina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.dat068_tentamina.ui.theme.DAT068TentaminaTheme
import com.example.dat068_tentamina.ui.Overlay
import com.example.dat068_tentamina.ui.Login
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import com.example.dat068_tentamina.viewmodel.ExamInfo
import com.example.dat068_tentamina.externalStorage.ExternalStorageManager

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
                val externalStorageManager = ExternalStorageManager()
                val tentaViewModel = remember { TentaViewModel() }
                val examInfo = remember { ExamInfo(tentaViewModel, externalStorageManager, this) }

                // Observe recovery mode
                val recoveryMode by tentaViewModel.recoveryMode.collectAsState()

                // Screen management
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
                tentaViewModel.addQuestions()
                examInfo.createTestExamPeriodJSON()

                when (currentScreen) {
                    Screen.Overlay -> Overlay(
                        viewModel = tentaViewModel,
                        examInfo = examInfo,
                        recoveryMode = recoveryMode // Pass the boolean state
                    )

                    Screen.Login -> Login(
                        viewModel = tentaViewModel,
                        examInfo = examInfo,
                        onNavigateToExam = { currentScreen = Screen.Overlay },
                    )
                }
            }
        }
    }
}
