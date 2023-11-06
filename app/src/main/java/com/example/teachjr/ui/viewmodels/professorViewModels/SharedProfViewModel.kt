package com.example.teachjr.ui.viewmodels.professorViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.teachjr.data.model.ProfessorUser
import com.example.teachjr.data.model.RvProfCourseListItem
import com.example.teachjr.data.model.RvStdCourseListItem
import com.example.teachjr.data.model.StudentUser
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.data.source.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedProfViewModel
@Inject constructor(
    private val profRepository: ProfRepository
): ViewModel() {

    private val TAG = SharedProfViewModel::class.java.simpleName

    private var _userDetails: ProfessorUser? = null
    val userDetails: ProfessorUser?
        get() = _userDetails

    fun setUserDetails(stdDetails: ProfessorUser) {
        _userDetails = stdDetails
    }

    private var _courseList: List<RvProfCourseListItem>? = null
    val courseList: List<RvProfCourseListItem>?
        get() = _courseList

    fun setCourseList(list: List<RvProfCourseListItem>) {
        _courseList = list
    }

    private var _courseCode: String? = null
    val courseCode: String?
        get() = _courseCode

    private var _courseName: String? = null
    val courseName: String?
        get() = _courseName

    private var _sem_sec: String? = null
    val sem_sec: String?
        get() = _sem_sec

    private var _lecCount: Int? = null
    val lecCount: Int?
        get() = _lecCount


    fun updateCourseDetails(courseCode: String, courseName: String, sem_sec: String) {
        _courseCode = courseCode
        _courseName = courseName
        _sem_sec = sem_sec
    }

    fun courseValuesNotNull(): Boolean {
        if(_courseCode != null && _courseName != null && _sem_sec != null) {
            return true
        } else {
            Log.i(TAG, "ProfessorTesting: null values. courseCode=$_courseCode, courseName-$_courseName, sem_sec-$_sem_sec")
            return false
        }
    }

    fun lecCountNotNull(): Boolean {
        if(_lecCount != null) {
            return true
        } else {
            Log.i(TAG, "ProfessorTesting: null values. lecCount=$_lecCount")
            return false
        }
    }

    fun updateLecCount(lecCount: Int) {
        _lecCount = lecCount
    }

    fun clearValues() {
        _courseCode = null
        _courseName = null
        _sem_sec = null
        _lecCount = null
    }

    fun logout() {
        profRepository.logout()
    }
}