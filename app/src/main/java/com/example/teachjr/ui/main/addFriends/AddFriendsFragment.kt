package com.example.teachjr.ui.main.addFriends


import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.teachjr.data.auth.model.CallStatus
import com.example.teachjr.data.auth.model.FriendsListItem
import com.example.teachjr.databinding.FragmentAddFriendsBinding
import com.example.teachjr.databinding.FragmentHomeBinding
import com.example.teachjr.ui.base.BaseFragment
import com.example.teachjr.ui.main.MainViewModel
import com.example.teachjr.ui.main.adapter.FriendsListAdapter
import com.example.teachjr.ui.main.friendRequest.FriendRequestsBottomSheet
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.sealedClasses.Response
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddFriendsFragment : BaseFragment<FragmentAddFriendsBinding>(
    FragmentAddFriendsBinding::inflate
), FriendsListAdapter.FriendsAdapterListener {

    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var friendsListAdapter: FriendsListAdapter

    override fun initUserInterface(view: View?) {

        mainViewModel.getUsersList()

        setupUi()
        setupObservers()

        mainViewModel.observeFriendRequests()
    }

    private fun setupUi() {
        with(binding) {
            val flow = MutableSharedFlow<String>()
            searchLayout.searchEditText.hint = "Search users"
            searchLayout.searchEditText.doOnTextChanged { text, start, before, count ->
                lifecycleScope.launch {
                    flow.emit(text.toString())
                }
            }

            lifecycleScope.launch {
                flow.debounce(1000L).collectLatest { searchQuery ->
                    val filteredList = mainViewModel.getFilteredUsers(searchQuery)
                    friendsListAdapter.submitList(filteredList)
                    binding.tvEmpty.isVisible = filteredList.isEmpty()
                }
            }

            friendsListAdapter = FriendsListAdapter(this@AddFriendsFragment)
            rvFriends.adapter = friendsListAdapter

            btnPendingRequests.setOnClickListener {
                val friendRequestsBottomSheet = FriendRequestsBottomSheet.newInstance()
                friendRequestsBottomSheet.show(
                    childFragmentManager,
                    FriendRequestsBottomSheet::class.java.simpleName
                )
            }

        }
    }

    private fun setupObservers() {
        mainViewModel.usersList.observe(this) {
            when (it) {
                is Response.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.searchLayout.searchContainer.isEnabled = false
                    binding.searchLayout.searchContainer.alpha = 0.5f
                }

                is Response.Error -> {
                    binding.progressBar.isVisible = false
                    binding.searchLayout.searchContainer.isEnabled = true
                    binding.searchLayout.searchContainer.alpha = 1f
                    showToast(it.errorMessage.toString())
                }

                is Response.Success -> {
                    binding.progressBar.isVisible = false
                    binding.searchLayout.searchContainer.isEnabled = true
                    binding.searchLayout.searchContainer.alpha = 1f

                }
            }
        }

        mainViewModel.requestMap.observe(this) {
            Log.e("TESTING2", "setupObservers: it = $it", )
            val idx = it.values.indexOfFirst {
                it == FirebasePaths.FRIENDS_STATUS_REQUEST_RECEIVED
            }
            binding.btnPendingRequests.isVisible = idx >= 0 && idx < it.values.size
        }
    }

    override fun onFriendClicked(friendItem: FriendsListItem) {
        mainViewModel.sendFriendRequest(friendItem.uuid)
    }

    private fun showToast(msg: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

}