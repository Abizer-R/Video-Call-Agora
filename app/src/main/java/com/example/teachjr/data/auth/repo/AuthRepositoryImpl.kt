package com.example.teachjr.data.auth.repo

import android.util.Log
import com.example.teachjr.data.auth.model.UserModel
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.sealedClasses.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl
    @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {

    private val TAG = AuthRepositoryImpl::class.java.simpleName
    val currUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun login(email: String, password: String): Response<FirebaseUser> {
        try {
            // We have no use of result right now...
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            return Response.Success(result.user)
        } catch (e: Exception) {
            e.printStackTrace()
            return Response.Error(e.message.toString(), null)
        }
    }


    suspend fun createUser(
        userModel: UserModel
    ): String {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currUser!!.uid)
                .setValue(userModel)
                .addOnSuccessListener {

                    dbRef.getReference(FirebasePaths.FRIENDS_COLLECTION)
                        .child(currUser!!.uid)
                        .child(currUser!!.uid)
                        .setValue(FirebasePaths.FRIENDS_STATUS_SELF)

                    continuation.resume(FirebaseConstants.STATUS_SUCCESSFUL)
                }
                .addOnFailureListener {
                    Log.i(TAG, "AuthTesting_Repo: markAtd = ${it.message}")
                    continuation.resume(it.message.toString())
                }
        }
    }

    suspend fun getUserType(): Response<String> {

        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currUser!!.uid)
                .child(FirebaseConstants.userType)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(Response.Success(snapshot.value.toString()))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Response.Error(error.message, null))
                    }
                })
        }
    }

    suspend fun checkEnrollment(enrollment: String): Response<Boolean> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currUser!!.uid)
                .child(FirebasePaths.STUDENT_ENROLLMENT)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userEnrollment = snapshot.value.toString()
                        continuation.resume(Response.Success(enrollment.equals(userEnrollment)))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Response.Error(error.message, null))
                    }
                })
        }
    }



//    fun logout() {
//        firebaseAuth.signOut()
//    }

}