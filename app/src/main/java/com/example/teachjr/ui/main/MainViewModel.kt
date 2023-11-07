package com.example.teachjr.ui.main

import android.telecom.Call
import android.util.Log
import androidx.lifecycle.*
import com.example.teachjr.data.auth.model.CallStatus
import com.example.teachjr.data.auth.model.FriendButtonType
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

    private val _usersList = MutableLiveData<Response<List<FriendsListItem>>>()
    val usersList: LiveData<Response<List<FriendsListItem>>>
        get() = _usersList

    private val _requestMap = MutableLiveData<Map<String, String>>(mapOf())
    val requestMap: LiveData<Map<String, String>>
        get() = _requestMap

    private val _pendingRequestList = MutableLiveData<List<FriendsListItem>>(emptyList())
    val pendingRequestList: LiveData<List<FriendsListItem>>
        get() = _pendingRequestList


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
            val friendsListDeferred = async { mainRepository.getFriendsList() }
            _friendsList.postValue(friendsListDeferred.await())
        }
    }

    fun getUsersList() {
        _usersList.postValue(Response.Loading())
        Log.i(TAG, "StdTesting-ViewModel: Calling getCourselist")
        viewModelScope.launch {
            val usersListResponse = mainRepository.getUsersList()
            when (usersListResponse) {
                is Response.Success -> {
                    val usersList = usersListResponse.data
                    usersList?.let { list ->
                        val newList = ArrayList(list)
                        val iterator = newList.iterator()
                        while (iterator.hasNext()) {
                            val user = iterator.next()
                            if(user.uuid == mainRepository.currentUser.uid) {
                                iterator.remove()
                                continue
                            }

                            val idx = friendsList.value?.data?.indexOfFirst {
                                it.uuid == user.uuid
                            } ?: -1
                            if(idx >= 0 && idx < (friendsList.value?.data?.size ?: 0)) {
                                iterator.remove()
                            }
                        }
                        Log.e("TESTING2", "getUsersList: newList = ${newList}", )
                        _usersList.postValue(Response.Success(newList))

                    } ?: _usersList.postValue(Response.Error("null data", null))
                }

                else -> _usersList.postValue(usersListResponse)
            }
        }
    }

    fun getFilteredUsers(query: String): List<FriendsListItem> {
        val filteredList = mutableListOf<FriendsListItem>()
        if(usersList.value is Response.Success) {
            usersList.value?.data?.forEach {
                if(it.email.lowercase().contains(query.lowercase()) ||
                    it.name.lowercase().contains(query.lowercase())) {
                    filteredList.add(it)
                }
            }
        }
        return filteredList
    }

    fun sendFriendRequest(userUuid: String) = viewModelScope.launch{
        mainRepository.sendFriendRequest(userUuid)
    }

    fun cancelFriendRequest(userUuid: String) = viewModelScope.launch{
        mainRepository.cancelFriendRequest(userUuid)
    }

    fun acceptFriendRequest(friend: FriendsListItem, currUsername: String) = viewModelScope.launch{
        mainRepository.addFriend(friend, currUsername)
        mainRepository.cancelFriendRequest(friend.uuid)
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

    fun observeFriendRequests() = viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) {
                mainRepository.observeFriendRequests()
                    .collect {responsePair ->
                        val prevMap = mutableMapOf<String, String>()
                        prevMap.putAll(_requestMap.value!!)

                        if(responsePair.first) {
                            prevMap.putAll(responsePair.second)
                        } else {
                            responsePair.second.keys.forEach {
                                prevMap.remove(it)
                            }
                        }
                        Log.e("TESTING3", "observeFriendRequests: prevMap = $prevMap", )
                        _requestMap.postValue(prevMap)
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

    fun updatePendingRequests() {
        val requestsList = mutableListOf<FriendsListItem>()

        requestMap.value!!.forEach { mapItem ->
            val userUuid = mapItem.key
            val user = usersList.value!!.data!!.find {
                it.uuid == userUuid
            }
            if(user != null) {
                Log.e("TESTING3", "getPendingRequests: user = $user", )
                val buttonType = when(mapItem.value) {
                    FirebasePaths.FRIENDS_STATUS_REQUEST_SENT -> FriendButtonType.REQUEST_SENT
                    FirebasePaths.FRIENDS_STATUS_REQUEST_RECEIVED -> FriendButtonType.REQUEST_RECEIVED
                    else -> FriendButtonType.NO_BUTTON
                }
                requestsList.add(
                    FriendsListItem(
                        uuid = user.uuid,
                        name = user.name,
                        email = user.email,
                        btnType = buttonType
                    )
                )
            }
        }

        _pendingRequestList.postValue(requestsList)
    }

}