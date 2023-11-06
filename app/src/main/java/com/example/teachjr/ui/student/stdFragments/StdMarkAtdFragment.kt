package com.example.teachjr.ui.student.stdFragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdMarkAtdBinding
import com.example.teachjr.ui.viewmodels.studentViewModels.SharedStdViewModel
import com.example.teachjr.ui.viewmodels.studentViewModels.StdMarkAtdViewModel
import com.example.teachjr.utils.Adapter_ViewModel_Utils
import com.example.teachjr.utils.sealedClasses.AttendanceStatusStd
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Permissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

@AndroidEntryPoint
class StdMarkAtdFragment : Fragment() {

    // TODO: Create a child observer on 'isAtdOngoing' and create a layout for "Unfortunately, Attendance is over", if atd is not marked yet

    private val TAG = StdMarkAtdFragment::class.java.simpleName
    private lateinit var binding: FragmentStdMarkAtdBinding

    private val markAtdViewModel by viewModels<StdMarkAtdViewModel>()
    private val sharedStdViewModel by activityViewModels<SharedStdViewModel>()

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

    private val SERVICE_TYPE = "_presence._tcp"

    private lateinit var confirmDialog: AlertDialog

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if(granted) {
                startAttendance()
            } else {
                // TODO: Display a message that attendance cannot be initiated without permission
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStdMarkAtdBinding.inflate(layoutInflater)
        Log.i(TAG, "StudentTesting_MarkAtdPage: Student MarkAtdPage Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showFadeAnimation()
        initialSetup()
        initiateAtdMarking()
        setupObservers()
    }

    private fun showFadeAnimation() {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        binding.toolbar.startAnimation(animation)
        binding.atdLecDetails.startAnimation(animation)
    }

    private fun initialSetup() {
//        binding.toolbar.title = "Mark Attendance"
        binding.icClose.setOnClickListener {
            checkExitConditions()
        }

        binding.tvCourseName.text = sharedStdViewModel.courseName!!
        binding.tvCourseCode.text = sharedStdViewModel.courseCode!!
        binding.tvLecDate.text = Adapter_ViewModel_Utils.getFormattedDate2(Calendar.getInstance().timeInMillis.toString())

        createConfirmDialog()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                checkExitConditions()
            }

        })

        binding.fabTryAgain.setOnClickListener {
            initiateAtdMarking()
        }
    }

    private fun setupObservers() {
        markAtdViewModel.timerStatus.observe(viewLifecycleOwner) {
            when(it) {
                null -> { hideTimerLayout() }
                else -> {
                    showTimerLayout(Adapter_ViewModel_Utils.getFormattedTime(abs(it)))
                }
            }
        }

        markAtdViewModel.atdStatus.observe(viewLifecycleOwner) {
            when(it) {
                is AttendanceStatusStd.DiscoveringTimestamp -> {
                    markAtdViewModel.updateIsDiscovering(true)

                    showAtdStatus()
                    showTimerLayout(it.remainingTime!!)
                    Log.i(TAG, "WIFI_SD_Observer_Status: Discovering Timestamp")
                }

                is AttendanceStatusStd.TimestampNotFound -> {
                    // TODO : Show error msg "Uh-Oh, looks like you are not in range" with a try again button
                }

                is AttendanceStatusStd.TimestampDiscovered -> {
                    Log.i(TAG, "WIFI_SD_Observer_Status: Timestamp Discovered")
                    markAtdViewModel.updateIsDiscovering(false)

                    /**
                     * Marking Attendance
                     */
                    markAtdViewModel.martAtd(sharedStdViewModel.courseCode!!, it.timestamp.toString(),
                        sharedStdViewModel.userDetails!!.sem_sec!!, sharedStdViewModel.userDetails!!.enrollment!!)
                }
                is AttendanceStatusStd.AttendanceMarked -> {
                    Log.i(TAG, "WIFI_SD_Observer_Status: Attendance Marked")
                    /**
                     * Broadcasting Timestamp
                     */
                    broadcastTimestamp(it.timestamp.toString())
                    binding.tvMarkingAtd.text = "Almost done."
                    binding.tvPleaseWait.text = "Do not exit the screen."
                }

                is AttendanceStatusStd.BroadcastComplete -> {
                    Log.i(TAG, "WIFI_SD_Observer_Status: Broadcast Completed")
                    hideTimerLayout()
                    showSuccessful()
                }

                is AttendanceStatusStd.Error -> {
                    markAtdViewModel.updateIsDiscovering(false)
                    markAtdViewModel.stopTimer()
                    Log.i(TAG, "WIFI_SD_Observer_Status: Error. ErrorMsg - ${it.errorMessage}")
                    showError()
                }
            }
        }
    }

    private fun initiateAtdMarking() {
        if(Permissions.hasAccessCoarseLocation(activity as Context)
            && Permissions.hasAccessFineLocation(activity as Context)
            && (Build.VERSION.SDK_INT >= 33 && Permissions.hasNearbyWifiDevices(activity as Context))) {

            if(markAtdViewModel.isDiscovering == false) {
//                checkGpsAndStartAttendance()
                startAttendance()
            }

        } else {
            permissionRequestLauncher.launch(Permissions.getPendingPermissions(activity as Activity))
        }
    }

    private fun startAttendance() {
        Log.i(TAG, "StudentTesting_MarkAtdPage: startAttendance() called")

        if(sharedStdViewModel.courseValuesNotNull() && sharedStdViewModel.userDetails != null) {

            if(manager == null) {
                val applicationContext = (activity as Activity).applicationContext
                manager = applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            }
            if(channel == null) {
                channel = manager?.initialize(context, Looper.getMainLooper(), null)
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val serviceInstance = "${sharedStdViewModel.userDetails!!.sem_sec}/${sharedStdViewModel.courseCode}"
                markAtdViewModel.discoverTimestamp(manager!!, channel!!, serviceInstance)
            }

        } else {
            Toast.makeText(context, "Null error. Something went wrong.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        if(channel != null) {
            manager?.clearLocalServices(channel, null)
            markAtdViewModel.removeServiceRequest(manager!!, channel!!)
        }
    }

    private fun broadcastTimestamp(timestamp: String) {
        val serviceInstance = "${sharedStdViewModel.userDetails!!.sem_sec}/${sharedStdViewModel.courseCode}"
        val record = mapOf( FirebasePaths.TIMESTAMP to timestamp )
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceInstance, SERVICE_TYPE, record)
        if(manager == null) {
            val applicationContext = (activity as Activity).applicationContext
            manager = applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        }
        if(channel == null) {
            channel = manager?.initialize(context, Looper.getMainLooper(), null)
        }
        markAtdViewModel.broadcastTimestamp(manager!!, channel!!, serviceInfo)
    }

    private fun checkExitConditions() {
        when(markAtdViewModel.atdStatus.value) {
            is AttendanceStatusStd.DiscoveringTimestamp -> confirmDialog.show()
            is AttendanceStatusStd.TimestampDiscovered -> confirmDialog.show()
            is AttendanceStatusStd.AttendanceMarked -> {
                Toast.makeText(context, "You cannot exit right now. Please Wait a few seconds", Toast.LENGTH_SHORT).show()
            }
            else -> findNavController().navigateUp()
        }
    }

    private fun createConfirmDialog() {
        confirmDialog = AlertDialog.Builder(context)
            .setTitle("Cancel Attendance?")
            .setMessage("Your attendance hasn't been marked yet")
            .setPositiveButton("Yes") { _, _ ->
                markAtdViewModel.removeServiceRequest(manager!!, channel!!)
                findNavController().navigateUp()
            }
            .setNegativeButton("No", null)
            .create()
    }

    private fun showAtdStatus() {
        binding.errorLayout.visibility = View.INVISIBLE

        binding.atdStatusLayout.visibility = View.VISIBLE
        binding.loadingAnimation.playAnimation()
    }

    private fun showTimerLayout(remainingTime: String) {
        binding.tvEstimatedTime.text = remainingTime
    }

    private fun hideTimerLayout() {
        binding.timerLayout.visibility = View.GONE
    }

    private fun showError() {
        binding.atdStatusLayout.visibility = View.INVISIBLE

        binding.errorLayout.visibility = View.VISIBLE
        binding.loadingAnimation.cancelAnimation()
    }

    private fun showSuccessful() {
        binding.atdStatusLayout.visibility = View.GONE
        binding.loadingAnimation.cancelAnimation()


        binding.errorLayout.visibility = View.VISIBLE
        binding.icError.setImageResource(R.drawable.ic_baseline_verified_64)
        binding.tvErrorMessage.text = "Attendance has been marked successfully."
        binding.tvSolutionMsg.text = "You can exit the screen now."
        binding.fabTryAgain.visibility = View.GONE

        // Setting these invisible because I need a margin below the "exit now" TextView
        binding.tvSolution1.visibility = View.INVISIBLE
        binding.tvSolution2.visibility = View.INVISIBLE
    }

}