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
import com.example.teachjr.databinding.FragmentLoginProfBinding
import com.example.teachjr.ui.professor.ProfessorActivity
import com.example.teachjr.ui.viewmodels.AuthViewModel
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginProfFragment : Fragment() {

    private lateinit var binding: FragmentLoginProfBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginProfBinding.inflate(inflater, container, false);
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
            loginProfessor()
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
                    val intent = Intent(activity, ProfessorActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
                null -> {}
            }
        }
    }

    private fun loginProfessor() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if(email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Please enter all credentials", Toast.LENGTH_SHORT).show()
            return
        }

        authViewModel.loginProfessor(email, password)
    }
}