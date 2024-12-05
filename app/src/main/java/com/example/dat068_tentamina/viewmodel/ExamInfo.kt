package com.example.dat068_tentamina.viewmodel

import android.content.Context
import android.util.Log
import com.example.dat068_tentamina.externalStorage.ExternalStorageManager
import com.example.dat068_tentamina.model.CanvasObject
import org.json.JSONArray
import org.json.JSONObject
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
    private fun getStorageObjectFromExternal():JSONObject?{
        //fel hantering behövs, vad om det ej finns en fil
        return externalStorageManager.readFromBackUp(context)

    }

    fun alreadyStartedExam() : Boolean{
        if(externalStorageManager.backUpExists(context))
        {
            //fel hantering krävs, för den kan ju innehålla ngt annat

            val storedObject  = getStorageObjectFromExternal()

            if (storedObject== null){return false}

            val storedAnonymousCode = storedObject.get("anonymousCode")
            val storedExamID = storedObject.get("examID")

            return storedExamID == examID && storedAnonymousCode == anonymousCode
        }
      return false
    }

    fun continueAlreadyStartedExam(){

        tentaViewModel.questions = getStorageObjectFromExternal()?.get("answers") as MutableMap<Int, List<CanvasObject>>
    }
// checks if the anonymousCode and examID entered is valid
// (+ temporarily does the creation of a studentObject and storageObject, this will not be here later on)
    fun loginCheck(aCode: String, exId: String): Boolean {
        val examObj = examObject.optJSONObject(exId) ?: return false

        val studentsArray = examObj.optJSONArray("students") ?: return false

        for (i in 0 until studentsArray.length()) {
            val student = studentsArray.getJSONObject(i)
            if (student.getString("anonymousCode") == aCode) {
                Log.d("examinfo", exId)
                Log.d("examinfo", aCode)

                val studentInfo = student.optJSONObject("studentInfo")
                if (studentInfo != null) {
                    personalNumber = studentInfo.optString("personalNumber", "")
                    name = studentInfo.optString("name", "")
                    examID = exId
                    anonymousCode = aCode

                    Log.d("examinfo", name)
                    Log.d("examinfo", anonymousCode)
                    Log.d("examinfo", personalNumber)

                    createStudentObject()
                    createStorageJSON()
                    return true
                }
            }
        }
        return false
    }
}