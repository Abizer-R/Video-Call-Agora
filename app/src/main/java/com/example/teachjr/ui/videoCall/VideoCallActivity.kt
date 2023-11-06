package com.example.teachjr.ui.videoCall

import PreferenceManager
import android.Manifest
import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.teachjr.data.auth.model.CallStatus
import com.example.teachjr.databinding.ActivityMainBinding
import com.example.teachjr.databinding.ActivityVideoCallBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.main.MainViewModel
import com.example.teachjr.utils.ConnectionLiveData
import com.example.teachjr.utils.FirebasePaths
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class VideoCallActivity : AppCompatActivity() {

    private val TAG = VideoCallActivity::class.java.simpleName
    private lateinit var binding: ActivityVideoCallBinding

    private lateinit var connectionLiveData: ConnectionLiveData
    private var connectionStatus = true

    private var channelName: String? = null
    private var otherUid: String? = null

    private val mainViewModel by viewModels<MainViewModel>()

    private var preferenceManager: PreferenceManager? = null

    /**
     * AGORA STUFF START HERE ----------
     */

    private val appId = "12b8c22e8a614433b6caa7ea43b3afb6"
//    var appCertificate = "b5065fbfa5ed4d8aba0c25de974502b1"
    var expirationTimeInSeconds = 3600
//    private val channelName = "abizer_rampurawala"
    private val token = "007eJxTYGBQFdpr7bonS13mv8fp+b+VDPWkinIEXzp2Rn25t/hxs4ECg6FRkkWykVGqRaKZoYmJsXGSWXJionlqoolxknFiWpLZ81nuqQ2BjAwHFd6yMDJAIIgvxJCYlFmVWhRflJhbUFqUWJ6Yk8jAAAC3IiSq"
    private val currUid: Int = System.currentTimeMillis().toInt()
    private var isJoined = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote host joining the channel to get the uid of the host.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showToast("Remote user joined $uid")

            // Set the remote video view
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showToast("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showToast("Remote user offline $uid $reason")
            runOnUiThread { remoteSurfaceView!!.visibility = View.GONE }
        }
    }

    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine?.enableVideo()
        } catch (e: Exception) {
            showToast(e.toString())
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        remoteSurfaceView = SurfaceView(baseContext)
//        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        binding.remoteVideoViewContainer.addView(remoteSurfaceView)

        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )

        binding.cvLocalView.bringToFront()
    }

    private fun setupLocalVideo() {
        localSurfaceView = SurfaceView(baseContext)
        localSurfaceView!!.setZOrderMediaOverlay(true)
        binding.localVideoViewContainer.addView(localSurfaceView)

        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                currUid
            )
        )
        // Display RemoteSurfaceView.
        localSurfaceView!!.visibility = View.VISIBLE
    }

    private fun joinCall(channelName: String) {
        Toast.makeText(this@VideoCallActivity, "ChannelName = $channelName", Toast.LENGTH_LONG).show()
        Log.e("TESTING", "joinCall: ChannelName = $channelName", )
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()

            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            setupLocalVideo()
            localSurfaceView!!.visibility = View.VISIBLE
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(token, "abizer_rampurawala", currUid, options)
        } else {
            showToast("Permissions not granted")
        }
    }

    private fun leaveCall() {
        if (!isJoined) {
            showToast("Join a channel first")
        } else {
            agoraEngine!!.leaveChannel()
            showToast("You left the channel")
            if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
            if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
            isJoined = false

            otherUid?.let {
                val userUid = FirebaseAuth.getInstance().currentUser?.uid!!
                mainViewModel.pushCallNotification(
                    it,
                    callStatus = CallStatus(
                        otherUserName = preferenceManager?.getUsername(),
                        otherUserEmail = preferenceManager?.getEmail(),
                        otherUserUuid = userUid,
                        status = FirebasePaths.CALL_STATUS_NOTHING
                    )
                )
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine?.stopPreview()
        agoraEngine?.leaveChannel()

        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()

        preferenceManager = null
    }

    /**
     * --------- AGORA STUFF END HERE
     */




//    private lateinit var appBarConfiguration: AppBarConfiguration

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1] ) != PackageManager.PERMISSION_GRANTED)
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Log.i(TAG, "StudentTesting: Student Activity Created")

        preferenceManager = PreferenceManager(this)


        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        setupVideoSDKEngine()

        setupUI()
        setupConnectionLiveData()

        channelName = intent.getStringExtra(CHANNEL_NAME_EXTRA)
        otherUid = intent.getStringExtra(UUID_EXTRA)
        if(channelName.isNullOrBlank()) {
            showToast("Something went wrong")
        } else {
            joinCall(channelName!!)
        }
    }

    private fun setupUI() {
        with(binding) {
//            btnJoin.setOnClickListener {
//                if(channelName.isNullOrBlank()) {
//                    showToast("Something went wrong")
//                } else {
//                    joinCall(channelName!!)
//                }
//            }

            btnLeave.setOnClickListener {
                leaveCall()
            }

//            btnLogout.setOnClickListener {
//                FirebaseAuth.getInstance().signOut()
//                val intent = Intent(this@VideoCallActivity, AuthActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                startActivity(intent)
//                finish()
//            }
        }
    }

    private fun setupConnectionLiveData() {
        /**
         * We cant use Network callback and connectivity manager until onCreate is called
         */
        connectionLiveData = ConnectionLiveData(this)

        connectionLiveData.observe(this) { isConnected ->
            /** we need to check if it is not already true.
             * Otherwise, this will get triggered on activity's launch as well.
             */
            if(isConnected && connectionStatus == false) {
                Log.i(TAG, "StudentTesting_ACTIVITY: Internet Available")
                connectionStatus = true
                updateConnectionTextView(true)

            } else if(!isConnected){
                Log.i(TAG, "StudentTesting_ACTIVITY: Internet NOT Available")
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
            Toast.makeText(this@VideoCallActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val CHANNEL_NAME_EXTRA = "CHANNEL_NAME_EXTRA"
        const val UUID_EXTRA = "UUID_EXTRA"
    }
}