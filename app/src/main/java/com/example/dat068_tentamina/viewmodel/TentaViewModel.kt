package com.example.dat068_tentamina.viewmodel

import Stack
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.CanvasObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.remove
import kotlin.text.set

class TentaViewModel {
    private var _objects = mutableStateListOf<CanvasObject>()
    private val history = Stack<List<CanvasObject>>()
    //private var historyMap = mutableMapOf<Int, Stack<List<CanvasObject>>>()
    var textMode = mutableStateOf(false)
    var strokeWidth = 2.dp
    var eraserWidth = 6.dp
    var eraser = false
    var currentQuestion = mutableIntStateOf(1)
    var currentCanvasHeight = mutableStateOf(2400.dp)
    val objects: SnapshotStateList<CanvasObject> get() = _objects
    var questions = mutableMapOf<Int, List<CanvasObject>>()
    var height = mutableMapOf<Int, Dp>().apply {
        // Initialize default heights for each question (e.g., 2400.dp)
        for (i in 1..questions.size) {
            this[i] = 2600.dp
        }
    }
    var scrollPositions = mutableMapOf<Int, Int>()

    @Synchronized
    fun addObject(obj: CanvasObject) {
            objects.add(obj)
            questions[currentQuestion.intValue] = _objects.toList()
    }

    fun getAnswers(): MutableMap<Int, List<CanvasObject>> {
        return questions
    }

    // TODO: (Gabbe) Here is the 'undo' function.
    //  Make it scoped to the active question user is viewing
    // Also add another function to redo the undoing
    fun pop() {
        if (_objects.isNotEmpty()) {
            _objects.clear()
            val previousState = history.getCurrValue()
            if (previousState != null) {
                _objects.addAll(previousState)
            }
            // We don't want to pop the actual history because we need to keep it to be able to redo.
            // Here we could 'soft delete' the change instead of popping it,
            // we set a flag like 'deleted': true for undo (then remove change from the UI)
            // and when the user clicks redo, we set 'deleted' : false again, and show the change in the UI
            history.pop()
        }
    }

    fun saveHistory() {
        history.append(_objects.toList())
    }

    fun addQuestions(size : Int) {
        for (i in 1..size) {
            questions[i] = emptyList()
        }
    }

    fun changeQuestion(qNr: Int, newObjects: List<CanvasObject>, canvasHeight: Dp) {
        textMode.value = false
        height[currentQuestion.intValue] = canvasHeight
        currentQuestion.intValue = qNr
        // Change the content on the DrawingScreen to the current question
        val currentObjects = questions[currentQuestion.intValue] ?: emptyList()
        _objects.clear()
        _objects.addAll(currentObjects)
        currentCanvasHeight.value = height[currentQuestion.intValue] ?: 2400.dp

        //history is cleared when changing to a new question
        history.clear()
    }
    fun updateCanvasHeight(newHeight: Dp) {
        currentCanvasHeight.value = newHeight
        height[currentQuestion.intValue] = newHeight
    }
    fun saveScrollPosition(questionNr: Int, scrollValue: Int) {
        if (scrollValue > 0) {
            scrollPositions[questionNr] = scrollValue
        } else {
            scrollPositions[questionNr] = 0 // Ensure default is 0 for unscrolled questions
        }
    }
    fun getScrollPosition(questionId: Int): Int {
        return scrollPositions[questionId] ?: 0
    }

    fun replaceObject(oldObject: CanvasObject, newObject: CanvasObject) {
        val index = _objects.indexOf(oldObject)
        if (index != -1) {
            _objects[index] = newObject
            questions[currentQuestion.intValue] = _objects.toList()
        }
    }

    fun removeObject(obj: CanvasObject) {
        _objects.remove(obj)
        questions[currentQuestion.intValue] = _objects.toList()
    }
}
