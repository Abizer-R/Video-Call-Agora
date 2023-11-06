package com.example.teachjr.ui.professor.profFragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
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
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.teachjr.R
import com.example.teachjr.data.model.RvProfMarkAtdListItem
import com.example.teachjr.databinding.FragmentProfMarkAtdBinding
import com.example.teachjr.ui.adapters.AttendanceAdapter
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfMarkAtdViewModel
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.example.teachjr.utils.*
import com.example.teachjr.utils.sealedClasses.AttendanceStatusProf
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfMarkAtdFragment : Fragment() {
    /**
     * TODO: For editing attendance after endAttendance(), we can just get the lecList,
     * TODO: sort that list, and add the attendance in the last one
     */

    private val TAG = ProfMarkAtdFragment::class.java.simpleName
    private lateinit var binding: FragmentProfMarkAtdBinding

    private val markAtdViewModel by viewModels<ProfMarkAtdViewModel>()
    private val sharedProfViewModel by activityViewModels<SharedProfViewModel>()

    private val attendanceAdapter = AttendanceAdapter(::onItemClicked)
    /**
     * We don't want same function to be invoked when nonEditable RvItem is clicked
     */
    private val attendanceAdapter2 = AttendanceAdapter( onItemClicked = {studentList, pos ->})

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
//                if(markAtdViewModel.isAtdOngoing) {
//                    endAttendance()
//                } else {
//                    startAttendance()
//                }
            } else {
                // TODO: Display a message that attendance cannot be initiated without permission
                Toast.makeText(context, "Attendance can't be initiated without permission", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfMarkAtdBinding.inflate(layoutInflater)
        Log.i(TAG, "ProfessorTesting_MarkAtdPage: Professor MarkAtdPage Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showFadeAnimation()
        initialSetup()
        setupViews()
        initiateAtdMarking()
        setupObservers()
    }

    private fun showFadeAnimation() {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        binding.toolbar.startAnimation(animation)
    }

    private fun initialSetup() {

        binding.icClose.setOnClickListener {
            checkExitConditions()
        }

        createConfirmDialog()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                checkExitConditions()
//                if(markAtdViewModel.isAtdOngoing) {
//                    confirmDialog.show()
//                } else {
//                    findNavController().navigateUp()
//                }
            }

        })

        binding.fabTryAgain.setOnClickListener {
            initiateAtdMarking()
        }

        binding.fabEndAtd.setOnClickListener {
            endAttendance()
        }

        binding.btnEdit.setOnClickListener {
            if(markAtdViewModel.isEditing) {
                if(haveUnsavedChanges()) {
                    saveChanges()
                } else {
                    stopEditing()
                }
            } else {
                startEditing()
            }
        }
    }

    private fun setupViews() {

        binding.rvAttendance.apply {
            hasFixedSize()
            adapter = attendanceAdapter
            layoutManager = GridLayoutManager(context, 4)
        }

        binding.rvAttendanceNonEditable.apply {
            hasFixedSize()
            adapter = attendanceAdapter2
            layoutManager = GridLayoutManager(context, 4)
        }

        binding.svMarkAtd.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = binding.svMarkAtd.scrollY
            if (scrollY > 0) {
                binding.fabEndAtd.hide()
            } else {
                if(!markAtdViewModel.isEditing) {
                    binding.fabEndAtd.show()
                }
            }
        }
    }

    private fun setupObservers() {

        markAtdViewModel.atdStatus.observe(viewLifecycleOwner) {
            when(it) {
                is AttendanceStatusProf.FetchingTimestamp -> {
                    markAtdViewModel.updateIsAtdOngoing(true)

                    showAtdStatus(Constants.INITIATING_ATTENDANCE)
//                    showTimerLayout(it.remainingTime!!)
                    Log.i(TAG, "WIFI_SD_Observer_Status: Fetching Timestamp")

                }
                is AttendanceStatusProf.Initiated -> {
                    // TODO: Show (present / total) students as well... somewhere....
                    showAtdStatus(Constants.MARKING_ATTENDANCE)

                    /**
                     * Applying LifeCycleScope so that when we exit the fragment,
                     * the flow gets cancelled automatically.
                     * Otherwise, the childEventListener (in Repo) would
                     * keep listening for updates in the lec location
                     */

                    broadcastTimestamp(it.timestamp!!)
                    lifecycleScope.launch {
                        /**
                         * This is a blocking call, therefore it is placed in its different coroutine
                         */
                        markAtdViewModel.observeAtd(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!, it.timestamp)
                    }
                }
                is AttendanceStatusProf.Ended -> {
                    showSuccessful()
                    markAtdViewModel.updateIsAtdOngoing(false)
                    Toast.makeText(context, "Attendance is over", Toast.LENGTH_SHORT).show()
                }
                is AttendanceStatusProf.Error -> {
                    markAtdViewModel.updateIsAtdOngoing(false)
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                    showError()
                }
            }
        }

        markAtdViewModel.presentList.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {}
                is Response.Error -> Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                is Response.Success -> {

                    if(markAtdViewModel.isEditing) {
                        updateRecyclerViewsEDT()
                    } else {
                        updateRecyclerViewsEDTEnded(it.data!!)
                    }
                    binding.tvPresentCount.text = "${markAtdViewModel.presentCount} / ${it.data!!.size}"
                }
            }
        }

        markAtdViewModel.saveManualStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {
                    binding.cvRVLayoutEditable.alpha = 0.5F
                    binding.cvRVLayoutNotEditable.alpha = 0.5F
                    binding.cvSaveManualAtd.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    // TODO: Man, do something here
                }
                is Response.Success -> {
                    stopEditing()
                }
            }
        }
    }

    private fun broadcastTimestamp(timestamp: String) {
        val serviceInstance = "${sharedProfViewModel.sem_sec}/${sharedProfViewModel.courseCode}"
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

    private fun initiateAtdMarking() {
        if(Permissions.hasAccessCoarseLocation(activity as Context)
            && Permissions.hasAccessFineLocation(activity as Context)
            && (Build.VERSION.SDK_INT >= 33 && Permissions.hasNearbyWifiDevices(activity as Context))) {


            if(markAtdViewModel.isAtdOngoing == false) {
                startAttendance()
            }

        } else {
            permissionRequestLauncher.launch(Permissions.getPendingPermissions(activity as Activity))
        }
    }

    private fun startAttendance() {
        Log.i(TAG, "ProfessorTesting_MarkAtdPage: startAttendance() called")

        if(sharedProfViewModel.courseValuesNotNull() && sharedProfViewModel.lecCountNotNull()) {
            // TODO: Give an option to choose for manually closing atd or closing in 2 min

            // Makes a new Lec doc and fetches list of enrolled students
            markAtdViewModel.initAtd(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!, sharedProfViewModel.lecCount!!)
        } else {
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()

        }
    }

    private fun endAttendance() {
        /**
         * This won't be triggered while editing, because fabEndBtn will be disabled
         */
        val timestamp = markAtdViewModel.atdStatus.value?.timestamp
        if(timestamp != null) {
            markAtdViewModel.endAttendance(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!, timestamp)
            lifecycleScope.launch {
                markAtdViewModel.stopBroadcasting(manager!!, channel!!)
            }
            sharedProfViewModel.updateLecCount(sharedProfViewModel.lecCount!! + 1)
        } else {
            Log.i(TAG, "ProfessorTesting_MarkAtdPage: null timestamp")
            Toast.makeText(context, "null timestamp", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        if(channel != null) {
            manager?.clearLocalServices(channel, null)
        }
    }

    private fun checkExitConditions() {
        if(markAtdViewModel.isEditing && haveUnsavedChanges()) {
            Toast.makeText(context, "Can't exit, you have unsaved changes.", Toast.LENGTH_SHORT).show()

        } else if(markAtdViewModel.isAtdOngoing) {
            confirmDialog.show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun createConfirmDialog() {
        confirmDialog = AlertDialog.Builder(context)
            .setTitle(Constants.ALERT_DIALOG_END_ATTENDANCE_PRIMARY)
            .setMessage(Constants.ALERT_DIALOG_END_ATTENDANCE_SECONDARY)
            .setPositiveButton(Constants.YES) { _, _ ->
                endAttendance()
                findNavController().navigateUp()
            }
            .setNegativeButton(Constants.NO, null)
            .create()
    }

    private fun showAtdStatus(atdString: String) {
        binding.cvErrorLayout.visibility = View.GONE

        binding.cvAtdStatusLayout.visibility = View.VISIBLE
//        binding.cvRVLayout.visibility = View.VISIBLE
        binding.fabEndAtd.visibility = View.VISIBLE
        if(atdString.equals(Constants.MARKING_ATTENDANCE)) {
            binding.loadingAnimation.visibility = View.VISIBLE
            binding.loadingAnimation.playAnimation()
        } else {
            binding.loadingAnimation.visibility = View.GONE
        }

        binding.tvAtdStatus.text = atdString

        binding.btnEdit.visibility = View.VISIBLE

//        binding.tvNoAtd.visibility = View.VISIBLE
//        binding.rvAttendance.visibility = View.GONE

    }

    private fun showError() {
        binding.cvAtdStatusLayout.visibility = View.GONE
        binding.cvRVLayoutEditable.visibility = View.GONE
        binding.fabEndAtd.visibility = View.GONE

        binding.cvErrorLayout.visibility = View.VISIBLE
        binding.loadingAnimation.cancelAnimation()
    }


    private fun showSuccessful() {
        binding.cvAtdStatusLayout.visibility = View.GONE
        binding.fabEndAtd.visibility = View.GONE
        binding.loadingAnimation.cancelAnimation()


        binding.cvErrorLayout.visibility = View.VISIBLE
        binding.icError.setImageResource(R.drawable.ic_baseline_verified_64)
        binding.tvErrorMessage.text = Constants.ATTENDANCE_MARKED_SUCCESSFULLY
        binding.tvSolutionMsg.text = Constants.EXIT_SCREEN_ALLOWED_PROMPT
        binding.fabTryAgain.visibility = View.GONE

        // Setting these invisible because I need a margin below the "exit now" TextView
        binding.tvSolution1.visibility = View.INVISIBLE
        binding.tvSolution2.visibility = View.INVISIBLE

        binding.btnEdit.visibility = View.GONE
    }

    private fun getEditableList() : List<RvProfMarkAtdListItem> {
        val editableStdList: MutableList<RvProfMarkAtdListItem> = ArrayList()
        markAtdViewModel.presentList.value!!.data!!.forEach { mpElement ->
            if(mpElement.value.atdStatus == Constants.ATD_STATUS_ABSENT || mpElement.value.atdStatus == Constants.ATD_STATUS_PRESENT_MANUAL)
                editableStdList.add(mpElement.value)
        }
        return editableStdList
    }

    private fun getNonEditableList() : List<RvProfMarkAtdListItem>{
        val nonEditableStdList: MutableList<RvProfMarkAtdListItem> = ArrayList()
        markAtdViewModel.presentList.value!!.data!!.forEach { mpElement ->
            if(mpElement.value.atdStatus == Constants.ATD_STATUS_PRESENT_WIFI_SD)
                nonEditableStdList.add(mpElement.value)
        }
        return nonEditableStdList
    }

    private fun updateRecyclerViewsEDT() {
        attendanceAdapter.updateList(getEditableList())
        attendanceAdapter2.updateList(getNonEditableList())
    }
    private fun updateRecyclerViewsEDTEnded(mp: Map<String, RvProfMarkAtdListItem>) {
        val stdList: MutableList<RvProfMarkAtdListItem> = ArrayList()
        mp.forEach { mpElement ->
            stdList.add(mpElement.value)
        }
        attendanceAdapter.updateList(stdList)
    }

    private fun haveUnsavedChanges(): Boolean {
        return attendanceAdapter.getManualAtdList().isNotEmpty()
    }

    private fun startEditing() {
        markAtdViewModel.updateIsEditing(true)
        binding.btnEdit.apply {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_24)
            text = "DONE"
        }
        binding.cvRVLayoutNotEditable.visibility = View.VISIBLE
        binding.fabEndAtd.apply {
//            isClickable = false
//            alpha = 0.5F
            hide()
        }

        updateRecyclerViewsEDT()
    }

    private fun stopEditing() {
        binding.cvRVLayoutEditable.alpha = 1F
        binding.cvRVLayoutNotEditable.alpha = 1F
        binding.cvRVLayoutNotEditable.visibility = View.GONE
        binding.cvSaveManualAtd.visibility = View.GONE
        binding.fabEndAtd.apply {
//            isClickable = true
//            alpha = 1F
            show()
        }


        markAtdViewModel.updateIsEditing(false)
        binding.btnEdit.apply {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_edit_24)
            text = "EDIT"
        }

        updateRecyclerViewsEDTEnded(markAtdViewModel.presentList.value!!.data!!)
    }

    private fun saveChanges() {
        val stdList = attendanceAdapter.getManualAtdList()
        if(stdList.isNotEmpty()) {
            markAtdViewModel.saveManualAtd(
                sem_sec = sharedProfViewModel.sem_sec!!,
                courseCode = sharedProfViewModel.courseCode!!,
                timestamp = markAtdViewModel.atdStatus.value!!.timestamp!!,
                students = stdList
            )
        }
    }

    private fun onItemClicked(studentList: List<RvProfMarkAtdListItem>, pos: Int) {
        if(markAtdViewModel.isEditing == false)
            return
        Log.i(TAG, "TESTING_onItemClicked: pos = $pos, std = ${studentList[pos]}")
        if(studentList[pos].atdStatus == Constants.ATD_STATUS_ABSENT) {
            studentList[pos].atdStatus = Constants.ATD_STATUS_PRESENT_MANUAL

        } else if(studentList[pos].atdStatus == Constants.ATD_STATUS_PRESENT_MANUAL){
            studentList[pos].atdStatus = Constants.ATD_STATUS_ABSENT
        }
        attendanceAdapter.updateList(studentList)
    }
}