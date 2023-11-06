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
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentRegisterBinding
import com.example.teachjr.ui.main.MainActivity
import com.example.teachjr.ui.viewmodels.AuthViewModel
import com.example.teachjr.ui.student.StudentActivity
import com.example.teachjr.utils.AuthUtils
import com.example.teachjr.utils.sealedClasses.Response
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }

        binding.layoutSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_register_fragment_to_login_fragment)
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

    private fun registerUser() {
        val username = binding.etUsername.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        val userRequestValidation = AuthUtils.validateCredentials(
            username = username,
            email = email,
            password = password,
            isLogin = false
        )

        if(userRequestValidation.first) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    authViewModel.createUser(username, email)
                }
        } else {
            binding.tvError.text = userRequestValidation.second
        }

    }



}