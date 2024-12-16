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
    fun alreadyStartedExamExist() : Boolean{
        if(externalStorageManager.backUpExists(context) )
        {
            val storedObject: JSONObject = getStorageObjectFromExternal()?:return false

            val storedAnonymousCode: String = storedObject.optJSONObject("studentInfo")?.optString("anonymousCode","") ?: return false

            val storedExamID: String = storedObject.optString("examID","")

            return storedExamID == examID && storedAnonymousCode == anonymousCode
        }
      return false
    }
    //saves the answers already made to the question map. Returns true if all was successful otherwise false.
    fun continueAlreadyStartedExam(textMeasurer: TextMeasurer): Boolean {
        Log.d("Backup", "Starting recovery process...")

        val storedObject = getStorageObjectFromExternal()
        if (storedObject == null) {
            Log.e("Backup", "No stored object found in external storage.")
            return false
        } else {
            Log.d("Backup", "Stored object retrieved successfully: $storedObject")
        }

        // Extract "answers" as a JSON array string
        val answersJsonArray = storedObject.optJSONArray("answers")?.toString()
        if (answersJsonArray == null) {
            Log.e("Backup", "No 'answers' found in stored object.")
            return false
        } else {
            Log.d("Backup", "Answers JSON Array: $answersJsonArray")
        }

        return try {
            // Deserialize the JSON string back into a List<Answer>
            Log.d("Backup", "Deserializing answers JSON...")
            val deserializedAnswers: List<Answer> = Json.decodeFromString(answersJsonArray)
            Log.d("Backup", "Deserialization successful. Total answers: ${deserializedAnswers.size}")

            // Convert the deserialized Answers into TentaViewModel's questions map
            val questionsMap = deserializedAnswers.associate { answer ->
                Log.d("Backup", "Processing questionId: ${answer.questionId}")
                val canvasObjects: List<CanvasObject> = answer.canvasObjects.map { serializedObject ->
                    when (serializedObject) {
                        is SerializableLine -> {
                            Log.d(
                                "Backup",
                                "Restoring SerializableLine: start=${serializedObject.start}, end=${serializedObject.end}"
                            )
                            Line(
                                start = serializedObject.start.toOffset(),
                                end = serializedObject.end.toOffset(),
                                color = Color.Black, // Default color or extract from serialized data
                                strokeWidth = serializedObject.strokeWidth.dp
                            )
                        }
                        is SerializableTextbox -> {
                            Log.d(
                                "Backup",
                                "Restoring SerializableTextbox: position=${serializedObject.position}, text=${serializedObject.text}"
                            )
                            val measuredText = textMeasurer.measure(
                                text = AnnotatedString(serializedObject.text)
                            )
                            Log.d("Backup", "Measured TextLayoutResult created for text=${serializedObject.text}")
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
                Log.d("Backup", "Restored ${canvasObjects.size} objects for questionId=${answer.questionId}")
                answer.questionId to canvasObjects
            }

            // Assign to TentaViewModel's questions
            tentaViewModel.questions = mutableStateMapOf<Int, List<CanvasObject>>().apply {
                putAll(questionsMap)
            }
            Log.d("Backup", "Recovery process completed. Total questions restored: ${tentaViewModel.questions.size}")

            true // Successfully recovered
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Backup", "Failed to deserialize exam data: ${e.message}")
            false
        }
    }


    fun testContinue() {
        val storedObject = getStorageObjectFromExternal()
        storedObject as JSONObject
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