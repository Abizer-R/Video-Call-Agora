package com.example.teachjr.utils.sealedClasses

sealed class Response<T>(val data: T? = null, val errorMessage: String? = null)
{
    class Loading<T> : Response<T>()
    class Error<T> (errorMessage: String, data: T?) : Response<T>(data, errorMessage = errorMessage)
    class Success<T> (data: T? = null) : Response<T>(data = data)
}