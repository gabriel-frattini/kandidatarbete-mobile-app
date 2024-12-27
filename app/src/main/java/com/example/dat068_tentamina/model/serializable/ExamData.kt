package com.example.dat068_tentamina.model.serializable

import kotlinx.serialization.Serializable

@Serializable
data class ExamData(
    val examID: String,
    val studentInfo: StudentInfo,
    val answers: List<Answer>
)