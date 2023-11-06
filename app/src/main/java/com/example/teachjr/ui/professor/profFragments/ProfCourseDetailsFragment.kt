package com.example.teachjr.ui.professor.profFragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfCourseDetailsBinding
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfCourseViewModel
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.example.teachjr.utils.Adapter_ViewModel_Utils
import com.example.teachjr.utils.AnimationExtUtil.startAnimation
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class ProfCourseDetailsFragment : Fragment() {

    private val TAG = ProfCourseDetailsFragment::class.java.simpleName
    private lateinit var binding: FragmentProfCourseDetailsBinding

    private val courseViewModel by viewModels<ProfCourseViewModel>()
    private val sharedProfViewModel by activityViewModels<SharedProfViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfCourseDetailsBinding.inflate(layoutInflater)
        Log.i(TAG, "ProfessorTesting_CoursePage: Professor CoursePage Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {

            //Adding up navigation
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_white_32)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        initialSetup()
    }

    private fun initialSetup() {

        if(sharedProfViewModel.courseValuesNotNull()) {
            binding.tvCourseCode.text = sharedProfViewModel.courseCode
            binding.tvCourseName.text = sharedProfViewModel.courseName
            binding.tvSection.text = Adapter_ViewModel_Utils.getSection(sharedProfViewModel.sem_sec.toString())

            courseViewModel.getLectureCount(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!)

        } else {
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        }

        courseViewModel.lecCount.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {
                    showLoading()
//                    binding.progressBar.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    // TODO: Error Layout
                    stopLoading()
                    Log.i(TAG, "ProfessorTesting_CoursePage: lecCount_Error - ${it.errorMessage}")
                    Toast.makeText(context, "Error: ${it.errorMessage}", Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    stopLoading()
                    Log.i(TAG, "ProfessorTesting_CoursePage: lecCount - ${it.data}")
//                    lecCount = it.data
                    sharedProfViewModel.updateLecCount(it.data!!)
//                    binding.progressBar.visibility = View.GONE
                    if(it.data != null) {
                        updateViews(it.data.toString())
                    } else {
                        Toast.makeText(context, "lec count was null", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnAtdReport.setOnClickListener {
            findNavController().navigate(R.id.action_profCourseDetailsFragment_to_profAtdReportFragment)
        }
    }

    private fun updateViews(lecCount: String) {
        stopLoading()

        binding.tvUserName.text = sharedProfViewModel.userDetails!!.name
        binding.tvCurrDate.text = Adapter_ViewModel_Utils.getFormattedDate2(Calendar.getInstance().timeInMillis.toString())
        binding.tvLecCount.text = "Total Lectures: $lecCount"

        setupFAB()
    }

    private fun setupFAB() {

        binding.fabInitiateAtd.setOnClickListener {

            val mLocationManager = (activity as Context).getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // Checking GPS is enabled
            val mGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if(mGPS) {
                binding.toolbar.visibility = View.INVISIBLE
                explodeFAB()
            } else {
                showGPSAlert()
            }
        }
    }

    private fun explodeFAB() {
        // Setting up the FAB explosion
        val animation = AnimationUtils.loadAnimation(context, R.anim.fab_explosion_anim)
        animation.apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
        }

        binding.fabInitiateAtd.visibility = View.INVISIBLE
        binding.fabShape.visibility = View.VISIBLE
        // We will use our own extension func that we made (ViewExt.kt) and not the default one
        binding.fabShape.startAnimation(animation) {
            // This callback will be called when the animation ends
            findNavController().navigate(R.id.action_profCourseDetailsFragment_to_profMarkAtdFragment)
        }
    }

    private fun showGPSAlert() {
        val gpsAlertBuilder = AlertDialog.Builder(context)
        gpsAlertBuilder.setMessage("GPS is required to mark the attendance. Please enable it and try again.")
            .setCancelable(true)
            .setPositiveButton("Okay", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0?.cancel()
                }
            })

        val gpsAlert = gpsAlertBuilder.create()
        gpsAlert.show()
    }

    private fun showLoading() {
        binding.layoutLoaded.visibility = View.GONE
        binding.linearLayoutCompat.visibility = View.GONE
        binding.fabInitiateAtd.visibility = View.GONE

        binding.layoutLoading.visibility = View.VISIBLE
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.loadingAnimation.playAnimation()
    }

    private fun stopLoading() {
        binding.layoutLoaded.visibility = View.VISIBLE
        binding.linearLayoutCompat.visibility = View.VISIBLE
        binding.fabInitiateAtd.visibility = View.VISIBLE

        binding.layoutLoading.visibility = View.GONE
        binding.loadingAnimation.visibility = View.GONE
        binding.loadingAnimation.cancelAnimation()

//        binding.scrollView.requestFocus(View.FOCUS_UP)
    }

}