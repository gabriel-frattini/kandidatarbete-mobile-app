package com.example.dat068_tentamina.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.example.dat068_tentamina.externalStorage.ExternalStorageManager
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.ui.continueOldExamPopUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors

class ExamInfo(tV: TentaViewModel, exManager : ExternalStorageManager, context: Context) {
    var examObject = JSONObject()
    var studentsObject = JSONObject()
    var storageObject = JSONObject()
    var studentObject = JSONObject()
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

    private fun createStudentObject(){
        studentObject = JSONObject().apply {
            put("anonymousCode",anonymousCode)
            put("name", name)
            put("personalNumber",personalNumber )
        }

    }
    private fun createStorageJSON(){
        storageObject = JSONObject().apply {
            put("examID",examID)
            put("studentInfo", studentObject)
            put("answers",tentaViewModel.getAnswers() )
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
                // Validate and convert to the correct type
                val validatedAnswers = answers.mapNotNull { (key, value) ->
                    if (key is Int && value is List<*>) {
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


                    createStudentObject()
                    createStorageJSON()
                    return true
                }
            }
        }
        return false
    }
}