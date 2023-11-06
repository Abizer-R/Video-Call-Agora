package com.example.teachjr.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class RvProfCourseListItem(
    val courseCode: String = "",
    val courseName: String = "",
    val sem_sec: String = ""
)
