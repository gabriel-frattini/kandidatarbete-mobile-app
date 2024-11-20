package com.example.dat068_tentamina.viewmodel

import Stack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.ui.DrawingScreen
import com.example.dat068_tentamina.ui.ExamScreen
import com.example.dat068_tentamina.ui.Overlay

class TentaViewModel {
    private val _objects = mutableStateListOf<CanvasObject>()
    private val history = Stack<List<CanvasObject>>()
    var textMode = mutableStateOf(false)
    var strokeWidth = 2.dp
    var eraserWidth = 6.dp
    var eraser = false
    val objects : SnapshotStateList<CanvasObject> get() = _objects
    val questions = mutableMapOf<Int, List<CanvasObject>>()
    private var currentQuestion = 1
    private var ob : List<CanvasObject>? = emptyList()


    fun addObject(obj : CanvasObject) {
        objects.add(obj)
        questions.put(currentQuestion,_objects)
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
    fun addQuestions()
    {
        var i = 1
        while(i<=10)
        {
            //just adding empty first
            questions.put(i, emptyList())
            i++
        }
    }
    fun changedQuestion(qNr:Int)
    {
        currentQuestion = qNr
        // hur ska jag ändra värdet på _object för varje page???
        ob =  questions.get(currentQuestion)
    }
    fun getCurrentQuestionNr(): Int
    {
        return currentQuestion
    }
}