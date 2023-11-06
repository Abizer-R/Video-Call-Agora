package com.example.teachjr.ui.professor.profFragments

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
import com.example.teachjr.databinding.FragmentProfProfileBinding
import com.example.teachjr.databinding.FragmentStdProfileBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.student.stdFragments.StdProfileFragment
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.example.teachjr.ui.viewmodels.studentViewModels.SharedStdViewModel

class ProfProfileFragment : Fragment() {

    private val TAG = ProfProfileFragment::class.java.simpleName
    private lateinit var binding: FragmentProfProfileBinding

    private val sharedProfViewModel by activityViewModels<SharedProfViewModel>()

    private lateinit var confirmDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfProfileBinding.inflate(layoutInflater)
        Log.i(TAG, "ProfessorTesting_HomePage: Professor Profile Page Created")
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
            val userDetails = sharedProfViewModel.userDetails!!
            tvUserName.text = userDetails.name
            tvInstitute.text = userDetails.institute
            tvBranch.text = userDetails.branch

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

        sharedProfViewModel.logout()
        val intent = Intent(activity, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        activity?.finish()
    }

}