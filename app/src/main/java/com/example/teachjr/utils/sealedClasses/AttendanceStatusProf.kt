package com.example.teachjr.utils.sealedClasses

sealed class AttendanceStatusProf(val timestamp: String? = null, val errorMessage: String? = null)
{
    class FetchingTimestamp : AttendanceStatusProf()
    class Initiated (timestamp: String) : AttendanceStatusProf(timestamp = timestamp)
    class Ended : AttendanceStatusProf()
    class Error (errorMessage: String) : AttendanceStatusProf(errorMessage = errorMessage)
}
