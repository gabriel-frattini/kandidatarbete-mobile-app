package com.example.dat068_tentamina.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.externalStorage.ExternalStorageManager
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.serializable.Answer
import com.example.dat068_tentamina.model.serializable.StudentInfo
import com.example.dat068_tentamina.model.serializable.SerializableTextbox
import com.example.dat068_tentamina.model.serializable.SerializableLine
import com.example.dat068_tentamina.utils.CanvasObjectSerializationUtils.toOffset
import com.example.dat068_tentamina.utils.CanvasObjectSerializationUtils.toSerializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

class ExamInfo(tV: TentaViewModel, exManager : ExternalStorageManager, context: Context) {
    var examObject = JSONObject()
    var studentsObject = JSONObject()
    var storageObject = JSONObject()
    private var anonymousCode = ""
    private var examID = ""
    private var personalNumber = ""
    private var name = ""
    private var tentaViewModel: TentaViewModel = tV
    private var externalStorageManager : ExternalStorageManager = exManager
    private var context : Context = context
    val job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)


    fun getAnonymousCode(): String {
        return anonymousCode
    }

    fun getPersonalNumber(): String {
        return personalNumber
    }

    fun getName(): String {
        return name
    }


    fun createTestExamPeriodJSON() {
        //should be able to remove this test as soon as it is merged with submit...

        studentsObject = JSONObject().apply {
            put("123ALI", JSONObject().apply {
                put("personalNumber", "0309230000")
                put("name", "Alice Emanuelsson")
            })
            put("123CHE", JSONObject().apply {
                put("personalNumber", "0204120000")
                put("name", "Che Long Tran")
            })
        }
        examObject = JSONObject().apply {
            put("testID", JSONObject().apply {
                put("examId", "testID")
                put("date", "2003-09-23")
                put("time(h)", 7)
                put("examiner", "C")
                // Add students to the exam as a JSONArray
                put("students", JSONArray().apply {
                    studentsObject.keys().forEach { key ->
                        put(JSONObject().apply {
                            put("anonymousCode", key)
                            put("studentInfo", studentsObject.getJSONObject(key))
                        })
                    }
                })
            })
        }
    }

    fun createSerialization(): List<Answer> {
        return tentaViewModel.getAnswers().map { (questionId, canvasObjects) ->
            Answer(
                questionId = questionId,
                canvasObjects = canvasObjects.map { it.toSerializable() }
            )
        }
    }

    private fun getStorageObjectFromExternal(): JSONObject? {
        //fel hantering behövs, vad om det ej finns en fil
        return externalStorageManager.readFromBackUp(context)
    }

    // check if there is a recoverable exam on external storage for said examID and anonymousCode
    fun continueAlreadyStartedExam(textMeasurer: TextMeasurer): Boolean {
        Log.d("Backup", "Attempting to continue an already started exam")

        val storedObject = getStorageObjectFromExternal()
        if (storedObject == null) {
            Log.d("Backup", "No stored object found")
            return false
        }

        Log.d("Backup", "Stored object retrieved: $storedObject")

        val answersJsonArray = storedObject.optJSONArray("answers")?.toString()
        if (answersJsonArray == null) {
            Log.d("Backup", "No answers JSON array found in stored object")
            return false
        }

        Log.d("Backup", "Answers JSON array extracted: $answersJsonArray")

        return try {
            val deserializedAnswers: List<Answer> = Json.decodeFromString(answersJsonArray)
            Log.d("Backup", "Deserialized answers: $deserializedAnswers")

            // Step 1: Determine the range of questions dynamically
            val allQuestionIds = deserializedAnswers.map { it.questionId }
            val maxQuestionId = allQuestionIds.maxOrNull() ?: 0
            val questionRange = 1..maxQuestionId

            // Step 2: Initialize all questions dynamically as empty lists
            tentaViewModel.questions.clear()
            questionRange.forEach { questionId ->
                tentaViewModel.questions[questionId] = emptyList()
            }
            Log.d("Backup", "Initialized questions dynamically: ${tentaViewModel.questions}")

            // Step 3: Populate questions with recovered objects
            deserializedAnswers.forEach { answer ->
                Log.d("Backup", "Processing answer: $answer")

                val canvasObjects: List<CanvasObject> = answer.canvasObjects.map { serializedObject ->
                    Log.d("Backup", "Processing canvas object: $serializedObject")
                    when (serializedObject) {
                        is SerializableLine -> {
                            Log.d("Backup", "SerializableLine detected: $serializedObject")
                            Line(
                                start = serializedObject.start.toOffset(),
                                end = serializedObject.end.toOffset(),
                                color = Color.Black,
                                strokeWidth = serializedObject.strokeWidth.dp
                            )
                        }
                        is SerializableTextbox -> {
                            Log.d("Backup", "SerializableTextbox detected: $serializedObject")
                            val measuredText = textMeasurer.measure(
                                text = AnnotatedString(serializedObject.text)
                            )
                            TextBox(
                                position = serializedObject.position.toOffset(),
                                text = measuredText
                            )
                        }
                        else -> {
                            Log.e("Backup", "Unknown SerializableCanvasObject type: $serializedObject")
                            throw IllegalArgumentException("Unknown SerializableCanvasObject type")
                        }
                    }
                }

                tentaViewModel.questions[answer.questionId] = canvasObjects
                Log.d("Backup", "Objects for question ${answer.questionId} added successfully")
            }

            Log.d("Backup", "Recovery process completed. Questions: ${tentaViewModel.questions}")
            true // Successfully recovered
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Backup", "Failed to deserialize exam data: ${e.message}")
            false
        }
    }

    fun startBackUp(){
            externalStorageManager.writeToBackUp(context,storageObject)
            startPerodicallyUpdatingExternalStorage(scope)
    }

    private fun startPerodicallyUpdatingExternalStorage(scope: CoroutineScope) {
        scope.launch {
            while (isActive) { // Ensures the coroutine can be canceled
                updateStorageObject()
                delay(30 * 1000L) // 30 second delay
                Log.d("ExamInfo", "{${tentaViewModel.getAnswers()}}")
            }
        }
    }

    private fun updateStorageObject() {
        try {
            // Get the serialized answers as a list of Answer objects
            val serializedAnswers = createSerialization()

            // Convert the serialized answers to JSON format
            val answersJsonString = Json.encodeToString(ListSerializer(Answer.serializer()), serializedAnswers)

            // Update the storageObject with the serialized data
            storageObject = JSONObject().apply {
                put("examID", examID)
                put("anonymousCode", anonymousCode)
                put("answers", JSONArray(answersJsonString)) // Convert the JSON string into a JSONArray
            }

            // Write the updated storageObject to external storage
            externalStorageManager.writeToBackUp(context, storageObject)

            Log.d("ExamInfo", "Backup successfully updated!")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ExamError", "Failed to update backup: ${e.message}")
        }
    }

    fun verifyBackupCredentials(aCode: String, exId: String): Boolean {
        Log.d("Backup", "Verifying backup credentials for anonymousCode: $aCode and examID: $exId")

        // Step 1: Check if a backup file exists
        if (!externalStorageManager.backUpExists(context)) {
            Log.d("Backup", "No backup file exists")
            return false
        }

        // Step 2: Retrieve the stored backup object
        val storedObject = getStorageObjectFromExternal() ?: return false
        Log.d("Backup", "Retrieved stored object: $storedObject")

        // Step 3: Extract and verify credentials
        val storedExamID: String = storedObject.optString("examID", "")
        val storedAnonymousCode: String = storedObject.optString("anonymousCode", "")

        Log.d("Backup", "Stored examID: $storedExamID, Stored anonymousCode: $storedAnonymousCode")

        val credentialsMatch = storedExamID == exId && storedAnonymousCode == aCode
        if (credentialsMatch) {
            Log.d("Backup", "Credentials match!")
        } else {
            Log.d("Backup", "Credentials do not match")
        }

        return credentialsMatch
    }


    // checks if the anonymousCode and examID entered is valid
// (+ temporarily does the creation of a studentObject and storageObject, this will not be here later on)
 fun loginCheck(aCode: String, exId: String): Boolean {
        val examObj = examObject.optJSONObject(exId) ?: return false

        val studentsArray = examObj.optJSONArray("students") ?: return false

        for (i in 0 until studentsArray.length()) {
            val student = studentsArray.getJSONObject(i)
            if (student.getString("anonymousCode") == aCode) {

                val studentInfo = student.optJSONObject("studentInfo")
                if (studentInfo != null) {
                    personalNumber = studentInfo.optString("personalNumber", "")
                    name = studentInfo.optString("name", "")
                    examID = exId
                    anonymousCode = aCode

                    createSerialization()

                    return true
                }
            }
        }
        return false
    }
}