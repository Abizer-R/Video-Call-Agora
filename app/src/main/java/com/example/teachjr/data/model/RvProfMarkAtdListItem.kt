package com.example.teachjr.data.model

import com.example.teachjr.utils.Constants

data class RvProfMarkAtdListItem (
    val enrollment: String = "",
    val uid: String = "",
    var atdStatus: String = Constants.ATD_STATUS_ABSENT
)