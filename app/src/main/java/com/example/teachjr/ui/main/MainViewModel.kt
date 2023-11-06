package com.example.teachjr.ui.main

import android.telecom.Call
import android.util.Log
import androidx.lifecycle.*
import com.example.teachjr.data.auth.model.CallStatus
import com.example.teachjr.data.auth.model.FriendsListItem
import com.example.teachjr.data.auth.model.UserModel
import com.example.teachjr.data.main.repo.MainRepository
import com.example.teachjr.data.model.ProfessorUser
import com.example.teachjr.data.model.RvProfMarkAtdListItem
import com.example.teachjr.data.model.RvStdCourseListItem
import com.example.teachjr.data.model.StudentUser
import com.example.teachjr.data.source.repository.StudentRepository
import com.example.teachjr.utils.Constants
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.sealedClasses.AttendanceStatusProf
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject constructor(
        private val mainRepository: MainRepository
): ViewModel() {

    private val TAG = MainViewModel::class.java.simpleName

    var otherUid: String? = null

    private val _friendsList = MutableLiveData<Response<List<FriendsListItem>>>()
    val friendsList: LiveData<Response<List<FriendsListItem>>>
        get() = _friendsList


    private val _callStatus = MutableLiveData<CallStatus>()
    val callStatus: LiveData<CallStatus>
        get() = _callStatus


    private val _outgoingCallStatus = MutableLiveData<Response<String>>()
    val outgoingCallStatus: LiveData<Response<String>>
        get() = _outgoingCallStatus

    private val _userDetails = MutableLiveData<Response<UserModel>>()
    val userDetails: LiveData<Response<UserModel>>
        get() = _userDetails

    fun getUserDetails() {
        _userDetails.postValue(Response.Loading())
        viewModelScope.launch {
            _userDetails.postValue(mainRepository.getUserDetails())
        }
    }

    fun getFriendsList() {
        _friendsList.postValue(Response.Loading())
        Log.i(TAG, "StdTesting-ViewModel: Calling getCourselist")
        viewModelScope.launch {
            val courseListDeferred = async { mainRepository.getFriendsList() }
            _friendsList.postValue(courseListDeferred.await())
        }
    }

    /**
     * To understand why we are using 'suspend fun',
     * read the note in ProfMarkAtdFrag (in setupObserver())
     */
    fun observeCallStatus() = viewModelScope.launch {
        var currCallStatus = CallStatus(null, null)
        try {
            withContext(Dispatchers.IO) {
                mainRepository.observeCallStatus()
                    .collect {
                        val newVal = CallStatus(
                            otherUserUuid = it.otherUserUuid ?: currCallStatus.otherUserUuid,
                            otherUserEmail = it.otherUserEmail ?: currCallStatus.otherUserEmail,
                            otherUserName = it.otherUserName ?: currCallStatus.otherUserName,
                            status = it.status ?: currCallStatus.status
                        )
                        currCallStatus = newVal
                        Log.e("TESTING", "observeCallStatus: newVal = $newVal", )
                        _callStatus.postValue(newVal)
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pushCallNotification(uuid: String, callStatus: CallStatus) {
        viewModelScope.launch {
            val result = mainRepository.pushCallNotification(uuid, callStatus)
            if(result == FirebaseConstants.STATUS_SUCCESSFUL && callStatus.status == FirebasePaths.CALL_STATUS_INCOMING_REQUEST) {
                _outgoingCallStatus.postValue(Response.Success(result))
            } else {
                _outgoingCallStatus.postValue(Response.Error(result, null))
            }
        }
    }

}