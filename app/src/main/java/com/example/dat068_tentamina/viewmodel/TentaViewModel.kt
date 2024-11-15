package com.example.dat068_tentamina.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.dat068_tentamina.model.Line

class TentaViewModel {
    private val _lines = mutableStateListOf<Line>()
    val lines : SnapshotStateList<Line> get() = _lines

    fun addLine(line : Line) {
        _lines.add(line)
    }
    fun pop() {
        if (_lines.isNotEmpty()) {
            _lines.removeLast()
        }
    }
}