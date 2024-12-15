package com.example.dat068_tentamina.model.serializable

import kotlinx.serialization.Serializable


@Serializable
data class StudentInfo(
    val anonymousCode: String,
    val name: String,
    val personalNumber: String
)