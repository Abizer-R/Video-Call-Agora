package com.example.teachjr.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class RvStdCourseListItem(
    val courseCode: String = "",
    val courseName: String = "",
    val profName: String = ""
)
