package com.example.teachjr.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.auth.model.FriendsListItem
import com.example.teachjr.data.model.RvProfCourseListItem
import com.example.teachjr.data.model.RvStdCourseListItem

class FriendsListAdapter(
    private val onItemClicked: (FriendsListItem) -> Unit
): RecyclerView.Adapter<FriendsListAdapter.CourseViewHolder>() {

    private var friendsList: List<FriendsListItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.friends_list_item, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.apply {

            tvName.text = friendsList[position].name
//            tvProfName.text = courses[position].profName
            holder.itemView.setOnClickListener {
                onItemClicked.invoke(friendsList[position])
            }
        }
    }

    fun updateList(updatedCourses: List<FriendsListItem>) {
        friendsList = updatedCourses
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName = itemView.findViewById<TextView>(R.id.tvName)
    }
}