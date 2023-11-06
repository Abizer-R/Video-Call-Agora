package com.example.teachjr.ui.main.friendRequest

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Rect
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.example.teachjr.data.auth.model.FriendsListItem
import com.example.teachjr.databinding.FriendRequestBottomSheetBinding
import com.example.teachjr.ui.base.BaseBottomSheetDialog
import com.example.teachjr.ui.main.MainViewModel
import com.example.teachjr.ui.main.adapter.FriendsListAdapter
import com.example.teachjr.utils.FirebasePaths
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class FriendRequestsBottomSheet : BaseBottomSheetDialog(),
    FriendsListAdapter.FriendsAdapterListener {

    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var friendsListAdapter: FriendsListAdapter

    companion object {
        fun newInstance() = FriendRequestsBottomSheet().apply {
//            arguments = bundleOf()
        }
    }

    private lateinit var binding: FriendRequestBottomSheetBinding
    private var listener: FriendRequestBsListener? = null


    override fun onBottomSheetBehavior(
        dialog: BottomSheetDialog?,
        bottomSheetBehavior: BottomSheetBehavior<FrameLayout>?
    ) {
        if (bottomSheetBehavior == null) return

        setContentHeight(true)
        // setting up bottomSheet
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.skipCollapsed = false
        val expandedHeight = getDisplayHeight() - getStatusBarHeight()
        val peekHeight = (expandedHeight * 0.85).toInt()
        bottomSheetBehavior.peekHeight = peekHeight
        setContentHeight(true)

        setUpViews()
    }

    private fun setUpViews() {
        with(binding) {
            friendsListAdapter = FriendsListAdapter(this@FriendRequestsBottomSheet)
            rvRequests.adapter = friendsListAdapter
            Log.e("TESTING2", "setUpViews: pendingRequests = ${mainViewModel.getPendingRequests()}", )
        }

        val requestList = mainViewModel.getPendingRequests()
        friendsListAdapter.submitList(requestList)
    }

    private fun getDisplayHeight(): Int = Resources.getSystem().displayMetrics.heightPixels

    private fun getStatusBarHeight(): Int {
        val rectangle = Rect()
        val window = activity?.window ?: return 0
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        return rectangle.top
    }

    override fun onCreateViewBinding(): ViewBinding {
        binding = FriendRequestBottomSheetBinding.inflate(layoutInflater)
        return binding
    }




    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentFragment = parentFragment
        val activity = activity
        if (parentFragment is FriendRequestBsListener) {
            listener = parentFragment
        } else if (activity is FriendRequestBsListener) {
            listener = activity
        }
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    private fun showToast(msg: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFriendClicked(friendItem: FriendsListItem) {
        showToast("friend = ${friendItem.name}")


        /**
         *
         *  TODO: Implement this feature
         *
         *
         */




    }

    interface FriendRequestBsListener {
        fun onAccepted(userUuid: String)
    }
}