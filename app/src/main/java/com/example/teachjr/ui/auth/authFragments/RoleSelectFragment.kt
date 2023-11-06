package com.example.teachjr.ui.auth.authFragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentRoleSelectBinding
import com.example.teachjr.ui.viewmodels.AuthViewModel
import com.example.teachjr.utils.sealedClasses.UserType
import com.example.teachjr.utils.FirebaseConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoleSelectFragment : Fragment() {

    private lateinit var binding: FragmentRoleSelectBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoleSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOnclickListeners()
        setupObservers()
    }

    private fun setupOnclickListeners() {
        binding.layoutStd.setOnClickListener {
            authViewModel.setUserType(FirebaseConstants.TYPE_STUDENT)
        }

        binding.layoutProf.setOnClickListener {
            authViewModel.setUserType(FirebaseConstants.TYPE_PROFESSOR)
        }

        binding.btnLogin.setOnClickListener {
            gotoLoginScreen()
        }
    }

    private fun setupObservers() {
        authViewModel.userType.observe(viewLifecycleOwner) {
            when(it) {
                is UserType.Student -> {
                    selectStudent()
                    unSelectProfessor()
                }
                is UserType.Teacher -> {
                    selectProfessor()
                    unSelectStudent()
                }
                null -> {
                    unSelectStudent()
                    unSelectProfessor()
                }
            }
        }
    }


    private fun gotoLoginScreen() {
        when(authViewModel.userType.value) {
            is UserType.Student -> {
                findNavController().navigate(R.id.action_register_fragment_to_login_fragment)
            }
            is UserType.Teacher -> {
                findNavController().navigate(R.id.action_roleSelectFragment_to_loginProfFragment)
            }
            null -> Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectStudent() {
        binding.imgStd.alpha = 1F
        binding.tvStdLabel.apply {
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#000000"))
        }
    }

    private fun selectProfessor() {
        binding.imgProf.alpha = 1F
        binding.tvProfLabel.apply {
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#000000"))
        }
    }

    private fun unSelectStudent() {
        binding.imgStd.alpha = 0.6F
        binding.tvStdLabel.apply {
            typeface = Typeface.DEFAULT
            setTextColor(Color.parseColor("#51A2A8AD"))
        }
    }

    private fun unSelectProfessor() {
        binding.imgProf.alpha = 0.6F
        binding.tvProfLabel.apply {
            typeface = Typeface.DEFAULT
            setTextColor(Color.parseColor("#51A2A8AD"))
        }
    }
}