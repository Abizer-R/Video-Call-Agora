package com.example.teachjr.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.auth.model.UserModel
import com.example.teachjr.data.auth.repo.AuthRepositoryImpl
import com.example.teachjr.utils.sealedClasses.UserType
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.sealedClasses.Response
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject constructor(
    private val authRepository: AuthRepositoryImpl
): ViewModel() {

    private val _userType = MutableLiveData<UserType?>(UserType.Student())
    val userType: LiveData<UserType?>
        get() = _userType

    private val _loginStatus = MutableLiveData<Response<FirebaseUser>>()
    val loginStatus: LiveData<Response<FirebaseUser>>
        get() = _loginStatus


    fun setUserType(userString: String) {
        if(userString.equals(FirebaseConstants.TYPE_STUDENT)) {
            _userType.postValue(UserType.Student())
        } else if(userString.equals(FirebaseConstants.TYPE_PROFESSOR)){
            _userType.postValue(UserType.Teacher())
        }
    }

    fun createUser(username: String, email: String) {
        _loginStatus.value = Response.Loading()
        viewModelScope.launch {
            val result = authRepository.createUser(UserModel(username, email))
            if(result == FirebaseConstants.STATUS_SUCCESSFUL) {
                _loginStatus.postValue(Response.Success(authRepository.currUser))
            } else {
                _loginStatus.postValue(Response.Error(result, null))
            }
        }
    }

    fun signInUser(email: String, password: String) {
        _loginStatus.value = Response.Loading()
        viewModelScope.launch {
            val result = async { authRepository.login(email, password) }
            when(val response = result.await()) {
                is Response.Error -> {
                    _loginStatus.postValue(Response.Error(response.errorMessage.toString(), null))
                }
                is Response.Loading -> {}
                is Response.Success -> {
                    _loginStatus.postValue(Response.Success(authRepository.currUser))
                }
            }
        }
    }

    fun loginProfessor(email: String, password: String) {
        _loginStatus.value = Response.Loading()
        viewModelScope.launch {
            val result = async { authRepository.login(email, password) }
            when(val response = result.await()) {
                is Response.Error -> {
                    _loginStatus.postValue(Response.Error(response.errorMessage.toString(), null))
                }
                is Response.Loading -> {}
                is Response.Success -> {
                    // Verifies if user is professor and updates _loginStatus
//                    verifyProfAndUpdate()
                    _loginStatus.postValue(Response.Success(authRepository.currUser))
                }
            }
        }
    }

    fun loginStudent(email: String, enrollment: String, password: String) {
        _loginStatus.value = Response.Loading()
        viewModelScope.launch {
            val result = async { authRepository.login(email, password) }
            when(val response = result.await()) {
                is Response.Error -> {
                    _loginStatus.postValue(Response.Error(response.errorMessage.toString(), null))
                }
                is Response.Loading -> {}
                is Response.Success -> {
                    // Verifies if user is student and updates _loginStatus
                    verifyStdAndUpdate(enrollment)
                }
            }
        }
    }

    private suspend fun verifyProfAndUpdate() {
        withContext(Dispatchers.IO) {
            val userType = async { authRepository.getUserType() }
            when(val typeResponse = userType.await()) {
                is Response.Error -> _loginStatus.postValue(Response.Error(typeResponse.errorMessage.toString(), null))
                is Response.Loading -> {}
                is Response.Success -> {
                    if(typeResponse.data.equals(FirebaseConstants.TYPE_PROFESSOR)) {
                        _loginStatus.postValue(Response.Success(authRepository.currUser))
                    } else {
                        _loginStatus.postValue(Response.Error("These credentials does not belong to a professor", null))
                    }
                }
            }
        }
    }

    private suspend fun verifyStdAndUpdate(enrollment: String) {
        withContext(Dispatchers.IO) {
            val userType = async { authRepository.getUserType() }
            when(val typeResponse = userType.await()) {
                is Response.Error -> _loginStatus.postValue(Response.Error(typeResponse.errorMessage.toString(), null))
                is Response.Loading -> {}
                is Response.Success -> {
                    if(typeResponse.data.equals(FirebaseConstants.TYPE_STUDENT)) {

                        val validEnrollment = async { authRepository.checkEnrollment(enrollment) }
                        when(val isValid = validEnrollment.await()) {
                            is Response.Loading -> {}
                            is Response.Error -> _loginStatus.postValue(Response.Error(typeResponse.errorMessage.toString(), null))
                            is Response.Success -> {
                                if(isValid.data!!) {
                                    _loginStatus.postValue(Response.Success(authRepository.currUser))
                                } else {
                                    _loginStatus.postValue(Response.Error("Invalid Enrollment Number", null))
                                }
                            }
                        }
                    } else {
                        _loginStatus.postValue(Response.Error("These credentials does not belong to a student", null))
                    }
                }
            }
        }
    }

}