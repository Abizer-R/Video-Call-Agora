package com.example.teachjr.utils

import android.text.TextUtils
import android.util.Patterns

object AuthUtils {

    fun validateCredentials(username: String, email: String, password: String, isLogin: Boolean): Pair<Boolean, String> {
        var result = Pair(true, "")

        if((!isLogin && TextUtils.isEmpty(username)) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            result = Pair(false, "Please enter all credentials")

        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            result = Pair(false, "Please provide valid email")

        } else if(password.length <= 5) {
            result = Pair(false, "Password length must be greater than 5")

        }
        return result
    }
}