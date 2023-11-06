package com.example.teachjr.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.auth.model.FriendButtonType
import com.example.teachjr.data.auth.model.FriendsListItem
import com.example.teachjr.databinding.FriendsListItemBinding

class FriendsListAdapter(
    private val listener: FriendsAdapterListener
): ListAdapter<FriendsListItem, FriendsListAdapter.FriendViewHolder>(
    object : DiffUtil.ItemCallback<FriendsListItem>() {
        override fun areItemsTheSame(oldItem: FriendsListItem, newItem: FriendsListItem) = oldItem.uuid == newItem.uuid
        override fun areContentsTheSame(oldItem: FriendsListItem, newItem: FriendsListItem) = oldItem == newItem

    })
{

    inner class FriendViewHolder(private val binding: FriendsListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friendItem: FriendsListItem) {
            with(binding) {
                tvName.text = friendItem.name
                root.setOnClickListener {
                    listener.onFriendClicked(currentList[adapterPosition])
                }

                if(friendItem.email.isNotBlank()) {
                    tvEmail.text = friendItem.email
                    tvEmail.isVisible = true
                }

                when(friendItem.btnType) {
                    FriendButtonType.NO_BUTTON -> {}
                    FriendButtonType.REQUEST_SENT -> {
                        btnToggleRequest.text = "Reject"
                        btnToggleRequest.setBackgroundColor(ContextCompat.getColor(root.context, R.color.red))
                        btnToggleRequest.isVisible = true
                    }

                    FriendButtonType.REQUEST_RECEIVED -> {
                        btnToggleRequest.text = "Accept"
                        btnToggleRequest.setBackgroundColor(ContextCompat.getColor(root.context, R.color.green))
                        btnToggleRequest.isVisible = true

                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        return FriendViewHolder(
            FriendsListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    interface FriendsAdapterListener {
        fun onFriendClicked(friendItem: FriendsListItem)
    }
}