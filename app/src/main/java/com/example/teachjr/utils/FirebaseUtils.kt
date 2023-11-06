package com.example.teachjr.utils

object FirebaseConstants {

    const val TYPE_STUDENT: String = "student"
    const val TYPE_PROFESSOR: String = "professor"
    const val userType: String = "userType"

    const val STATUS_ATD_ENDED = "ATTENDANCE_ENDED"
    const val STATUS_SUCCESSFUL = "SUCCESSFUL"
}

object FirebasePaths {

    const val ATTENDANCE_COLLECTION = "Attendance_Collection"
//    const val ATD_LEC_COUNT = "lecCount"
    const val TIMESTAMP = "timestamp"
    const val LEC_LIST = "lecList"
    const val ATD_IS_CONTINUING = "isContinuing"

    const val COURSE_COLLECTION = "Course_Collection"
    const val COURSE_NAME = "courseName"
    const val LEC_COUNT = "lecCount"
    const val COURSE_PROF_NAME = "profName"
    const val COURSE_PATHS_PROF = "coursePaths"
    const val COURSE_BRANCH = "branch"

    const val ENROLLMENT_COLLECTION = "Enrollment_Collection"
    const val ENRL_STUDENT_COUNT = "stdCount"
    const val ENRL_STUDENT_LIST = "student_list"

    const val USER_COLLECTION = "User_Collection_VIDEO_APP"
    const val FRIENDS_LIST_KEY = "friends_list"
    const val CALL_STATUS_KEY = "call_status"
    const val PROF_SEM_SEC_LIST = "sem_sec_list"


    const val USER_INFO = "User Info"
    const val USER_EMAIL = "email"
    const val USER_ID = "id"
    const val USER_NAME = "name"
    const val STUDENT_ENROLLMENT = "enrollment"
    const val COURSE_LIST = "courseList"

    const val COURSE_ID = "courseId"


    const val  COURSE_CODE = "courseCode"
    const val  PROF_ID = "profId"
//    const val  PROF_NAME = "profName"
    const val  ENROLLMENT_DOC_ID = "enrolDocId"
    const val  LECTURE_DOC_ID = "lecDocId"

    const val LECTURE_COLLECTION = "Lectures Collection"
    const val ENROLLMENT_REQ_COUNT = "reqCount"
    const val ENROLLMENT_REQ_LIST = "reqList"



    const val OTHER_USER_UUID = "otherUserUuid"
    const val OTHER_USER_EMAIL = "otherUserEmail"
    const val OTHER_USER_NAME = "otherUserName"
    const val STATUS = "status"
    const val CALL_STATUS_NOTHING = "nothing"
    const val CALL_STATUS_INCOMING_REQUEST = "incoming_request"
    const val CALL_STATUS_OUTGOING_REQUEST = "outgoing_request"
    const val CALL_STATUS_OUTGOING_REJECTED = "rejected"
}