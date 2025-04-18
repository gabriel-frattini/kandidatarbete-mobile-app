package com.example.dat068_tentamina.viewmodel

import Stack
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.CanvasObject
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.remove
import kotlin.text.set
import android.util.Log
import androidx.compose.ui.geometry.Rect
import com.example.dat068_tentamina.model.TextBox

enum class BackgroundType {
    BLANK, GRAPH, LINED, DOTTED
}  //Junyi


class TentaViewModel {
    private var _objects = mutableStateListOf<CanvasObject>()

    private var history = mutableMapOf<Int, Stack<List<CanvasObject>>>()
    private var redoHistory = mutableMapOf<Int, Stack<List<CanvasObject>>>()
    private var redoLive = mutableMapOf<Int, Stack<List<CanvasObject>>>()

    var answeredQuestions = mutableStateOf(setOf<Int>()) //För att markera frågor som besvarade/obesvarade

    var textMode = mutableStateOf(false)
    var strokeWidth = 2.dp
    var eraserWidth = 6.dp
    var eraser = false

    // elementIndexes is shared between MoveMode and Copy feature
    // for easier duplication(deepCopy) of marked lines
    private var elementIndexes = mutableListOf<Int>()
    private var _mark = mutableStateOf(false)
    var mark: Boolean
        get() = _mark.value
        set(value) {
            _mark.value = value
            if (!value) {
                copyModeAvailable = false
            }
        }

    // use copy feature
    var copy = mutableStateOf(false)
    // enable/disable copy-button & feature
    private var _copyModeAvailable = mutableStateOf(false)
    var copyModeAvailable: Boolean
        get() = _copyModeAvailable.value
        set(value) {
            _copyModeAvailable.value = value
            if (!value) {
                copy.value = false
            }
        }

    var currentQuestion = mutableIntStateOf(1)
    var currentCanvasHeight = mutableStateOf(2400.dp)
    val backgroundTypes = mutableStateMapOf<Int, BackgroundType>() //Junyi
    val objects: SnapshotStateList<CanvasObject> get() = _objects
    var questions = mutableMapOf<Int, List<CanvasObject>>()
    var height = mutableMapOf<Int, Dp>().apply {
        // Initialize default heights for each question (e.g., 2400.dp)
        for (i in 1..questions.size) {
            this[i] = 2600.dp
        }
    }
    var scrollPositions = mutableMapOf<Int, Int>()
    var questionChangeTrigger = mutableStateOf(0)
    val currentBackgroundType: BackgroundType
        get() = backgroundTypes[currentQuestion.intValue] ?: BackgroundType.BLANK
//Junyi

    fun copy() {
        copy.value = true
    }

    fun copyObjects(offset: Offset, start: Offset, end: Offset) {
        // check if copy is true here to avoid doing this twice
        if (copy.value) {
            findObjectsInsideArea(start, end)
            duplicateObjects(offset)
            moveObjects(offset)
        }
    }

    private fun duplicateObjects(offset: Offset) {
        if (copy.value) {
            elementIndexes.forEach { index ->
                if (!questions[currentQuestion.intValue].isNullOrEmpty()) {
                    val obj = questions[currentQuestion.intValue]!![index];
                    if (obj is Line) {
                        val duplicate = obj.deepCopy() as Line
                        duplicate.start -= offset * 2f;
                        duplicate.end -= offset * 2f;

                        addObject(duplicate)
                        elementIndexes[index] = objects.lastIndex
                    }
                }
            }
        }
    }

    @Synchronized
    fun addObject(obj: CanvasObject) {
        objects.add(obj)
        questions[currentQuestion.intValue] = _objects.toList()

        // Kontrollera om det finns objekt på frågan och markera den som besvarad om så är fallet
        if (_objects.isNotEmpty()) {
            answeredQuestions.value = answeredQuestions.value + currentQuestion.intValue
        }
    }

    fun deleteAll() {
        saveHistory()
        _objects.clear()
        questions[currentQuestion.intValue] = emptyList()
    }

