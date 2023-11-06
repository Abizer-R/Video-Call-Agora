package com.example.teachjr.ui.auth.authFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentLoginStdBinding
import com.example.teachjr.ui.viewmodels.AuthViewModel
import com.example.teachjr.ui.student.StudentActivity
import com.example.teachjr.utils.sealedClasses.Response

class LoginStdFragment : Fragment() {

    private lateinit var binding: FragmentLoginStdBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginStdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {

            // Adding up navigation
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_black_32)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        binding.btnLogin.setOnClickListener {
            loginStudent()
        }

        authViewModel.loginStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "USER VERIFIED", Toast.LENGTH_SHORT).show()
                    val intent = Intent(activity, StudentActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
                null -> {}
            }
        }

    }

    private fun loginStudent() {
        val email = binding.etEmail.text.toString()
        val enrollment = binding.etEnrollment.text.toString()
        val password = binding.etPassword.text.toString()

        if(email.isBlank() || enrollment.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Please enter all credentials", Toast.LENGTH_SHORT).show()
            return
        }

        authViewModel.loginStudent(email, enrollment, password)
    }

}