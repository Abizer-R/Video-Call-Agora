package com.example.teachjr.data.main.repo

import android.telecom.Call
import android.util.Log
import com.example.teachjr.data.auth.model.CallStatus
import com.example.teachjr.data.auth.model.FriendsListItem
import com.example.teachjr.data.auth.model.UserModel
import com.example.teachjr.data.model.*
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.FirebasePaths.FRIENDS_LIST_KEY
import com.example.teachjr.utils.FirebasePaths.OTHER_USER_EMAIL
import com.example.teachjr.utils.FirebasePaths.OTHER_USER_NAME
import com.example.teachjr.utils.FirebasePaths.OTHER_USER_UUID
import com.example.teachjr.utils.FirebasePaths.STATUS
import com.example.teachjr.utils.sealedClasses.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {

    private val TAG = MainRepository::class.java.simpleName
    private val currentUser: FirebaseUser
        get() = firebaseAuth.currentUser!!


    suspend fun getUserDetails(): Response<UserModel> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userModel = UserModel(
                            username = snapshot.child("username").value.toString(),
                            email = snapshot.child("email").value.toString()
                        )
                        Log.i(TAG, "StudentTesting_Repo: getUserDetails = ${snapshot}")
                        continuation.resume(Response.Success(userModel))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "StudentTesting_Repo: getUserDetails = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }

    suspend fun getFriendsList(): Response<List<FriendsListItem>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currentUser.uid)
                .child(FRIENDS_LIST_KEY)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val friendsList: MutableList<FriendsListItem> = ArrayList()
                        for(friend in snapshot.children) {
                            val uuid = friend.key.toString()
                            val name = friend.value.toString()
                            friendsList.add(FriendsListItem(uuid, name))
                        }
                        continuation.resume(Response.Success(friendsList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "StudentTesting_Repo: getCourseList = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }

    /**
     * This flow will get cancelled when prof will leave the MarkAtdFragment
     */
    fun observeCallStatus(): Flow<CallStatus> =
        dbRef.getReference("/${FirebasePaths.USER_COLLECTION}/${currentUser.uid}/${FirebasePaths.CALL_STATUS_KEY}")
            .observeChildEvent()
            .catch { Log.i(TAG, "MainTesting_ProRepo - observeAttendance: ERROR = ${it.message}") }


    private fun DatabaseReference.observeChildEvent(): Flow<CallStatus> = callbackFlow {
        val childEventListener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.i(TAG, "MainTesting_ProRepo: onCancelled = ${error.message}")
                close(error.toException())
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.i(TAG, "MainTesting_ProRepo - onChildAdded: snapshot - $snapshot, prevChildName - $previousChildName")

                val callStatus = when(snapshot.key) {
                    OTHER_USER_UUID -> {
                        CallStatus(otherUserUuid = snapshot.value.toString())
                    }

                    OTHER_USER_EMAIL -> {
                        CallStatus(otherUserEmail = snapshot.value.toString())

                    }

                    OTHER_USER_NAME -> {
                        CallStatus(otherUserName = snapshot.value.toString())

                    }

                    STATUS -> {
                        CallStatus(status = snapshot.value.toString())
                    }

                    else ->
                        CallStatus(snapshot.value.toString(), null)
                }
                trySend(callStatus).isSuccess
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.i(TAG, "MainTesting_ProRepo - onChildChanged: snapshot - $snapshot, prevChildName - $previousChildName")
                val callStatus = when(snapshot.key) {
                    OTHER_USER_UUID -> {
                        CallStatus(otherUserUuid = snapshot.value.toString())
                    }

                    OTHER_USER_EMAIL -> {
                        CallStatus(otherUserEmail = snapshot.value.toString())

                    }

                    OTHER_USER_NAME -> {
                        CallStatus(otherUserName = snapshot.value.toString())

                    }

                    STATUS -> {
                        CallStatus(status = snapshot.value.toString())
                    }

                    else ->
                        CallStatus(snapshot.value.toString(), null)
                }
                trySend(callStatus).isSuccess
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.i(TAG, "MainTesting_ProRepo - onChildRemoved: snapshot - $snapshot")
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.i(TAG, "MainTesting_ProRepo - onChildMoved: snapshot - $snapshot, prevChildName - $previousChildName")
                TODO("Not yet implemented")
            }
        }
        addChildEventListener(childEventListener)
        awaitClose {
            removeEventListener(childEventListener)
        }
    }

    suspend fun pushCallNotification(
        uuid: String,
        callStatus: CallStatus
    ): String {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(uuid)
                .child(FirebasePaths.CALL_STATUS_KEY)
                .setValue(callStatus)
                .addOnSuccessListener {
                    continuation.resume(FirebaseConstants.STATUS_SUCCESSFUL)
                }
                .addOnFailureListener {
                    Log.i(TAG, "AuthTesting_Repo: markAtd = ${it.message}")
                    continuation.resume(it.message.toString())
                }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

}