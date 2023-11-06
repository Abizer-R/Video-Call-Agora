package com.example.teachjr.data.auth.model

import com.example.teachjr.utils.FirebasePaths.CALL_STATUS_NOTHING

data class UserModel(
    val username: String,
    val email: String,
    val call_status: CallStatus = CallStatus(),

)

data class CallStatus (
    var otherUserUuid: String? = null,
    var otherUserName: String? = null,
    var otherUserEmail: String? = null,
    var status: String? = null
)