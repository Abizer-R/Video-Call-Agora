package com.example.teachjr.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.source.repository.SplashRepository
import com.example.teachjr.utils.sealedClasses.Response
import com.example.teachjr.utils.sealedClasses.UserType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject constructor(
    private val splashRepository: SplashRepository
): ViewModel() {

    private val _userType = MutableLiveData<Response<UserType?>>()
    val userType: LiveData<Response<UserType?>>
        get() = _userType

    init {
        getUserType()
    }

    private fun getUserType() {
        if(splashRepository.currUser != null) {
            viewModelScope.launch {
                val userTypeResponse = async { splashRepository.getUserType() }
                val userType = userTypeResponse.await()
                _userType.postValue(userType)
            }
        } else {
            _userType.postValue(Response.Success(null))
        }
    }
}