package com.example.teachjr.ui.main.home


import PreferenceManager
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.example.teachjr.data.auth.model.CallStatus
import com.example.teachjr.data.auth.model.FriendsListItem
import com.example.teachjr.databinding.FragmentHomeBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.base.BaseFragment
import com.example.teachjr.ui.main.MainActivity
import com.example.teachjr.ui.main.MainViewModel
import com.example.teachjr.ui.main.adapter.FriendsListAdapter
import com.example.teachjr.ui.videoCall.VideoCallActivity
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.sealedClasses.Response
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
), FriendsListAdapter.FriendsAdapterListener {

    private val TAG = HomeFragment::class.java.simpleName

    private var preferenceManager: PreferenceManager? = null

    private val mainViewModel by activityViewModels<MainViewModel>()
    private lateinit var friendsListAdapter: FriendsListAdapter

    override fun initUserInterface(view: View?) {
        preferenceManager = PreferenceManager(requireContext())
        if(preferenceManager?.getUsername().isNullOrBlank()) {
            mainViewModel.getUserDetails()
        } else {
            showUserDetails()
        }

        setupUI()
        setupObservers()
    }


    private fun setupUI() {
        with(binding) {
            btnLogout.setOnClickListener {
                preferenceManager?.logoutUser()
                FirebaseAuth.getInstance().signOut()
                activity?.let { activity ->
                    val intent = Intent(activity, AuthActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    activity.finish()

                }
            }

            friendsListAdapter = FriendsListAdapter(this@HomeFragment)
            rvFriends.adapter = friendsListAdapter

            btnAccept.setOnClickListener {
                activity?.let { activity ->
                    startActivity(Intent(activity, VideoCallActivity::class.java).apply {
                        putExtra(VideoCallActivity.CHANNEL_NAME_EXTRA, mainViewModel.callStatus.value?.otherUserUuid)
                    })
                }

            }

            btnReject.setOnClickListener {
                val userUid = FirebaseAuth.getInstance().currentUser?.uid!!
                mainViewModel.pushCallNotification(
                    userUid,
                    callStatus = CallStatus(
                        otherUserName = preferenceManager?.getUsername(),
                        otherUserEmail = preferenceManager?.getEmail(),
                        otherUserUuid = userUid,
                        status = FirebasePaths.CALL_STATUS_NOTHING
                    )
                )
            }
        }
    }

    private fun showUserDetails() {
        binding.cvUserDetails.isVisible = true
        binding.tvCurrentName.text = preferenceManager?.getUsername()
        binding.tvCurrentEmail.text = preferenceManager?.getEmail()
    }

    private fun setupObservers() {
        mainViewModel.friendsList.observe(this) {
            when(it) {
                is Response.Loading -> {
//                    binding.progressBar.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    Log.i(TAG, "StudentTesting_HomePage: CourseList_Error - ${it.errorMessage}")
                    showToast(it.errorMessage.toString())
                }
                is Response.Success -> {
                    if(it.data != null) {
                        friendsListAdapter.submitList(it.data)
                    } else {
                        showToast("List is null")
                    }
                }
            }
        }

        mainViewModel.observeCallStatus()

        mainViewModel.callStatus.observe(this) {
            Log.e("TESTING", "setupObservers: callStatus = $it", )
            if(it.otherUserUuid.isNullOrBlank().not() && it.status.isNullOrBlank().not()) {

                when(it.status) {
                    FirebasePaths.CALL_STATUS_INCOMING_REQUEST -> {
                        binding.cvIncomingCall.isVisible = true
                        binding.tvOtherName.text = it.otherUserName
                    }

                    else -> {
                        binding.cvIncomingCall.isVisible = false
                    }
                }
            }
        }

        mainViewModel.userDetails.observe(this) {
            when(it) {
                is Response.Loading -> {
                }
                is Response.Error -> {
                    showToast("Couldn't fetch user details")
                }
                is Response.Success -> {

                    if(it.data != null) {
                        preferenceManager?.saveUsername(it.data.username)
                        preferenceManager?.saveEmail(it.data.email)
                        showUserDetails()
                    } else {
                        showToast("Couldn't fetch user details")
                    }
                }
            }
        }

        mainViewModel.outgoingCallStatus.observe(this) {
            when(it) {
                is Response.Error -> {
                    showToast(it.toString())
                }
                is Response.Loading -> {}
                is Response.Success -> {
                    activity?.let { activity ->
                        startActivity(Intent(activity, VideoCallActivity::class.java).apply {
                            putExtra(VideoCallActivity.CHANNEL_NAME_EXTRA, FirebaseAuth.getInstance().currentUser?.uid)
                            putExtra(VideoCallActivity.UUID_EXTRA, mainViewModel.otherUid)
                        })
                    }
                }
            }
        }
    }

    override fun onFriendClicked(friendItem: FriendsListItem, position: Int) {
        mainViewModel.otherUid = friendItem.uuid
        mainViewModel.pushCallNotification(
            friendItem.uuid,
            callStatus = CallStatus(
                otherUserName = preferenceManager?.getUsername(),
                otherUserEmail = preferenceManager?.getEmail(),
                otherUserUuid = FirebaseAuth.getInstance().currentUser?.uid,
                status = FirebasePaths.CALL_STATUS_INCOMING_REQUEST
            )
        )
    }

    override fun onRequestCancelled(friendItem: FriendsListItem, position: Int) {
        // pass
    }

    override fun onRequestAccepted(friendItem: FriendsListItem, position: Int) {
        // pass
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager = null
    }

    private fun showToast(msg: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

}