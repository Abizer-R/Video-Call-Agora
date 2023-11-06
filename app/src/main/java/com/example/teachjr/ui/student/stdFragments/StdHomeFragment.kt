package com.example.teachjr.ui.student.stdFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdHomeBinding
import com.example.teachjr.ui.adapters.StdCourseListAdapter
import com.example.teachjr.ui.viewmodels.studentViewModels.SharedStdViewModel
import com.example.teachjr.ui.viewmodels.studentViewModels.StdHomeViewModel
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StdHomeFragment : Fragment() {

    private val TAG = StdHomeFragment::class.java.simpleName
    private lateinit var binding: FragmentStdHomeBinding

    private val stdHomeViewModel by viewModels<StdHomeViewModel>()
    private val sharedStdViewModel by activityViewModels<SharedStdViewModel>()

    private val profCourseListAdapter = StdCourseListAdapter(
        onItemClicked = { rvCourseListItem ->

            sharedStdViewModel.updateCourseDetails(
                rvCourseListItem.courseCode,
                rvCourseListItem.courseName,
                rvCourseListItem.profName
            )
            findNavController().navigate(R.id.action_stdHomeFragment_to_stdCourseDetailsFragment)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStdHomeBinding.inflate(layoutInflater)
        Log.i(TAG, "StudentTesting_HomePage: Student HomePage Created")
//        setHasOptionsMenu(true)
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
        sharedStdViewModel.clearCourseValues()

        /**
         * Fetch the user details and courseList only if we have just launched our app
         */
        if(sharedStdViewModel.userDetails != null && sharedStdViewModel.courseList != null) {
            updateViews()
        } else {
            stdHomeViewModel.getUser()
            setObservers()
        }

        binding.layoutUserGreeting.setOnClickListener {
            findNavController().navigate(R.id.action_stdHomeFragment_to_stdProfileFragment)
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
        stdHomeViewModel.currUserStd.observe(viewLifecycleOwner) {
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
                    Log.i(TAG, "StudentTesting_HomePage: StudentUser = ${it.data}")

                    if(it.data != null) {
                        sharedStdViewModel.setUserDetails(it.data)

                        // Fetch the courseList only if we have user's details
                        stdHomeViewModel.getCourseList(it.data.institute!!, it.data.branch!!, it.data.sem_sec!!)
                    } else {
                        Toast.makeText(context, "User Details are null. Try restarting the app.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        stdHomeViewModel.courseList.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {
//                    binding.progressBar.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    // TODO: Show Error Layout with "try again" button
                    Log.i(TAG, "StudentTesting_HomePage: CourseList_Error - ${it.errorMessage}")
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    Log.i(TAG, "StudentTesting_HomePage: CourseList = ${it.data}")
//                    binding.progressBar.visibility = View.GONE
                    if(it.data != null) {
                        sharedStdViewModel.setCourseList(it.data)
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

        binding.tvUsername.text = sharedStdViewModel.userDetails!!.name
        profCourseListAdapter.updateList(sharedStdViewModel.courseList!!)
    }

}