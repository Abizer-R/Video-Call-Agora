package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfHomeBinding
import com.example.teachjr.ui.adapters.ProfCourseListAdapter
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfHomeViewModel
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfHomeFragment : Fragment() {

    private val TAG = ProfHomeFragment::class.java.simpleName
    private lateinit var binding: FragmentProfHomeBinding

    private val homeViewModel by viewModels<ProfHomeViewModel>()
    private val sharedProfViewModel by activityViewModels<SharedProfViewModel>()

    private val profCourseListAdapter = ProfCourseListAdapter(
        onItemClicked = { rvCourseItem ->

            // Update the course values
            sharedProfViewModel.updateCourseDetails(
                rvCourseItem.courseCode,
                rvCourseItem.courseName,
                rvCourseItem.sem_sec
            )
            findNavController().navigate(R.id.action_profHomeFragment_to_profCourseDetailsFragment)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfHomeBinding.inflate(layoutInflater)
        Log.i(TAG, "ProfessorTesting_HomePage: Professor HomePage Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOptionsMenu()

        binding.rvCourseList.apply {
            hasFixedSize()
            adapter = profCourseListAdapter
            layoutManager = LinearLayoutManager(context)
        }

        /**
         * Making sure if when we navigate up from a courseDetail page,
         * the previous values are cleared
         */
        sharedProfViewModel.clearValues()

        /**
         * Fetch the user details and courseList only if we have just launched our app
         */
        if(sharedProfViewModel.userDetails != null && sharedProfViewModel.courseList != null) {
            updateViews()
        } else {
            homeViewModel.getUser()
            setObservers()
        }

        binding.layoutUserGreeting.setOnClickListener {
            findNavController().navigate(R.id.action_profHomeFragment_to_profProfileFragment)
        }
    }

    private fun setupOptionsMenu() {
        /**
         * Currently I don't have anything to put in settings
         */
//        binding.toolbar.inflateMenu(R.menu.homepage_menu)
//        binding.toolbar.setOnMenuItemClickListener {
//            when(it.itemId) {
//                R.id.action_settings -> {
//                    // TODO: Implement settings (HELP + ABOUT)
//                    Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show()
//                    true
//                }
//                else -> false
//            }
//        }
    }

    private fun setObservers() {
        homeViewModel.currUserProf.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {
                    showLoading()
                }
                is Response.Error -> {
                    // TODO: Show Error Layout with "try again" button
                    Log.i(TAG, "StudentTesting_HomePage: CurrUser_Error - ${it.errorMessage}")
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    Log.i(TAG, "ProfessorTesting_HomePage: ProfUser = ${it.data}")

                    if(it.data != null) {
                        sharedProfViewModel.setUserDetails(it.data)

                        /**
                         * Fetch the courseList only if we have user's details
                         */
                        homeViewModel.getCourseList()
                    } else {
                        Toast.makeText(context, "User Details are null. Try restarting the app.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        homeViewModel.courseList.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {
//                    binding.progressBar.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    // TODO: Show Error Layout with "try again" button
                    Log.i(TAG, "ProfessorTesting_HomePage: CourseList_Error - ${it.errorMessage}")
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    Log.i(TAG, "ProfessorTesting_HomePage: CourseList - ${it.data}")

                    if(it.data != null) {
                        sharedProfViewModel.setCourseList(it.data)
                        updateViews()
                    } else {
                        Toast.makeText(context, "Course List is null. Refresh", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.tvWelcome.visibility = View.GONE
        binding.rvCourseList.visibility = View.GONE

        binding.tvUsername.text = "Loading\nPlease Wait"

        binding.loadingAnimation.visibility = View.VISIBLE
        binding.loadingAnimation.playAnimation()
    }

    private fun updateViews() {
        binding.tvWelcome.visibility = View.VISIBLE
        binding.rvCourseList.visibility = View.VISIBLE

        binding.loadingAnimation.visibility = View.GONE
        binding.loadingAnimation.cancelAnimation()

        binding.tvUsername.text = sharedProfViewModel.userDetails!!.name
        profCourseListAdapter.updateList(sharedProfViewModel.courseList!!)
    }
}