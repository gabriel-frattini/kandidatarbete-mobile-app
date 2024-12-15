package com.example.dat068_tentamina.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.example.dat068_tentamina.externalStorage.ExternalStorageManager
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.serializable.Answer
import com.example.dat068_tentamina.model.serializable.StudentInfo
import com.example.dat068_tentamina.model.serializable.SerializableTextbox
import com.example.dat068_tentamina.model.serializable.SerializableLine
import com.example.dat068_tentamina.utils.CanvasObjectSerializationUtils.toSerializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import toSerializable

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
                canvasObjects = canvasObjects.map { canvasObject ->
                    canvasObject.toSerializable() // Use a helper extension to serialize CanvasObjects
                }
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

fun continueAlreadyStartedExam(): Boolean {
    val storedObject = getStorageObjectFromExternal()

    tentaViewModel.questions = if (storedObject != null) {
        val answers = storedObject.opt("answers")

        if (answers is Map<*, *>) {
            try {
                val validatedAnswers = answers.mapNotNull { (key, value) ->
                    if (key is Int && value is List<*>) {
                        Log.d("ExamInfo", "Key: $key, Value: $value")
                        @Suppress("UNCHECKED_CAST")
                        key to (value as List<CanvasObject>)
                    } else {
                        Log.e("ExamError", "Invalid entry in answers: $key -> $value")
                        null
                    }
                }.toMap()

                // Convert validated map to mutableStateMapOf
                val mapAnswers = mutableStateMapOf<Int, List<CanvasObject>>()
                mapAnswers.putAll(validatedAnswers)

                // Assign validated map to ViewModel
                mapAnswers
            } catch (e: ClassCastException) {
                e.printStackTrace()
                Log.e("ExamError", "Invalid type in answers. Starting new exam.")
                mutableStateMapOf() // Fallback to an empty map
            }
        } else {
            Log.e("ExamError", "Answers is not a valid map. Using empty map.")
            mutableStateMapOf() // Fallback to an empty map
        }
    } else {
        Log.e("ExamError", "No stored object available from external storage. Using empty map.")
        mutableStateMapOf() // Fallback to an empty map
    }

    // Return true if questions are successfully loaded
    return tentaViewModel.questions.isNotEmpty()


    }
    fun testContinue() {
        val storedObject = getStorageObjectFromExternal()
        storedObject as JSONObject
        Log.d("ExamInfo", "$storedObject")
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

    private fun updateStorageObject(){
        storageObject.put("answers",tentaViewModel.getAnswers())
        externalStorageManager.writeToBackUp(context,storageObject)
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