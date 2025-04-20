package com.example.dat068_tentamina.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
fun ShapePicker(viewModel: TentaViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = {
            expanded = !expanded
        }) {
            Icon(
                painter = painterResource(id = R.drawable.category),
                contentDescription = "category",
                modifier = Modifier.size(30.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            IconButton(onClick = {
                expanded = false
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.line_thin),
                    contentDescription = "line_thin",
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(onClick = {
                expanded = false
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.circle),
                    contentDescription = "circle",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
