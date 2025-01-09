package com.example.dat068_tentamina

import ExamInfo
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Device admin has been activated
            setupDevicePolicy()
        } else {
            // User denied the device admin request
            Toast.makeText(this, "Device admin not activated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable Immersive Mode
        enableImmersiveMode()

        // Manage Device Policy
        setupDevicePolicy()

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
                                    // Signout: Unlock the app
                                    unlockApp()
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
                            onNavigateToExam = {
                                // Lock the app when navigating to the exam
                                lockApp()
                                currentScreen = Screen.Overlay
                            }
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

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun setupDevicePolicy() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            // Request to activate device admin if it's not already active
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Please activate device admin to control device features.")
            activityResultLauncher.launch(intent)
        } else {
            // Device admin is already active, proceed with your logic
            Toast.makeText(this, "Device admin is already active.", Toast.LENGTH_SHORT).show()
            setupDevicePolicyLogic()
        }
    }

    private fun setupDevicePolicyLogic() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

        // Example: Disable the camera
        devicePolicyManager.setCameraDisabled(adminComponent, true)

        // Notify user
        Toast.makeText(this, "Camera has been disabled.", Toast.LENGTH_SHORT).show()
    }

    // Method to lock the app (enter Lock Task Mode)
    private fun lockApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            if (!activityManager.isInLockTaskMode) {
                startLockTask()
                Toast.makeText(this, "App is locked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to unlock the app (exit Lock Task Mode)
    private fun unlockApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopLockTask()
            Toast.makeText(this, "App is unlocked", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun LoadingScreen() {
    // A simple loading screen placeholder
    Text(text = "Loading...")
}
