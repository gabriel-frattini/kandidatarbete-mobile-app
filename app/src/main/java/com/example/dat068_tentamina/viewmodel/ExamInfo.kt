package com.example.dat068_tentamina.viewmodel

import org.json.JSONArray
import org.json.JSONObject
class ExamInfo {
    var examObject = JSONObject()
    var studentsObject = JSONObject()
    private var  anonymousCode = ""
    private var examID = ""
    var personalNumber = ""

    fun createTestExamPeriodJSON(){

        studentsObject = JSONObject().apply {
            put("123ALI", JSONObject().apply {
                put("personalNumber", "0309230000")
                put("Name","Alice Emanuelsson")
            })
            put("123CHE", JSONObject().apply {
                put("personalNumber", "0204120000")
                put("Name","Che Long Tran")
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
    fun loginCheck(aCode: String, exId : String) : Boolean {

        val examObj = examObject.optJSONObject(exId) ?: return false

        // Get the students array from the exam object
        val studentsArray = examObj.optJSONArray("students") ?: return false

        // Iterate through the students array and check for the anonymous code
        for (i in 0 until studentsArray.length()) {
            val student = studentsArray.getJSONObject(i)
            if (student.getString("anonymousCode") == aCode) {
                examID = exId
                anonymousCode = aCode
                personalNumber = examObj.optString("personalNumber") ?: ""
                //personalNumber = student.getString(personalNumber)
                return true // Found the anonymous code in the students list
            }
        }
        return false
    }
}