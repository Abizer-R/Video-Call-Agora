package com.example.teachjr.ui.main

import PreferenceManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.teachjr.data.auth.model.CallStatus
import com.example.teachjr.databinding.ActivityMainBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.main.adapter.FriendsListAdapter
import com.example.teachjr.ui.videoCall.VideoCallActivity
import com.example.teachjr.utils.ConnectionLiveData
import com.example.teachjr.utils.FirebasePaths.CALL_STATUS_INCOMING_REQUEST
import com.example.teachjr.utils.FirebasePaths.CALL_STATUS_NOTHING
import com.example.teachjr.utils.sealedClasses.Response
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var binding: ActivityMainBinding

    private lateinit var connectionLiveData: ConnectionLiveData
    private var connectionStatus = true

    private val mainViewModel by viewModels<MainViewModel>()

    private var preferenceManager: PreferenceManager? = null

    private val friendsListAdapter = FriendsListAdapter(
        onItemClicked = { item ->
            mainViewModel.otherUid = item.uuid
            mainViewModel.pushCallNotification(
                item.uuid,
                callStatus = CallStatus(
                    otherUserName = preferenceManager?.getUsername(),
                    otherUserEmail = preferenceManager?.getEmail(),
                    otherUserUuid = FirebaseAuth.getInstance().currentUser?.uid,
                    status = CALL_STATUS_INCOMING_REQUEST
                )
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        preferenceManager = PreferenceManager(this)
        if(preferenceManager?.getUsername().isNullOrBlank()) {
            mainViewModel.getUserDetails()
        } else {
            showUserDetails()
        }

        setupUI()
        setupObservers()
        setupConnectionLiveData()

        mainViewModel.getFriendsList()
    }

    private fun setupUI() {
        with(binding) {
            btnLogout.setOnClickListener {
                preferenceManager?.logoutUser()
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, AuthActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }

            rvFriends.adapter = friendsListAdapter

            btnAccept.setOnClickListener {
                startActivity(Intent(this@MainActivity, VideoCallActivity::class.java).apply {
                    putExtra(VideoCallActivity.CHANNEL_NAME_EXTRA, mainViewModel.callStatus.value?.otherUserUuid)
                })
            }

            btnReject.setOnClickListener {
                val userUid = FirebaseAuth.getInstance().currentUser?.uid!!
                mainViewModel.pushCallNotification(
                    userUid,
                    callStatus = CallStatus(
                        otherUserName = preferenceManager?.getUsername(),
                        otherUserEmail = preferenceManager?.getEmail(),
                        otherUserUuid = userUid,
                        status = CALL_STATUS_NOTHING
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
                        friendsListAdapter.updateList(it.data)
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
                    CALL_STATUS_INCOMING_REQUEST -> {
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
                    startActivity(Intent(this@MainActivity, VideoCallActivity::class.java).apply {
                        putExtra(VideoCallActivity.CHANNEL_NAME_EXTRA, FirebaseAuth.getInstance().currentUser?.uid)
                        putExtra(VideoCallActivity.UUID_EXTRA, mainViewModel.otherUid)
                    })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager = null
    }



    private fun setupConnectionLiveData() {
        connectionLiveData = ConnectionLiveData(this)

        connectionLiveData.observe(this) { isConnected ->
            if(isConnected && connectionStatus == false) {
                connectionStatus = true
                updateConnectionTextView(true)
            } else if(!isConnected){
                connectionStatus = false
                updateConnectionTextView(false)
            }
        }
    }

    private fun updateConnectionTextView(isConnected: Boolean) {
        if(isConnected) {
            binding.tvNoConnection.setBackgroundColor(Color.parseColor("#419b45"))
            binding.tvNoConnection.text = "Back Online"
            lifecycleScope.launch(Dispatchers.IO) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    binding.tvNoConnection.visibility = View.GONE
                }
            }

        } else {
            binding.tvNoConnection.setBackgroundColor(Color.parseColor("#ED4134"))
            binding.tvNoConnection.text = "No Connection"
            binding.tvNoConnection.visibility = View.VISIBLE
        }
    }

    private fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }
}