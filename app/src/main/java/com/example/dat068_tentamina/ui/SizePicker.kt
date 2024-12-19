package com.example.dat068_tentamina.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.R
import com.example.dat068_tentamina.viewmodel.TentaViewModel

@Composable
fun SizePicker(viewModel: TentaViewModel, tool: String = "pen") {
    // State to manage the visibility of the menu
    var expanded by remember { mutableStateOf(false) }

    // Anchor element for the dropdown menu
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (tool == "pen") {
            viewModel.eraser = false
            IconButton(onClick = { expanded = !expanded} ) {
                Icon(Icons.Filled.Edit, contentDescription = "erase")
            }
        } else {
            viewModel.eraser = true
            IconButton(onClick = { expanded = !expanded} ) {
                Icon(painter = painterResource(id = R.drawable.eraser), contentDescription = "eraser", modifier = Modifier.size(30.dp))
            }
        }
        viewModel.textMode.value = false


        // Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false } // Collapse when clicking outside
        ) {
            DropdownMenuItem(text = {
                    IconButton(onClick = {
                        if (tool == "pen")
                            viewModel.strokeWidth = 2.dp
                        else
                            viewModel.eraserWidth = 6.dp
                    }) {
                        Icon(painter = painterResource(id = R.drawable.dot_small_svgrepo_com), contentDescription = "size1", modifier = Modifier.size(30.dp))
                    }}, onClick = { expanded = false }
            )
            DropdownMenuItem(text = {
                IconButton(onClick = {
                    if (tool == "pen")
                        viewModel.strokeWidth = 4.dp
                    else
                        viewModel.eraserWidth = 12.dp
                }) {
                    Icon(painter = painterResource(id = R.drawable.dot_small_svgrepo_com), contentDescription = "size2", modifier = Modifier.size(40.dp))
                }}, onClick = { expanded = false }
            )
            DropdownMenuItem(text = {
                IconButton(onClick = {
                    if (tool == "pen")
                        viewModel.strokeWidth = 6.dp
                    else
                        viewModel.eraserWidth = 24.dp
                }) {
                    Icon(painter = painterResource(id = R.drawable.dot_small_svgrepo_com), contentDescription = "size3", modifier = Modifier.size(50.dp))
                }}, onClick = { expanded = false }
            )

        }
    }
}
