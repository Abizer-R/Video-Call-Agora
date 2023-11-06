package com.example.teachjr.ui.viewmodels.professorViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfCourseViewModel
    @Inject constructor(
    private val profRepository: ProfRepository
): ViewModel() {

    private val TAG = ProfCourseViewModel::class.java.simpleName

    private val _lecCount = MutableLiveData<Response<Int>>()
    val lecCount: LiveData<Response<Int>>
        get() = _lecCount


    fun getLectureCount(sem_sec: String, courseCode: String) {
        _lecCount.postValue(Response.Loading())
        viewModelScope.launch {
            val lecCountDeferred = async { profRepository.getLectureCount(sem_sec, courseCode) }
            _lecCount.postValue(lecCountDeferred.await())
        }
    }
}