package com.example.teachjr.ui.viewmodels.studentViewModels

import android.util.Log
import androidx.lifecycle.*
import com.example.teachjr.data.model.RvStdCourseListItem
import com.example.teachjr.data.model.StudentUser
import com.example.teachjr.data.source.repository.StudentRepository
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class StdHomeViewModel
    @Inject constructor(
        private val studentRepository: StudentRepository
): ViewModel() {

    private val TAG = StdHomeViewModel::class.java.simpleName

    private val _currUserStd = MutableLiveData<Response<StudentUser>>()
    val currUserStd: LiveData<Response<StudentUser>>
        get() = _currUserStd

    fun getUser() {
        _currUserStd.postValue(Response.Loading())
        viewModelScope.launch {
            _currUserStd.postValue(studentRepository.getUserDetails())
        }
    }

    private val _courseList = MutableLiveData<Response<List<RvStdCourseListItem>>>()
    val courseList: LiveData<Response<List<RvStdCourseListItem>>>
        get() = _courseList

    fun getCourseList(institute: String, branch: String, sem_sec: String) {
        _courseList.postValue(Response.Loading())
        Log.i(TAG, "StdTesting-ViewModel: Calling getCourselist")
        viewModelScope.launch {
            val courseListDeferred = async { studentRepository.getCourseList(institute, branch, sem_sec) }
            _courseList.postValue(courseListDeferred.await())
        }
    }

}