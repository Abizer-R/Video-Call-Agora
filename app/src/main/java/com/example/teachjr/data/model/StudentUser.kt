package com.example.teachjr.data.model

data class StudentUser constructor(
    val institute: String? = "",
    val name: String? = "",
    val enrollment: String? = "",
    val email: String? = "",
    val userType: String? = "",
    val branch: String? = "",
    val section: String? = "",
    val semester: Int? = null,
    val sem_sec: String? = "",
)