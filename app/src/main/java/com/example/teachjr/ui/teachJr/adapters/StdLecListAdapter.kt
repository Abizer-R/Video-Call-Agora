package com.example.teachjr.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.model.RvStdLecListItem
import com.example.teachjr.utils.Adapter_ViewModel_Utils

class StdLecListAdapter : RecyclerView.Adapter<StdLecListAdapter.CourseViewHolder>() {

    private var lectures: List<RvStdLecListItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_lec_rv_student, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.apply {
            val lecNum = lectures.size - position
            tvLecNum.text = "Lec-${lecNum}: "
            tvLecDate.text = Adapter_ViewModel_Utils.getFormattedDate(lectures[position].timestamp)
            if(lectures[position].isPresent) {
                tvLecNum.setTextColor(Color.parseColor("#01dd01"))
                tvLecDate.setTextColor(Color.parseColor("#01dd01"))
            } else {
                if(lectures[position].isContinuing) {

                    tvLecNum.setTextColor(Color.parseColor("#f79e00"))
                    tvLecDate.setTextColor(Color.parseColor("#f79e00"))
                } else {
                    tvLecNum.setTextColor(Color.parseColor("#f7292b"))
                    tvLecDate.setTextColor(Color.parseColor("#f7292b"))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return lectures.size
    }

    fun updateList(updatedLectures: List<RvStdLecListItem>) {
        lectures = updatedLectures
        notifyDataSetChanged()
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvLecNum = itemView.findViewById<TextView>(R.id.tvLecNum)
        var tvLecDate = itemView.findViewById<TextView>(R.id.tvLecDate)
    }
}