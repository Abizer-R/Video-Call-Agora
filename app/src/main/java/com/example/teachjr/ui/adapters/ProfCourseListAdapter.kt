package com.example.teachjr.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.model.RvProfCourseListItem
import com.example.teachjr.utils.Adapter_ViewModel_Utils

class ProfCourseListAdapter(
    private val onItemClicked: (RvProfCourseListItem) -> Unit
): RecyclerView.Adapter<ProfCourseListAdapter.CourseViewHolder>() {

    private var courses: List<RvProfCourseListItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_course_rv_prof, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.apply {
            tvCourseCode.text = courses[position].courseCode
            tvCourseName.text = courses[position].courseName
//            val semSec = courses[position].sem_sec
//            val secIdx = semSec.indexOf("_", 0)
//            tvSection.text = semSec.substring(secIdx + 1)
            tvSection.text = Adapter_ViewModel_Utils.getSection(courses[position].sem_sec)
            holder.itemView.setOnClickListener {
                onItemClicked.invoke(courses[position])
            }
        }
    }

    fun updateList(updatedCourses: List<RvProfCourseListItem>) {
        courses = updatedCourses
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return courses.size
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCourseCode = itemView.findViewById<TextView>(R.id.tvCourseCode)
        var tvCourseName = itemView.findViewById<TextView>(R.id.tvCourseName)
        var tvSection = itemView.findViewById<TextView>(R.id.tvSection)
    }
}