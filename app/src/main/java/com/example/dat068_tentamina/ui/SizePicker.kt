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
import com.example.dat068_tentamina.viewmodel.ToolMode

@Composable
fun SizePicker(viewModel: TentaViewModel, tool: String = "pen", iconTint: Color = Color.Black) {
    var expanded by remember { mutableStateOf(false) }
    val isEraser = tool == "eraser"

    Box(contentAlignment = Alignment.Center) {
        IconButton(onClick = {
            expanded = !expanded
            viewModel.selectedTool.value = if (isEraser) ToolMode.ERASER else ToolMode.PEN
        }) {
            if (isEraser) {
                Icon(
                    painter = painterResource(id = R.drawable.eraser),
                    contentDescription = "Eraser",
                    modifier = Modifier.size(30.dp),
                    tint = iconTint
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Pen",
                    tint = iconTint
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(
                2.dp to 24.dp,
                4.dp to 32.dp,
                6.dp to 48.dp,
                8.dp to 64.dp,
                10.dp to 96.dp,
                12.dp to 128.dp
            ).forEach { (penSize, eraserSize) ->
                DropdownMenuItem(
                    text = { },
                    leadingIcon = {
                        Canvas(modifier = Modifier.size(if (isEraser) eraserSize / 2 else penSize)) {
                            drawCircle(
                                color = if (isEraser) Color.White else Color.Black,
                                radius = size.minDimension / 2
                            )
                        }
                    },
                    onClick = {
                        if (isEraser) viewModel.eraserWidth = eraserSize
                        else viewModel.strokeWidth = penSize
                        expanded = false
                    }
                )
            }
        }
    }
}

