package com.example.dat068_tentamina.viewmodel

import Stack
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.CanvasObject

class TentaViewModel {
    private val _objects = mutableStateListOf<CanvasObject>()
    private val history = Stack<List<CanvasObject>>()
    private var ob: List<CanvasObject>? = emptyList()
    val objects: SnapshotStateList<CanvasObject> get() = _objects
    val questions = mutableMapOf<Int, List<CanvasObject>>()
    // Modes
    var textMode = mutableStateOf(false)
    var eraser = false
    var currentQuestion = mutableIntStateOf(1)

    var strokeWidth = 2.dp
    var eraserWidth = 6.dp

    fun addObject(obj: CanvasObject) {
        objects.add(obj)
        questions[currentQuestion.intValue] = _objects.toList()
    }

    fun getAnswers(): MutableMap<Int, List<CanvasObject>> {
        return questions
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
        history.append(_objects.toList())
    }

    fun addQuestions() {
        for (i in 1..10) {
            questions[i] = emptyList()
        }
    }

    fun changeQuestion(qNr: Int) {
        currentQuestion.intValue = qNr

        /* ska vi göra detta eller? osäker om vi ska börja jobba på att spara svaren o.s.v.
         questions[currentQuestion.intValue] = _objects.toList()
        _objects.clear()
        val newObjects = questions[qNr] ?: emptyList()
        _objects.addAll(newObjects)
         */
    }
}
