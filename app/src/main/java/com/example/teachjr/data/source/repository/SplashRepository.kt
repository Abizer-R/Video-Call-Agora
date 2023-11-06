package com.example.teachjr.data.source.repository

import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.sealedClasses.Response
import com.example.teachjr.utils.sealedClasses.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SplashRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {
    val currUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Same code is used in AuthRepository for getting User type
     */
    // TODO: RETURN A RESPONSE<USERTYPE>
    suspend fun getUserType(): Response<UserType?> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currUser!!.uid)
                .child(FirebaseConstants.userType)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userType = snapshot.value.toString()
                        when(userType) {
                            FirebaseConstants.TYPE_STUDENT -> continuation.resume(
                                Response.Success(
                                    UserType.Student()))
                            FirebaseConstants.TYPE_PROFESSOR -> continuation.resume(
                                Response.Success(
                                    UserType.Teacher()))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Response.Error(error.message, null))
                    }
                })
        }
    }
}