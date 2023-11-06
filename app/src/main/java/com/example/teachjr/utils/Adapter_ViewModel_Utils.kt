package com.example.teachjr.utils

import android.util.Log
import com.example.teachjr.data.model.Lecture
import com.example.teachjr.data.model.ProfAttendanceDetails
import com.example.teachjr.data.model.RvAtdReportListItem
import com.example.teachjr.data.model.RvProfCourseListItem
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object Adapter_ViewModel_Utils {

    fun getSection(sem_sec: String): String {
        val secIdx = sem_sec.indexOf("_", 0)
        return sem_sec.substring(secIdx + 1)
    }

    fun getFormattedDate(timestamp: String): String {
        try {
//            val sdf = SimpleDateFormat("EEE, MMM dd, yyyy")
            val sdf = SimpleDateFormat("dd/MM/yy")
            val netDate = Date(timestamp.toLong())
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    fun getFormattedDate2(timestamp: String): String {
        try {
//            val sdf = SimpleDateFormat("EEE, MMM dd, yyyy")
            val sdf = SimpleDateFormat("EEE, MMM dd")
            val netDate = Date(timestamp.toLong())
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    fun getFormattedDate3(timestamp: String): String {
        try {
//            val sdf = SimpleDateFormat("EEE, MMM dd, yyyy")
            val sdf = SimpleDateFormat("dd_MM_yyyy")
            val netDate = Date(timestamp.toLong())
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    fun getLecturePercentage(totalStdCount: Int, lecList: List<Lecture>): List<RvAtdReportListItem> {
        val lecPercentageList: MutableList<RvAtdReportListItem> = ArrayList()


        for(i in lecList.indices) {
            val lecture = lecList[i]

            val date = getFormattedDate(lecture.timestamp)
            val description = "Lec-${i+1}: ($date)"

            val presentCount = lecture.presentList.size.toDouble()
            val percentageVal: Double = (presentCount / totalStdCount.toDouble() ) * 100.0
            val percentageText = "${roundOffDecimal(percentageVal)}%"

            lecPercentageList.add(RvAtdReportListItem(description, percentageText))
        }

        return lecPercentageList
    }

    fun getStdPercentage(stdList: List<String>, lecList: List<Lecture>): List<RvAtdReportListItem> {
        val stdPercentageList: MutableList<RvAtdReportListItem> = ArrayList()

        for(studentEnrollment in stdList) {
            val totalLecCount = lecList.size.toDouble()

            var presentCount: Double = 0.0
            for(lecture in lecList) {
                val isPresent = lecture.presentList.find { it.equals(studentEnrollment) }
                if(isPresent != null) {
                    presentCount++
                }
            }

            val percentageVal: Double = (presentCount / totalLecCount) * 100.0
            val percentageText = "${roundOffDecimal(percentageVal)}%"

            stdPercentageList.add(RvAtdReportListItem(studentEnrollment, percentageText))
        }

        return stdPercentageList
    }

    private fun roundOffDecimal(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number).toDouble()
    }

    fun getFormattedTime(seconds: Int) : String {
        return "${(seconds / 60).toString().padStart(2, '0')} : " +
                (seconds % 60).toString().padStart(2, '0')
    }

    fun generateFileName(): String {
        val timestamp = Calendar.getInstance().timeInMillis.toString()
        val dateString = getFormattedDate3(timestamp)
        return "${Constants.REPORT_FILE_NAME_PREFIX}_$dateString"
    }
}