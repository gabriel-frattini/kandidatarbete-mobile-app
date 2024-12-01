package com.example.dat068_tentamina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dat068_tentamina.ui.theme.DAT068TentaminaTheme
import com.example.dat068_tentamina.ui.Overlay
import com.example.dat068_tentamina.viewmodel.TentaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DAT068TentaminaTheme {
                val tentaViewModel = TentaViewModel()
                tentaViewModel.addQuestions()
                Overlay(tentaViewModel, this)
            }
        }
    }
}





