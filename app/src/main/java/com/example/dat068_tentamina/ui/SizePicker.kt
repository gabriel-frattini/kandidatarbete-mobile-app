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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.R
import com.example.dat068_tentamina.viewmodel.TentaViewModel
@Composable
fun SizePicker(viewModel: TentaViewModel, tool: String = "pen") {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = {
            expanded = !expanded
            viewModel.eraser = (tool != "pen")
            viewModel.textMode.value = false
        }) {
            if (tool == "pen") {
                Icon(Icons.Filled.Edit, contentDescription = "pen")
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.eraser),
                    contentDescription = "eraser",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(
                2.dp to 24.dp,   // Small size
                4.dp to 32.dp,   // Medium size
                6.dp to 48.dp,   // Large size
                8.dp to 64.dp,   // Extra Large
                10.dp to 96.dp,  // XXL
                12.dp to 128.dp  // Mega
            ).forEach { (penSize, eraserSize) ->
                DropdownMenuItem(
                    text = { },
                    leadingIcon = {
                        Canvas(modifier = Modifier.size(if (viewModel.eraser) eraserSize /2 else penSize)) {
                            drawCircle(
                                color = if (viewModel.eraser) Color.White else Color.Black,
                                radius = size.minDimension / 2
                            )
                        }
                    },
                    onClick = {
                        if (viewModel.eraser) {
                            viewModel.eraserWidth = eraserSize
                        } else {
                            viewModel.strokeWidth = penSize
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}
