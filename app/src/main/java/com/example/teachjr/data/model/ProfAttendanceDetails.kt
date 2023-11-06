package com.example.teachjr.data.model

data class ProfAttendanceDetails(
    val studentList: List<String> = ArrayList(),
    val lectureList: List<Lecture> = ArrayList()
)