package com.example.dat068_tentamina.ui


import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import ExamInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ExamCountdownTimer(examInfo: ExamInfo, modifier: Modifier = Modifier) {
    var remainingTime by remember { mutableStateOf(0L) }

    LaunchedEffect(examInfo.getExamDate(), examInfo.getExamStartTime()) {

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Europe/Stockholm")
        }
        val examDate = examInfo.getExamDate()
        val examStartTime = examInfo.getExamStartTime()
        val examStartDateTimeStr = "$examDate $examStartTime"

        val examStartDate: Date? = try {
            sdf.parse(examStartDateTimeStr)
        } catch (e: Exception) {
            null
        }

        while (true) {
            val now = System.currentTimeMillis()
            remainingTime = (examStartDate?.time ?: now) - now
            if (remainingTime < 0L) {
                remainingTime = 0L
                break
            }
            delay(1000L)
        }
    }


    val seconds = (remainingTime / 1000) % 60
    val minutes = (remainingTime / (1000 * 60)) % 60
    val hours = remainingTime / (1000 * 60 * 60)
    val timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)


    Card(
        modifier = modifier.widthIn(max = 200.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeStr,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WaitingScreen(
    examInfo: ExamInfo,
    onNavigateToExam: () -> Unit
) {
    var waiting by remember { mutableStateOf(true) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    LaunchedEffect(Unit) {
        val checkRunnable = object : Runnable {
            override fun run() {
                if (examInfo.canStartExam()) {
                    waiting = false
                    onNavigateToExam()
                } else {
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(checkRunnable)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Waiting for exam to start...",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))

            ExamCountdownTimer(examInfo = examInfo)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Get ready! The exam will begin soon.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

