package com.example.teachjr.ui.viewmodels.studentViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.StdAttendanceDetails
import com.example.teachjr.data.source.repository.StudentRepository
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StdCourseViewModel
    @Inject constructor(
        private val studentRepository: StudentRepository
    ): ViewModel() {

    private val TAG = StdCourseViewModel::class.java.simpleName

    private val _atdDetails = MutableLiveData<Response<StdAttendanceDetails>>()
    val atdDetails: LiveData<Response<StdAttendanceDetails>>
        get() = _atdDetails

    private var _toBeShownFAB = false
    val toBeShownFAB: Boolean
        get() = _toBeShownFAB

    fun setFABVisibility(isVisibile: Boolean) {
        _toBeShownFAB = true
    }

    fun getAttendanceDetails(courseCode: String, sem_sem: String) {
        _atdDetails.postValue(Response.Loading())
        Log.i(TAG, "StdTesting: Calling getAttendanceDetails")
        viewModelScope.launch {
            _atdDetails.postValue(studentRepository.getAttendanceDetails(sem_sem, courseCode))
        }
    }
}