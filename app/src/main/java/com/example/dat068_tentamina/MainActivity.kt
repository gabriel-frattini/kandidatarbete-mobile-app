package com.example.dat068_tentamina

import ExamInfo
import android.app.ActivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.dat068_tentamina.ui.theme.DAT068TentaminaTheme
import com.example.dat068_tentamina.ui.Overlay
import com.example.dat068_tentamina.ui.Login

sealed class Screen {
    object Overlay : Screen()
    object Login : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable Lock Task Mode if app is whitelisted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            if (!activityManager.isInLockTaskMode) {
                startLockTask()
            }
        }

        setContent {
            DAT068TentaminaTheme {
                var examInfo = remember { ExamInfo() }
                val recoveryMode by examInfo.recoveryMode.collectAsState()
                var isDataFetched by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

                // Callback for when data is fetched
                LaunchedEffect(Unit) {
                    examInfo.setOnDataFetched {
                        isDataFetched = true
                        currentScreen = Screen.Overlay
                    }
                }

                when (currentScreen) {
                    Screen.Overlay -> {
                        if (isDataFetched) {
                            Overlay(
                                viewModel = examInfo.getTentaModel(),
                                examInfo = examInfo,
                                activity = this,
                                recoveryMode = recoveryMode,
                                signout = {
                                    isDataFetched = false
                                    examInfo.clearInfo()
                                    currentScreen = Screen.Login
                                }
                            )
                        } else {
                            LoadingScreen() // A composable for loading
                        }
                    }
                    Screen.Login -> {
                        Login(
                            examInfo = examInfo,
                            onNavigateToExam = { currentScreen = Screen.Overlay }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Exit Lock Task Mode when activity is destroyed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopLockTask()
        }
    }
}

@Composable
fun LoadingScreen() {
    // A simple loading screen placeholder
    Text(text = "Loading...")
}