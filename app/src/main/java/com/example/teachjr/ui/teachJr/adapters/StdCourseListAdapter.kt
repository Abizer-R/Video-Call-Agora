package com.example.teachjr.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.model.RvProfCourseListItem
import com.example.teachjr.data.model.RvStdCourseListItem

class StdCourseListAdapter(
    private val onItemClicked: (RvStdCourseListItem) -> Unit
): RecyclerView.Adapter<StdCourseListAdapter.CourseViewHolder>() {

    private var courses: List<RvStdCourseListItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_course_rv_student, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.apply {
            tvCourseCode.text = courses[position].courseCode
            tvCourseName.text = courses[position].courseName
//            tvProfName.text = courses[position].profName
            holder.itemView.setOnClickListener {
                onItemClicked.invoke(courses[position])
            }
        }
    }

    fun updateList(updatedCourses: List<RvStdCourseListItem>) {
        courses = updatedCourses
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return courses.size
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCourseCode = itemView.findViewById<TextView>(R.id.tvCourseCode)
        var tvCourseName = itemView.findViewById<TextView>(R.id.tvCourseName)
//        var tvProfName = itemView.findViewById<TextView>(R.id.tvProfName)
    }
}