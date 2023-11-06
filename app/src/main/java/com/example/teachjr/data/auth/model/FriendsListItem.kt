package com.example.teachjr.data.auth.model

data class FriendsListItem (
    val uuid: String,
    val name: String,
    val email: String = "",
    var btnType: FriendButtonType = FriendButtonType.NO_BUTTON
)

enum class FriendButtonType {
    NO_BUTTON, REQUEST_SENT, REQUEST_RECEIVED
}