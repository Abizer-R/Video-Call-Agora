package com.example.teachjr.utils.sealedClasses

sealed class UserType {
    class Student: UserType()
    class Teacher: UserType()
}
