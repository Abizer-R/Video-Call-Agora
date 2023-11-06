package com.example.teachjr.ui.student.stdFragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdProfileBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.viewmodels.studentViewModels.SharedStdViewModel

class StdProfileFragment : Fragment() {

    private val TAG = StdProfileFragment::class.java.simpleName
    private lateinit var binding: FragmentStdProfileBinding

    private val sharedStdViewModel by activityViewModels<SharedStdViewModel>()

    private lateinit var confirmDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStdProfileBinding.inflate(layoutInflater)
        Log.i(TAG, "StudentTesting_ProfilePage: Student Profile Page Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {

            // Adding up navigation
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_white_32)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        createConfirmDialog()
        binding.btnLogout.setOnClickListener {
            confirmDialog.show()
        }

        updateViews()
    }

    private fun updateViews() {
        binding.apply {
            val userDetails = sharedStdViewModel.userDetails!!
            tvUserName.text = userDetails.name
            tvInstitute.text = userDetails.institute
            tvBranch.text = userDetails.branch
            tvSem.text = userDetails.semester.toString()
            tvSection.text = userDetails.section

            tvEnrollment.text = userDetails.enrollment
            tvEmail.text = userDetails.email
        }
    }

    private fun createConfirmDialog() {
        confirmDialog = AlertDialog.Builder(context)
            .setTitle("Confirm logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .create()
    }

    private fun logout() {

        sharedStdViewModel.logout()
        val intent = Intent(activity, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        activity?.finish()
    }

}