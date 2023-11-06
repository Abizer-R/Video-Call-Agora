package com.example.teachjr.data.model

data class StdAttendanceDetails(
    val totalLecCount: Int = 0,
    val presentLecCount: Int = 0,
    val absentLecCount: Int = 0,
    val lecList: List<RvStdLecListItem> = ArrayList()
)
