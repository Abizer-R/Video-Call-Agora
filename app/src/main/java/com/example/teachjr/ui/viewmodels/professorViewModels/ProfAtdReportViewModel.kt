package com.example.teachjr.ui.viewmodels.professorViewModels

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.teachjr.data.model.ProfAttendanceDetails
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.utils.Adapter_ViewModel_Utils
import com.example.teachjr.utils.Constants
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.excelReadWrite.WriteExcel
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfAtdReportViewModel
    @Inject constructor(
    private val profRepository: ProfRepository
): ViewModel() {

    private val TAG = ProfAtdReportViewModel::class.java.simpleName

    private var _detailsLoaded = false
    val detailsLoaded: Boolean
        get() = _detailsLoaded

    private val _atdDetails = MutableLiveData<Response<ProfAttendanceDetails>>()
    val atdDetails: LiveData<Response<ProfAttendanceDetails>>
        get() = _atdDetails

    suspend fun getAtdDetails(sem_sec: String, courseCode: String) {
       _atdDetails.postValue(Response.Loading())
       withContext(Dispatchers.IO) {
            val stdListResponse = profRepository.getStdList(sem_sec)
            when(stdListResponse) {
                is Response.Loading -> {}
                is Response.Error -> {
                    _atdDetails.postValue(Response.Error(stdListResponse.errorMessage!!, null))
                    Log.i(TAG, "Prof_testing: stdList error - ${stdListResponse.errorMessage}")
                }
                is Response.Success -> {

                    val lecListResponse = profRepository.getLectureList(sem_sec, courseCode)
                    when(lecListResponse) {
                        is Response.Loading -> {}
                        is Response.Error -> {
                            _atdDetails.postValue(Response.Error(lecListResponse.errorMessage!!, null))
                            Log.i(TAG, "Prof_testing: lecList error - ${lecListResponse.errorMessage}")
                        }
                        is Response.Success -> {
                            _atdDetails.postValue(
                                Response.Success(ProfAttendanceDetails(
                                studentList = stdListResponse.data!!,
                                lectureList = lecListResponse.data!!
                            )))
                            _detailsLoaded = true
                        }
                    }
                }
            }
       }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onCleared: VIEWMODEL CLEARED")
    }


    /**
     * Below stuff is used in ProfExcelSheetFragment
     */

    private var _fileName = Constants.FILE_NOT_FOUND
    val fileName: String
        get() = _fileName
    fun updateFileName(name: String) { _fileName = name }

//    private val _fileSaveStatus = MutableLiveData<Response<String>>()
//    val fileSaveStatus: LiveData<Response<String>>
//        get() = _fileSaveStatus


}