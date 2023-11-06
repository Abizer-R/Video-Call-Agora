package com.example.teachjr.data.source.repository

import android.util.Log
import com.example.teachjr.data.model.*
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.sealedClasses.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StudentRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {

    private val TAG = StudentRepository::class.java.simpleName
    private val currentUser: FirebaseUser
        get() = firebaseAuth.currentUser!!


    suspend fun getUserDetails(): Response<StudentUser> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(Response.Success(snapshot.getValue(StudentUser::class.java)))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "StudentTesting_Repo: getUserDetails = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }

    suspend fun getCourseList(institute: String, branch: String, sem_sec: String): Response<List<RvStdCourseListItem>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.COURSE_COLLECTION)
                .child(institute)
                .child(branch)
                .child(sem_sec)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val courseList: MutableList<RvStdCourseListItem> = ArrayList()
                        for(courseSS in snapshot.children) {
                            val courseCode = courseSS.key.toString()
                            val courseName = courseSS.child(FirebasePaths.COURSE_NAME).value.toString()
                            val profName = courseSS.child(FirebasePaths.COURSE_PROF_NAME).value.toString()
                            courseList.add(RvStdCourseListItem(courseCode, courseName, profName))
                        }
//                        for(course in courseList) {
//                            Log.i(TAG, "StdTesting: courseItem - $course")
//                        }
                        continuation.resume(Response.Success(courseList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "StudentTesting_Repo: getCourseList = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }
    
    suspend fun getAttendanceDetails(sem_sec: String, courseCode: String): Response<StdAttendanceDetails> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ATTENDANCE_COLLECTION)
                .child(sem_sec)
                .child(courseCode)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val lecCount = snapshot.child(FirebasePaths.LEC_COUNT).getValue(Int::class.java)
                        var lecAttended: Int = 0
                        val stdLecListItem: MutableList<RvStdLecListItem> = ArrayList()

                        for(lecInfo in snapshot.child(FirebasePaths.LEC_LIST).children) {
//                            val timestamp = lecInfo.child(FirebasePaths.TIMESTAMP).value.toString()
                            val timestamp = lecInfo.key.toString()
                            var isContinuing = false
                            if(lecInfo.child(FirebasePaths.ATD_IS_CONTINUING).getValue(Boolean::class.java) == true) {
                                isContinuing = true
                            }
                            if(lecInfo.child(currentUser.uid).exists()) {
                                lecAttended++
                                stdLecListItem.add(RvStdLecListItem(timestamp, isContinuing, true))
                            } else {
                                stdLecListItem.add(RvStdLecListItem(timestamp, isContinuing, false))
                            }
                        }

                        val atdDetails = StdAttendanceDetails(lecCount!!, lecAttended, lecCount - lecAttended, stdLecListItem)
                        continuation.resume(Response.Success(atdDetails))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "StudentTesting_Repo: getLecDetails_Error = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }

    suspend fun checkAtdStatus(sem_sec: String, courseCode: String, timestamp: String): String {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ATTENDANCE_COLLECTION)
                .child(sem_sec)
                .child(courseCode)
                .child(FirebasePaths.LEC_LIST)
                .child(timestamp)
                .child(FirebasePaths.ATD_IS_CONTINUING)
                .get()
                .addOnSuccessListener {
                    Log.i(TAG, "StudentTesting_Repo: checkAtdStatus = ${it.getValue(Boolean::class.java).toString()}")
                    continuation.resume(it.getValue(Boolean::class.java).toString())
                }
                .addOnFailureListener {
                    Log.i(TAG, "StudentTesting_Repo: checkAtdStatus = ${it.message}")
                    continuation.resume(it.message.toString())
                }
        }
    }

    suspend fun markAtd(
        sem_sec: String, courseCode: String, timestamp: String, enrollment: String): String {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ATTENDANCE_COLLECTION)
                .child(sem_sec)
                .child(courseCode)
                .child(FirebasePaths.LEC_LIST)
                .child(timestamp)
                .child(currentUser.uid)
                .setValue(enrollment)
                .addOnSuccessListener {
                    continuation.resume(FirebaseConstants.STATUS_SUCCESSFUL)
                }
                .addOnFailureListener {
                    Log.i(TAG, "StudentTesting_Repo: markAtd = ${it.message}")
                    continuation.resume(it.message.toString())
                }
        }
    }

//    private suspend fun DatabaseReference.userDetails(): Response<User> = suspendCoroutine { continuation ->
//        val valueEventListener = object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if(snapshot.exists()) {
//                    continuation.resume(Response.Success(snapshot.getValue(User::class.java)))
//                } else {
//                    Log.i(TAG, "onDataChange: Something Went Wrong")
//                    continuation.resume(Response.Error("Something Went Wrong in repository.userDetails()", null))
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                continuation.resume(Response.Error(error.message, null))
//            }
//        }
//        // Subscribe to the callback
//        addListenerForSingleValueEvent(valueEventListener)
//    }

    fun logout() {
        firebaseAuth.signOut()
    }

}