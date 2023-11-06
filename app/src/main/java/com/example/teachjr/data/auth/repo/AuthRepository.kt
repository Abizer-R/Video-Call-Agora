package com.example.teachjr.data.auth.repo

import com.example.teachjr.utils.sealedClasses.Response
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun login(email: String, password: String): Response<FirebaseUser>

    suspend fun signupStudent(
        name: String, enrollment: String, email: String, password: String
    ): Response<FirebaseUser>

    suspend fun signupProfessor(
        name: String, email: String, password: String
    ): Response<FirebaseUser>
}