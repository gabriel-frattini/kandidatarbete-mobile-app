package com.example.dat068_tentamina.viewmodel

import Stack
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.dat068_tentamina.model.Line

class TentaViewModel {
    private val _lines = mutableStateListOf<Line>()
    private val history = Stack<List<Line>>()
    val lines : SnapshotStateList<Line> get() = _lines

    fun addLine(line : Line) {
        _lines.add(line)
    }
    fun pop() {
        if (_lines.isNotEmpty() && history.isNotEmpty()) {
            _lines.clear()
            val previousState = history.getCurrValue()
            if (previousState != null) {
                _lines.addAll(previousState)
            }
            history.pop()
        }
    }
    fun saveHistory() {
        // toList to avoid it as a reference
        history.append(_lines.toList())
    }

}