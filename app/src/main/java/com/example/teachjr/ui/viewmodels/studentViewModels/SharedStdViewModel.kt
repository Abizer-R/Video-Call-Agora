package com.example.teachjr.ui.viewmodels.studentViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.teachjr.data.model.RvStdCourseListItem
import com.example.teachjr.data.model.StudentUser
import com.example.teachjr.data.source.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedStdViewModel
@Inject constructor(
    private val studentRepository: StudentRepository
): ViewModel() {

    private val TAG = SharedStdViewModel::class.java.simpleName

    private var _userDetails: StudentUser? = null
    val userDetails: StudentUser?
        get() = _userDetails

    fun setUserDetails(stdDetails: StudentUser) {
        _userDetails = stdDetails
    }

    private var _courseList: List<RvStdCourseListItem>? = null
    val courseList: List<RvStdCourseListItem>?
        get() = _courseList

    fun setCourseList(list: List<RvStdCourseListItem>) {
        _courseList = list
    }

    private var _courseCode: String? = null
    val courseCode: String?
        get() = _courseCode

    private var _courseName: String? = null
    val courseName: String?
        get() = _courseName

    private var _profName: String? = null
    val profName: String?
        get() = _profName


    fun updateCourseDetails(courseCode: String, courseName: String, profName: String) {
        _courseCode = courseCode
        _courseName = courseName
        _profName = profName
    }

    fun courseValuesNotNull(): Boolean {
        if(_courseCode != null && _courseName != null && _profName != null) {
            return true
        } else {
            Log.i(TAG, "StudentTesting: null values. courseCode=$_courseCode, courseName-$_courseName, profName-$_profName")
            return false
        }
    }


    fun clearCourseValues() {
        _courseCode = null
        _courseName = null
        _profName = null
    }

    fun logout() {
        studentRepository.logout()
    }
}