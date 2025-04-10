package com.example.dat068_tentamina

import ExamInfo
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.dat068_tentamina.ui.Overlay
import com.example.dat068_tentamina.ui.Login
import com.example.dat068_tentamina.ui.WaitingScreen
import com.example.dat068_tentamina.ui.theme.DAT068TentaminaTheme
import com.example.dat068_tentamina.ui.LoadingScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import com.example.dat068_tentamina.ui.CustomAlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import kotlinx.coroutines.launch




sealed class Screen {
    object Overlay : Screen()
    object Login : Screen()
    object Waiting : Screen()
}

class MainActivity : ComponentActivity() {

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setupDevicePolicy()
            } else {
                Toast.makeText(this, "Device admin not activated", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableImmersiveMode()
        setupDevicePolicy()

        setContent {
            DAT068TentaminaTheme {

                val examInfo = remember { ExamInfo() }
                val recoveryMode by examInfo.recoveryMode.collectAsState()
                var isDataFetched by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

                var showAlertDialog by remember { mutableStateOf(false) }
                var alertDialogTitle by remember { mutableStateOf("") }
                var alertDialogMessage by remember { mutableStateOf("") }
                var alertDialogConfirmAction by remember { mutableStateOf<() -> Unit>({}) }



                LaunchedEffect(Unit) {
                    examInfo.setOnDataFetched {

                        //examInfo.setTestExamData("2025-04-10", "15:17", "16:00")

                        val examDate = examInfo.getExamDate()
                        val examStartTime = examInfo.getExamStartTime()
                        val examEndTime = examInfo.getExamEndTime()




                        if (examStartTime.isEmpty() || examEndTime.isEmpty()) {
                            /*
                            If we use the real exam logic, the rest are for testing
                            alertDialogTitle = "Exam Info"
                            alertDialogMessage = "Exam time information is missing. Cannot log in."
                            alertDialogConfirmAction = {  }
                            showAlertDialog = true
                            return@setOnDataFetched
                            Uncomment and comment the code under
                             */

                            currentScreen = Screen.Overlay
                            return@setOnDataFetched
                        }

                        if (examDate.isEmpty()) {
                            //alertDialogTitle = "Exam Info"
                            //alertDialogMessage = "Exam data is missing! Please check with the teacher."
                            //alertDialogConfirmAction = {  }
                            //showAlertDialog = true
                            //return@setOnDataFetched
                            currentScreen = Screen.Overlay
                            return@setOnDataFetched
                        }

                        if (examDate == examInfo.getTodayDate()) {
                            if (!examInfo.canStartExam()) {
                                currentScreen = Screen.Waiting
                            } else if (!examInfo.isExamOver()) {
                                currentScreen = Screen.Overlay

                                launch(Dispatchers.IO) {
                                    examInfo.startBackUp(this@MainActivity)
                                }
                            }

                            /*

                        if (examDate == examInfo.getTodayDate()) {

                            if (!examInfo.canStartExam()) {
                                currentScreen = Screen.Waiting
                            }

                            else if (!examInfo.isExamOver()) {

                                currentScreen = Screen.Overlay

                                launch(Dispatchers.IO) {
                                    examInfo.startBackUp(this@MainActivity)
                                }
                            }
                             */

                            else {

                                alertDialogTitle = "Exam Ended"
                                alertDialogMessage = "The exam has ended!"
                                //alertDialogConfirmAction = { }
                                showAlertDialog = true
                            }
                        } else {
                            // Om datumet inte stämmer, navigera ändå till tentamen
                            /*
                            alertDialogTitle = "Exam Info"
                            alertDialogMessage = "Exam data does not match today's date."
                            alertDialogConfirmAction = {  }
                            showAlertDialog = true
                             */
                            currentScreen = Screen.Overlay
                        }
                        isDataFetched = true
                    }
                }


                CustomAlertDialog(
                    showDialog = showAlertDialog,
                    title = alertDialogTitle,
                    message = alertDialogMessage,
                    onConfirm = {
                        showAlertDialog = false
                        alertDialogConfirmAction()
                    },
                    onDismissRequest = { showAlertDialog = false }
                )


                when (currentScreen) {
                    Screen.Overlay -> {
                        if (isDataFetched) {
                            Overlay(
                                viewModel = examInfo.getTentaModel(),
                                examInfo = examInfo,
                                activity = this,
                                recoveryMode = recoveryMode,
                                signout = {
                                    unlockApp()
                                    isDataFetched = false
                                    examInfo.clearInfo()
                                    currentScreen = Screen.Login
                                }
                            )
                        } else {
                            LoadingScreen{isDataFetched = true}
                        }
                    }
                    Screen.Login -> {
                        Login(
                            examInfo = examInfo,
                            onNavigateToExam = {

                                currentScreen = Screen.Overlay
                            }
                        )
                    }
                    Screen.Waiting -> {
                        WaitingScreen(
                            examInfo = examInfo,
                            onNavigateToExam = {
                                currentScreen = Screen.Overlay
                                examInfo.startBackUp(this)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
        val devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Please activate device admin to control device features."
            )
            activityResultLauncher.launch(intent)
        } else {
            Toast.makeText(this, "Device admin is already active.", Toast.LENGTH_SHORT).show()
            setupDevicePolicyLogic()
        }
    }

    private fun setupDevicePolicyLogic() {
        val devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
        // Ytterligare policy-logik vid behov
    }

    private fun lockApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            if (!activityManager.isInLockTaskMode) {
                startLockTask()
                Toast.makeText(this, "App is locked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun unlockApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopLockTask()
            Toast.makeText(this, "App is unlocked", Toast.LENGTH_SHORT).show()
        }
    }
}

