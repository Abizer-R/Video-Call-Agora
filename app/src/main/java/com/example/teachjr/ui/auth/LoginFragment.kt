package com.example.teachjr.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.databinding.FragmentLoginBinding
import com.example.teachjr.ui.main.MainActivity
import com.example.teachjr.utils.AuthUtils
import com.example.teachjr.utils.sealedClasses.Response
import com.example.teachjr.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val TAG = LoginFragment::class.java.simpleName + "_TESTING"
    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnSignIn.setOnClickListener {
            loginUser()
        }

        binding.layoutSignUp.setOnClickListener {
            findNavController().navigateUp()
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
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
                null -> {}
            }
        }

    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        val userRequestValidation = AuthUtils.validateCredentials(
            username = "",
            email = email,
            password = password,
            isLogin = true
        )

        if(userRequestValidation.first) {
            authViewModel.signInUser(email, password)
        } else {
            binding.tvError.text = userRequestValidation.second
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}