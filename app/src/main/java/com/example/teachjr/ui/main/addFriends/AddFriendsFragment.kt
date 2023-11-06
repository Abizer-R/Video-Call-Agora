package com.example.teachjr.ui.main.addFriends


import android.view.View
import com.example.teachjr.databinding.FragmentAddFriendsBinding
import com.example.teachjr.databinding.FragmentHomeBinding
import com.example.teachjr.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFriendsFragment : BaseFragment<FragmentAddFriendsBinding>(
    FragmentAddFriendsBinding::inflate
) {

    override fun initUserInterface(view: View?) {
        // pass
    }

}