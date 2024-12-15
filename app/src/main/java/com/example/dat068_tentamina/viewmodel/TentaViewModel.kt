package com.example.dat068_tentamina.viewmodel

import Stack
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.ui.DrawingScreen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log

class TentaViewModel {
    private var _objects = mutableStateListOf<CanvasObject>()
    private val history = Stack<List<CanvasObject>>()
    //private var historyMap = mutableMapOf<Int, Stack<List<CanvasObject>>>()
    var textMode = mutableStateOf(false)
    var strokeWidth = 2.dp
    var eraserWidth = 6.dp
    var eraser = false
    var currentQuestion = mutableIntStateOf(1)
    val objects: SnapshotStateList<CanvasObject> get() = _objects
    var questions = mutableStateMapOf<Int, List<CanvasObject>>()

    @Synchronized
    fun addObject(obj: CanvasObject) {
            objects.add(obj)
            questions[currentQuestion.intValue] = _objects.toList()
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
        //save the current question nr for usage across classes
        currentQuestion.intValue = qNr
        // Change the content on the DrawingScreen to the current question
        val currentObjects = questions[currentQuestion.intValue] ?: emptyList()
        _objects.clear()
        _objects.addAll(currentObjects)

        //history is cleared when changing to a new question
        history.clear()
    }

    fun getAnswers(): Map<Int, List<CanvasObject>>{
        return questions.toMap()
    }


    fun testGetAnswerJSON(): JSONArray{

        val canvasArray = JSONArray()

        for ((key, canvasList) in questions) {
            val canvasObject = JSONObject().apply {
                put(key.toString(),canvasList)
            }
            canvasArray.put(key,canvasObject)
        }
        println(canvasArray)
        return canvasArray

    }
}
