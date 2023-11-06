package com.example.teachjr.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.model.RvAtdReportListItem

class AtdReportAdapter : RecyclerView.Adapter<AtdReportAdapter.AtdReportViewHolder>() {

    private var itemList: List<RvAtdReportListItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AtdReportViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_atd_report_rv, parent, false)
        return AtdReportViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AtdReportViewHolder, position: Int) {
        holder.apply {
            tvDescription.text = itemList[position].description
            tvPercentage.text = itemList[position].percentage
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun updateList(updatedItems: List<RvAtdReportListItem>) {
        itemList = updatedItems
        notifyDataSetChanged()
    }

    inner class AtdReportViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvDescription = itemView.findViewById<TextView>(R.id.tvDescription)
        var tvPercentage = itemView.findViewById<TextView>(R.id.tvPercentage)
    }
}