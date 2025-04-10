package com.example.dat068_tentamina.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CustomAlertDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "OK",
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(text = message) },
            confirmButton = {
                Button(onClick = { onConfirm() }) {
                    Text(text = confirmButtonText)
                }
            }
        )
    }
}