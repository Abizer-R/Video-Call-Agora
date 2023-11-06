package com.example.teachjr.ui.viewmodels.professorViewModels

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.ProfAttendanceDetails
import com.example.teachjr.data.model.RvProfMarkAtdListItem
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.utils.Constants
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.sealedClasses.AttendanceStatusProf
import com.example.teachjr.utils.sealedClasses.Response
import com.example.teachjr.utils.WifiSD.BroadcastService
import com.example.teachjr.utils.sealedClasses.AttendanceStatusStd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.log

@HiltViewModel
class ProfMarkAtdViewModel
    @Inject constructor(
    private val profRepository: ProfRepository
): ViewModel() {

    private val TAG = ProfMarkAtdViewModel::class.java.simpleName

    private var _isAtdOngoing = false
    val isAtdOngoing: Boolean
        get() = _isAtdOngoing
    fun updateIsAtdOngoing(newState: Boolean) {_isAtdOngoing = newState}

    private var _isEditing = false
    val isEditing: Boolean
        get() = _isEditing
    fun updateIsEditing(newState: Boolean) {_isEditing = newState}


    private var _saveManualStatus = MutableLiveData<Response<Boolean>>()
    val saveManualStatus: LiveData<Response<Boolean>>
        get() = _saveManualStatus

    private var _presentCount = 0
    val presentCount: Int
        get() = _presentCount

    private var isServiceBroadcasting = false

    private val _atdStatus = MutableLiveData<AttendanceStatusProf>()
    val atdStatus: LiveData<AttendanceStatusProf>
        get() = _atdStatus

    private val _presentList = MutableLiveData<Response<MutableMap<String, RvProfMarkAtdListItem>>>()
    val presentList: LiveData<Response<MutableMap<String, RvProfMarkAtdListItem>>>
        get() = _presentList


    fun initAtd(sem_sec: String, courseCode: String, lecCount: Int) {
        _atdStatus.postValue(AttendanceStatusProf.FetchingTimestamp())
        viewModelScope.launch {
            /**
             * Getting list of all students (Initially all will be absent)
             */
            val stdListResponse = profRepository.getStdListWithUid(sem_sec)
            when(stdListResponse) {
                is Response.Loading -> {}
                is Response.Error -> {
                    _presentList.postValue(Response.Error(stdListResponse.errorMessage.toString(), null))
                    Log.i(TAG, "Prof_testing: stdList error - ${stdListResponse.errorMessage}")
                }
                is Response.Success -> {

                    val stdList: MutableMap<String, RvProfMarkAtdListItem> = mutableMapOf()
                    stdListResponse.data!!.forEach {
                        /**
                         * <String, String> == <Enrollment, Uid>
                         */
                        stdList[it.key] = RvProfMarkAtdListItem(enrollment = it.key, uid = it.value)    // Default: Absent
                    }
                    _presentList.postValue(Response.Success(stdList))

                    /**
                     * Getting timestamp and initiating attendance
                     */
                    val timestamp = Calendar.getInstance().timeInMillis.toString()
                    val newLecDoc = profRepository.createNewAtdRef(sem_sec, courseCode, timestamp, lecCount)
                    when(newLecDoc) {
                        is Response.Loading -> {}
                        is Response.Error -> _atdStatus.postValue(AttendanceStatusProf.Error(newLecDoc.errorMessage.toString()))
                        is Response.Success -> {
                            _atdStatus.postValue(AttendanceStatusProf.Initiated(timestamp))
                        }
                    }
                }
            }
        }
    }

    fun broadcastTimestamp(manager: WifiP2pManager, channel: WifiP2pManager.Channel, serviceInfo: WifiP2pDnsSdServiceInfo) {

        viewModelScope.launch {
            Log.i(TAG, "WIFI_SD_Testing-ViewModel: broadcastTimestamp - Calling broadcastTimestamp()")
            val broadcastResult = BroadcastService.startServiceBroadcast(serviceInfo, manager, channel)
            when(broadcastResult) {
                1 -> { /* Service Added Successfully */
                    Log.i(TAG, "WIFI_SD_Testing-ViewModel: broadcastTimestamp - Service Added Successfully")
                    isServiceBroadcasting = true
                    broadcastServiceInLoop(manager, channel)
                }
                2 -> { /* Failed to add service */
                    Log.i(TAG, "WIFI_SD_Testing-ViewModel: broadcastTimestamp - Failed to add service")
                    _atdStatus.postValue(AttendanceStatusProf.Error("Failed to add service"))
                }
                3 -> { /* Failed to Clear Service */
                    Log.i(TAG, "WIFI_SD_Testing-ViewModel: broadcastTimestamp - Failed to Clear Service")
                    _atdStatus.postValue(AttendanceStatusProf.Error("Failed to Clear Service"))
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    private suspend fun broadcastServiceInLoop(manager: WifiP2pManager, channel: WifiP2pManager.Channel) {
        // Following is the solution from the post on link - https://stackoverflow.com/questions/26300889/wifi-p2p-service-discovery-works-intermittently
        while(isServiceBroadcasting) {
            delay(10000)
            manager.discoverPeers(channel, object: WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "WIFI_SD_Testing-ViewModel: DiscoverPeers - Services Reset Successfully")
                }

                override fun onFailure(reason: Int) {
                    Log.i(TAG, "WIFI_SD_Testing-ViewModel: DiscoverPeers - Failed to reset services: Reason-$reason")
                }
            })
        }
    }

    suspend fun stopBroadcasting(manager: WifiP2pManager, channel: WifiP2pManager.Channel) {
        val isCleared = BroadcastService.clearLocalServices(manager, channel)
        if(isCleared) {
            isServiceBroadcasting = false
            Log.i(TAG, "WIFI_SD_Testing-ViewModel: stopBroadcasting - Service Cleared Successfully")
        } else {
            /* Failed to add service */
            Log.i(TAG, "WIFI_SD_Testing-ViewModel: stopBroadcasting - Failed to Clear service")
        }
    }

    /**
     * To understand why we are using 'suspend fun',
     * read the note in ProfMarkAtdFrag (in setupObserver())
     */
    suspend fun observeAtd(sem_sec: String, courseCode: String, timestamp: String) {
//        _presentList.postValue(Response.Loading())
        try {
            withContext(Dispatchers.IO) {
                Log.i(TAG, "observeAtdTesting: STEP 1 = ObserveAtd Called")
//                val stdList = _presentList.value!!.data!!.toMutableMap()
                val stdList = _presentList.value!!.data!!

                Log.i(TAG, "observeAtdTesting: STEP 2 done = $stdList")

                profRepository.observeAttendance(sem_sec, courseCode, timestamp)
                    .collect { flowResult ->
                        Log.i(TAG, "observeAtdTesting: flowResult = $flowResult")

                        if(flowResult == "false") {
                            _atdStatus.postValue(AttendanceStatusProf.Ended())
                        } else {
//                            stdList.add(flowResult)
                            val student = stdList[flowResult]
                            stdList[flowResult] = RvProfMarkAtdListItem(
                                enrollment = flowResult, uid = student!!.uid, atdStatus = Constants.ATD_STATUS_PRESENT_WIFI_SD
                            )
                            _presentList.postValue(Response.Success(stdList))
                            _presentCount++
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _presentList.postValue(Response.Error(e.message.toString(), null))
        }
    }

    fun saveManualAtd(sem_sec: String, courseCode: String, timestamp: String, students: List<RvProfMarkAtdListItem>) {
        Log.i(TAG, "ProfTesting-ViewModel: Calling saveManualAtd")
        _saveManualStatus.postValue(Response.Loading())
        viewModelScope.launch {
            val markAtdResult = profRepository.markAtd(sem_sec, courseCode, timestamp, students)
            if(markAtdResult == FirebaseConstants.STATUS_SUCCESSFUL) {
                _saveManualStatus.postValue(Response.Success(true))
            } else {
                _saveManualStatus.postValue(Response.Error(markAtdResult, null))
            }
        }
    }

    fun endAttendance(sem_sec: String, courseCode: String, timestamp: String) {
        viewModelScope.launch {

            val endedStatus = profRepository.endAttendance(sem_sec, courseCode, timestamp)
            when(endedStatus) {
                is Response.Loading -> {}
                is Response.Error -> _atdStatus.postValue(AttendanceStatusProf.Error(endedStatus.errorMessage.toString()))
                is Response.Success -> {
                    _atdStatus.postValue(AttendanceStatusProf.Ended())
                }
            }
        }
    }

}