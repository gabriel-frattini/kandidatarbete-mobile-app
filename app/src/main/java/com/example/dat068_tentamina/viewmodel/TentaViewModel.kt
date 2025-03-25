package com.example.dat068_tentamina.viewmodel

import Stack
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.dat068_tentamina.model.TextBox

enum class BackgroundType {
    BLANK, GRAPH, LINED, DOTTED
}  //Junyi

class TentaViewModel {
    private var _objects = mutableStateListOf<CanvasObject>()

    private var history = mutableMapOf<Int, Stack<List<CanvasObject>>>()
    private var redoHistory = mutableMapOf<Int, Stack<List<CanvasObject>>>()
    private var redoLive = mutableMapOf<Int, Stack<List<CanvasObject>>>()

    var textMode = mutableStateOf(false)
    var strokeWidth = 2.dp
    var eraserWidth = 6.dp
    var eraser = false
    var currentQuestion = mutableIntStateOf(1)
    var currentCanvasHeight = mutableStateOf(2400.dp)
    var backgroundType = mutableStateOf(BackgroundType.BLANK) //Junyi
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


    @Synchronized
    fun addObject(obj: CanvasObject) {
            objects.add(obj)
            questions[currentQuestion.intValue] = _objects.toList()
    }
    //Eraser removes intersecting objects
    fun removeIntersectingObjects(start: Offset, end: Offset, eraserWidth: Dp) {
        val eraserRadius = eraserWidth.value / 2
        val newLines = mutableListOf<Line>()

        val objectsToModify = objects.filterIsInstance<Line>().filter { line ->
            isLineIntersecting(line.start, line.end, start, end, eraserRadius)
        }

        objectsToModify.forEach { line ->
            removeObject(line) // Remove the original line

            // Try to split the line
            val splitLines = splitLineAtIntersection(line, start, end, eraserRadius)
            newLines.addAll(splitLines)
        }

        // Add the remaining un-erased parts of the lines back
        newLines.forEach { addObject(it) }
    } //Junyi

    private fun splitLineAtIntersection(line: Line, eraseStart: Offset, eraseEnd: Offset, radius: Float): List<Line> {
        val intersectionPoints = findIntersectionPoints(line.start, line.end, eraseStart, eraseEnd, radius)

        if (intersectionPoints.isEmpty()) return listOf(line) // If no intersection, return original line

        val splitLines = mutableListOf<Line>()
        var lastPoint = line.start

        // Create new smaller lines between the intersection points
        intersectionPoints.forEach { point ->
            if ((point - lastPoint).getDistance() > radius) { // Avoid tiny lines
                splitLines.add(Line(start = lastPoint, end = point, strokeWidth = line.strokeWidth))
            }
            lastPoint = point
        }

        if ((line.end - lastPoint).getDistance() > radius) {
            splitLines.add(Line(start = lastPoint, end = line.end, strokeWidth = line.strokeWidth))
        }

        return splitLines
    }

    private fun findIntersectionPoints(lineStart: Offset, lineEnd: Offset, eraseStart: Offset, eraseEnd: Offset, radius: Float): List<Offset> {
        val points = mutableListOf<Offset>()

        // Sample multiple points along the line to check where the eraser overlaps
        val numSamples = 20
        for (i in 0..numSamples) {
            val t = i / numSamples.toFloat()
            val pointOnLine = Offset(
                lineStart.x + (lineEnd.x - lineStart.x) * t,
                lineStart.y + (lineEnd.y - lineStart.y) * t
            )

            if (minDistanceBetweenLineAndPoint(eraseStart, eraseEnd, pointOnLine) <= radius) {
                points.add(pointOnLine)
            }
        }

        return points
    } //Junyi

    private fun isLineIntersecting(
        lineStart: Offset, lineEnd: Offset,
        eraseStart: Offset, eraseEnd: Offset,
        radius: Float
    ): Boolean {
        val minDist = minDistanceBetweenLineAndPoint(lineStart, lineEnd, eraseStart)
        val maxDist = minDistanceBetweenLineAndPoint(lineStart, lineEnd, eraseEnd)
        return minDist <= radius || maxDist <= radius
    } //Junyi

    private fun minDistanceBetweenLineAndPoint(lineStart: Offset, lineEnd: Offset, point: Offset): Float {
        val lineLengthSquared = (lineEnd - lineStart).getDistanceSquared()
        if (lineLengthSquared == 0f) return (point - lineStart).getDistance() // If line is a single point

        val t = ((point - lineStart).dot(lineEnd - lineStart) / lineLengthSquared).coerceIn(0f, 1f)
        val closestPoint = lineStart + (lineEnd - lineStart) * t
        return (point - closestPoint).getDistance()
    }//Junyi

    private fun Offset.dot(other: Offset): Float {
        return this.x * other.x + this.y * other.y
    }//Junyi

    fun getAnswers(): MutableMap<Int, List<CanvasObject>> {
        return questions
    }

    fun undo() {
        if (_objects.isNotEmpty()) {
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
    }
}