    fun findObjectsInsideArea(start: Offset, end: Offset) {
        val left = minOf(start.x, end.x)
        val right = maxOf(start.x, end.x)
        val top = minOf(start.y, end.y)
        val bottom = maxOf(start.y, end.y)

        val selectionRect = Rect(left, top, right, bottom)
        elementIndexes = mutableListOf()

        questions[currentQuestion.intValue]?.forEachIndexed { i, obj ->
            if (obj is Line) {
                val isLineInside = selectionRect.contains(obj.start) && selectionRect.contains(obj.end)

                if (isLineInside) {
                    elementIndexes.add(i)
                }
            }
        }
    }

    fun moveObjects(amount: Offset) {
        elementIndexes.forEach{index ->
            if (!questions[currentQuestion.intValue].isNullOrEmpty()) {
                moveObject(questions[currentQuestion.intValue]!![index], amount, index)
            }
        }
    }

    private fun moveObject(obj: CanvasObject, amount: Offset, index: Int) {
        if (obj is Line) {
            // deepCopy, cause references to obj might exist in history, and if we
            // change start & end on obj only, all references will be affected which is bad
            val newObj = obj.deepCopy() as Line
            newObj.start = obj.start + amount
            newObj.end = obj.end + amount

            _objects[index] = newObj
            questions[currentQuestion.intValue] = _objects.toList()
        }
    }

    fun setBackgroundTypeForCurrentQuestion(type: BackgroundType) {
        backgroundTypes[currentQuestion.intValue] = type
    }//Junyi

    fun getAnswers(): MutableMap<Int, List<CanvasObject>> {
        return questions
    }

    fun undo() {
        val Q = currentQuestion.intValue;
        val previousState = history[Q]?.getCurrValue()
        if (previousState != null) {
            redoHistory[Q]?.append(previousState)
            redoLive[Q]?.append(_objects.toList())

            _objects.clear()
            _objects.addAll(previousState)

            questions[currentQuestion.intValue] = _objects.toList()
            history[Q]?.pop()
        }
        // Kolla om det finns några objekt kvar, annars markera frågan som obesvarad
        if (_objects.isEmpty()) {
            answeredQuestions.value = answeredQuestions.value - currentQuestion.intValue
        }
    }

    fun redo() {
        val Q = currentQuestion.intValue;
        if (redoHistory[Q]?.isNotEmpty() == true && redoLive[Q]?.isNotEmpty() == true) {
            val recoverHead = redoHistory[Q]?.getCurrValue()
            val live = redoLive[Q]?.getCurrValue()

            _objects.clear()

            if (live != null && recoverHead != null) {
                history[Q]?.append(recoverHead)
                _objects.addAll(live)
            }

            questions[currentQuestion.intValue] = _objects.toList()
            redoHistory[Q]?.pop()
            redoLive[Q]?.pop()
        }
        // Uppdatera answeredQuestions beroende på om det finns objekt kvar
        if (_objects.isEmpty()) {
            answeredQuestions.value = answeredQuestions.value - currentQuestion.intValue
        } else {
            answeredQuestions.value = answeredQuestions.value + currentQuestion.intValue
        }
    }

    fun saveHistory() {
        history[currentQuestion.intValue]?.append(_objects.toList())
        clearRedo()
    }

    private fun clearRedo() {
        redoHistory[currentQuestion.intValue]?.clear()
        redoLive[currentQuestion.intValue]?.clear()
    }

    fun addQuestions(size : Int) {
        for (i in 1..size) {
            questions[i] = emptyList()
            history[i] = Stack()
            redoHistory[i] = Stack()
            redoLive[i] = Stack()
        }
    }

    fun changeQuestion(qNr: Int, newObjects: List<CanvasObject>, canvasHeight: Dp) {
        textMode.value = false
        height[currentQuestion.intValue] = canvasHeight
        currentQuestion.intValue = qNr
        // Change the content on the DrawingScreen to the current question
        questionChangeTrigger.value++
        val currentObjects = questions[currentQuestion.intValue] ?: emptyList()
        _objects.clear()
        _objects.addAll(currentObjects)

        // Load rich text content and styles if available
        val textBox = currentObjects.find { it is TextBox } as? TextBox
        currentCanvasHeight.value = height[currentQuestion.intValue] ?: 2400.dp
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

        if (_objects.isEmpty()) {
            answeredQuestions.value = answeredQuestions.value - currentQuestion.intValue
        } else {
            // Om det finns objekt kvar på frågan, lägg till den i answeredQuestions om den inte redan finns där
            answeredQuestions.value = answeredQuestions.value + currentQuestion.intValue
        }
    }
}
