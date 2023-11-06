package com.example.teachjr.ui.viewmodels.professorViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.ProfessorUser
import com.example.teachjr.data.model.RvProfCourseListItem
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class ProfHomeViewModel
    @Inject constructor(
    private val profRepository: ProfRepository
): ViewModel() {

    private val TAG = ProfHomeViewModel::class.java.simpleName

    private val _currUserProf = MutableLiveData<Response<ProfessorUser>>()
    val currUserProf: LiveData<Response<ProfessorUser>>
        get() = _currUserProf

    fun getUser() {
        _currUserProf.postValue(Response.Loading())
        viewModelScope.launch {
            _currUserProf.postValue(profRepository.getUserDetails())
        }
    }

    private val _courseList = MutableLiveData<Response<List<RvProfCourseListItem>>>()
    val courseList: LiveData<Response<List<RvProfCourseListItem>>>
        get() = _courseList

    fun getCourseList() {
        Log.i(TAG, "ProfTesting-ViewModel: Calling getCourselist")
        _courseList.postValue(Response.Loading())
        viewModelScope.launch {
            val courseListDeferred = async { profRepository.getCourseList() }
            _courseList.postValue(courseListDeferred.await())
        }
    }

}