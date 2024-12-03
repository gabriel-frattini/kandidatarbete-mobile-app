package com.example.dat068_tentamina.viewmodel

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
class ExamInfo {
    var examObject = JSONObject()
    var studentsObject = JSONObject()
    private var anonymousCode = ""
    private var examID = ""
    private var personalNumber = ""
    private var name = ""

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


                    return true
                }
            }
        }
        return false
    }
}