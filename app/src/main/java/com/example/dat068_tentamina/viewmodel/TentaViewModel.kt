package com.example.dat068_tentamina.viewmodel

import Stack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line

class TentaViewModel {
    private val _objects = mutableStateListOf<CanvasObject>()
    private val history = Stack<List<CanvasObject>>()
    var textMode = mutableStateOf(false)
    var strokeWidth = 2.dp
    var eraserWidth = 6.dp
    var eraser = false
    val objects : SnapshotStateList<CanvasObject> get() = _objects

    fun addObject(obj : CanvasObject) {
        _objects.add(obj)
    }
    fun pop() {
        if (_objects.isNotEmpty()) {
            _objects.clear()
            val previousState = history.getCurrValue()
            if (previousState != null) {
                _objects.addAll(previousState)
            }
            history.pop()
        }
    }
    fun saveHistory() {
        // toList to avoid it as a reference
        history.append(_objects.toList())
    }


}