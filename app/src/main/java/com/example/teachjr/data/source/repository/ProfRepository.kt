package com.example.teachjr.data.source.repository

import android.util.Log
import com.example.teachjr.data.model.*
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
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

class ProfRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {

    private val TAG = ProfRepository::class.java.simpleName
    private val currentUser: FirebaseUser
        get() = firebaseAuth.currentUser!!

    suspend fun getUserDetails(): Response<ProfessorUser> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(Response.Success(snapshot.getValue(ProfessorUser::class.java)))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "ProfessorTesting_Repo: getUserDetails = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }


    suspend fun getCourseList(): Response<List<RvProfCourseListItem>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currentUser.uid)
                .child(FirebasePaths.COURSE_PATHS_PROF)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val courseList: MutableList<RvProfCourseListItem> = ArrayList()
                        for(courseDetail in snapshot.children) {
                            val courseCode = courseDetail.key.toString()
                            val courseName = courseDetail.child(FirebasePaths.COURSE_NAME).value.toString()
                            for(sem_sec in courseDetail.child(FirebasePaths.PROF_SEM_SEC_LIST).children) {
                                courseList.add(RvProfCourseListItem(courseCode, courseName, sem_sec.key.toString()))
                            }

                        }
                        for(course in courseList) {
                            Log.i(TAG, "ProfessorTesting_ProRepo: course - $course")
                        }
                        continuation.resume(Response.Success(courseList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "ProfessorTesting_Repo: getCourseList = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }


    suspend fun getLectureCount(sem_sec: String, courseCode: String): Response<Int> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ATTENDANCE_COLLECTION)
                .child(sem_sec)
                .child(courseCode)
                .child(FirebasePaths.LEC_COUNT)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(Response.Success(snapshot.getValue(Int::class.java)))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "ProfessorTesting_Repo: getLectureCount = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }
                })
        }
    }

    suspend fun getStdList(sem_sec: String): Response<List<String>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ENROLLMENT_COLLECTION)
                .child(sem_sec)
                .child(FirebasePaths.ENRL_STUDENT_LIST)
                .get()
                .addOnSuccessListener {
                    val stdList: MutableList<String> = ArrayList()
                    for(students in it.children) {
                        stdList.add(students.key.toString())
                    }
                    continuation.resume(Response.Success(stdList))
                }
                .addOnFailureListener {
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }

    suspend fun getStdListWithUid(sem_sec: String): Response<MutableMap<String, String>> {
//        /**
//         * Testing purpose
//         */
//        return Response.Success(mutableMapOf(
//            "0818CS201001" to "uid1",
//            "0818CS201002" to "uid2",
//            "0818CS201003" to "uid3",
//            "0818CS201004" to "uid4",
//            "0818CS201005" to "uid5",
//            "0818CS201006" to "uid6",
//            "0818CS201007" to "uid7",
//            "0818CS201008" to "uid8",
//            ))
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ENROLLMENT_COLLECTION)
                .child(sem_sec)
                .child(FirebasePaths.ENRL_STUDENT_LIST)
                .get()
                .addOnSuccessListener {
                    val stdList: MutableMap<String, String> = mutableMapOf()
                    for(student in it.children) {
                        /**
                         * <String, String> == <Enrollment, Uid>
                          */
                        stdList[student.key.toString()] = student.value.toString()
//                        stdList.add(students.key.toString())
                    }
                    continuation.resume(Response.Success(stdList))
                }
                .addOnFailureListener {
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }

    suspend fun getLectureList(sem_sec: String, courseCode: String): Response<List<Lecture>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ATTENDANCE_COLLECTION)
                .child(sem_sec)
                .child(courseCode)
                .child(FirebasePaths.LEC_LIST)
                .get()
                .addOnSuccessListener {

                    val lectureList: MutableList<Lecture> = ArrayList()

                    for(lecture in it.children) {
                        val timestamp = lecture.key.toString()
                        val presentList: MutableList<String> = ArrayList()
                        for(student in lecture.children) {
                            if(student.key != FirebasePaths.ATD_IS_CONTINUING) {
                                presentList.add(student.value.toString())
                            }
                        }
                        lectureList.add(Lecture(timestamp, presentList))
                    }
                    continuation.resume(Response.Success(lectureList))
                }
                .addOnFailureListener {
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }

    /**
     * It doesn't matter what we return here, all we need is confirmation
     */
    suspend fun createNewAtdRef(
        sem_sec: String, courseCode: String, timestamp: String, lecCount: Int): Response<Boolean> {
        return suspendCoroutine { continuation ->
            Log.i(TAG, "createNewAtdRef: TIMESTAMP = $timestamp")

            val courseAtdPath = "/${FirebasePaths.ATTENDANCE_COLLECTION}/$sem_sec/$courseCode"
            val updates = hashMapOf<String, Any>(
                "$courseAtdPath/${FirebasePaths.LEC_COUNT}" to (lecCount+1),
                "$courseAtdPath/${FirebasePaths.LEC_LIST}/$timestamp/${FirebasePaths.ATD_IS_CONTINUING}" to true
            )
            dbRef.reference.updateChildren(updates)
                .addOnSuccessListener {
                    continuation.resume(Response.Success(true))
                }
                .addOnFailureListener {
                    Log.i(TAG, "ProfessorTesting_Repo: createNewAtdRef = ${it.message}")
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }


    /**
     * This flow will get cancelled when prof will leave the MarkAtdFragment
     */
    fun observeAttendance(sem_sec: String, courseCode: String, timestamp: String): Flow<String> =
        dbRef.getReference("/${FirebasePaths.ATTENDANCE_COLLECTION}/$sem_sec/$courseCode/${FirebasePaths.LEC_LIST}/$timestamp")
            .observeChildEvent()
            .catch { Log.i(TAG, "ProfessorTesting_ProRepo - observeAttendance: ERROR = ${it.message}") }

    private fun DatabaseReference.observeChildEvent(): Flow<String> = callbackFlow {
            val childEventListener = object : ChildEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, "ProfessorTesting_Repo: onCancelled = ${error.message}")
                    close(error.toException())
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.i(TAG, "ProfessorTesting_ProRepo - onChildAdded: snapshot - $snapshot, prevChildName - $previousChildName")
                    if(snapshot.key != (FirebasePaths.ATD_IS_CONTINUING)) {
                        trySend(snapshot.value.toString()).isSuccess
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.i(TAG, "ProfessorTesting_ProRepo - onChildChanged: snapshot - $snapshot, prevChildName - $previousChildName")
                    if(snapshot.key == FirebasePaths.ATD_IS_CONTINUING) {
                        trySend(snapshot.value.toString()).isSuccess
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.i(TAG, "ProfessorTesting_ProRepo - onChildRemoved: snapshot - $snapshot")
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.i(TAG, "ProfessorTesting_ProRepo - onChildMoved: snapshot - $snapshot, prevChildName - $previousChildName")
                    TODO("Not yet implemented")
                }
            }
        addChildEventListener(childEventListener)
        awaitClose {
            removeEventListener(childEventListener)
        }
    }

    /**
     * It doesn't matter what we return here, all we need is confirmation
     */
    suspend fun endAttendance(
        sem_sec: String, courseCode: String, timestamp: String): Response<Boolean> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ATTENDANCE_COLLECTION)
                .child("/$sem_sec/$courseCode/${FirebasePaths.LEC_LIST}/$timestamp/${FirebasePaths.ATD_IS_CONTINUING}")
                .setValue(false)
                .addOnSuccessListener {
                    continuation.resume(Response.Success(true))
                }
                .addOnFailureListener {
                    Log.i(TAG, "ProfessorTesting_Repo: endAttendance = ${it.message}")
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }

    suspend fun markAtd(
        sem_sec: String, courseCode: String, timestamp: String, students: List<RvProfMarkAtdListItem>): String {
        return suspendCoroutine { continuation ->

            Log.i(TAG, "ProfTesting: markAtd() called")

            val lecPath = "/${FirebasePaths.ATTENDANCE_COLLECTION}/$sem_sec/$courseCode/${FirebasePaths.LEC_LIST}/$timestamp"
            val updates = mutableMapOf<String, Any>()
            students.forEach { it ->
                updates["$lecPath/${it.uid}"] = it.enrollment
            }


            dbRef.reference.updateChildren(updates)
                .addOnSuccessListener {
                    continuation.resume(FirebaseConstants.STATUS_SUCCESSFUL)
                }
                .addOnFailureListener {
                    Log.i(TAG, "ProfessorTesting_Repo: createNewAtdRef = ${it.message}")
                    continuation.resume(it.message.toString())
                }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

}