package com.example.teachjr.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.model.RvProfMarkAtdListItem
import com.example.teachjr.utils.Constants
import com.google.android.material.card.MaterialCardView

class AttendanceAdapter(
    private val onItemClicked: (List<RvProfMarkAtdListItem>, Int) -> Unit
) : RecyclerView.Adapter<AttendanceAdapter.CourseViewHolder>() {

    private var enrollmentList: List<RvProfMarkAtdListItem> = ArrayList()
//    private var enrollments: List<String> = ArrayList()

    fun getManualAtdList(): List<RvProfMarkAtdListItem> {
        return enrollmentList.filter { it.atdStatus == Constants.ATD_STATUS_PRESENT_MANUAL }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_attendance_rv, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.apply {
            val enrollment = enrollmentList[position].enrollment
            val size = enrollment.length
            tvEnrollment.text = enrollment.substring(size - 3)

            when(enrollmentList[position].atdStatus) {
                Constants.ATD_STATUS_ABSENT -> {
                    tvEnrollment.setTextColor(Color.parseColor(Constants.HEX_CODE_RED))
                    cvParent.strokeWidth = 0
                }
                Constants.ATD_STATUS_PRESENT_WIFI_SD -> {
                    tvEnrollment.setTextColor(Color.parseColor(Constants.HEX_CODE_GREEN))
                    cvParent.strokeWidth = 0
                }
                Constants.ATD_STATUS_PRESENT_MANUAL -> {
                    tvEnrollment.setTextColor(Color.parseColor(Constants.HEX_CODE_GREEN))
                    cvParent.strokeColor = Color.parseColor(Constants.HEX_CODE_GREEN)
                    cvParent.strokeWidth = 4
                }
            }

            holder.itemView.setOnClickListener {
                onItemClicked(enrollmentList, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return enrollmentList.size
    }

    fun updateList(updatedList: List<RvProfMarkAtdListItem>) {
        enrollmentList = updatedList
        notifyDataSetChanged()
    }

//    override fun getItemCount(): Int {
//        return enrollments.size
//    }
//
//    fun updateList(updatedList: List<String>) {
//        enrollments = updatedList
//        notifyDataSetChanged()
//    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvEnrollment = itemView.findViewById<TextView>(R.id.tvEnrollment)
        var cvParent = itemView.findViewById<MaterialCardView>(R.id.cvParent)
    }
}