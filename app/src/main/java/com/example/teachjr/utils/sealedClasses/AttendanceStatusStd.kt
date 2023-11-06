package com.example.teachjr.utils.sealedClasses

sealed class AttendanceStatusStd(val timestamp: String? = null, val remainingTime: String? = null, val errorMessage: String? = null)
{
//    class InitiatingDiscovery: AttendanceStatusStd()
    class DiscoveringTimestamp (remainingTime : String) : AttendanceStatusStd(remainingTime = remainingTime)
    class TimestampNotFound : AttendanceStatusStd()
    class TimestampDiscovered (timestamp: String) : AttendanceStatusStd(timestamp = timestamp)
    class AttendanceMarked () : AttendanceStatusStd()
    class BroadcastComplete () : AttendanceStatusStd()
    class Error (errorMessage: String) : AttendanceStatusStd(errorMessage = errorMessage)
}
