package com.example.dat068_tentamina.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.dat068_tentamina.R
import com.example.dat068_tentamina.viewmodel.BackgroundType
import com.example.dat068_tentamina.viewmodel.TentaViewModel

@Composable
fun BackgroundPicker(viewModel: TentaViewModel) {
    // State to manage the visibility of the dropdown menu
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Button to open dropdown
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Select Background",
                modifier = Modifier.size(30.dp) //Adjust size for visibility
            )
        }

        // Dropdown Menu for Background Selection
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Icon(
                        painter = painterResource(id = R.drawable.blank),
                        contentDescription = "Blank",
                        modifier = Modifier.size(26.dp) // ✅ Slightly bigger for dropdown clarity
                    )
                },
                onClick = {
                    viewModel.setBackgroundTypeForCurrentQuestion(BackgroundType.BLANK)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Icon(
                        painter = painterResource(id = R.drawable.graph),
                        contentDescription = "Graph",
                        modifier = Modifier.size(26.dp)
                    )
                },
                onClick = {
                    viewModel.setBackgroundTypeForCurrentQuestion(BackgroundType.GRAPH)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Icon(
                        painter = painterResource(id = R.drawable.lined),
                        contentDescription = "Lined",
                        modifier = Modifier.size(26.dp)
                    )
                },
                onClick = {
                    viewModel.setBackgroundTypeForCurrentQuestion(BackgroundType.LINED)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Icon(
                        painter = painterResource(id = R.drawable.dotted),
                        contentDescription = "Dotted",
                        modifier = Modifier.size(26.dp)
                    )
                },
                onClick = {
                    viewModel.setBackgroundTypeForCurrentQuestion(BackgroundType.DOTTED)
                    expanded = false
                }
            )
        }
    }
}

