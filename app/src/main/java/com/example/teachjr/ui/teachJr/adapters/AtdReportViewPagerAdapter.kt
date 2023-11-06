package com.example.teachjr.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.teachjr.ui.professor.profFragments.AtdReportLecturesFragment
import com.example.teachjr.ui.professor.profFragments.AtdReportStudentsFragment

class AtdReportViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        when(position) {
            0 -> return AtdReportLecturesFragment()
            else -> return AtdReportStudentsFragment()
        }
    }
}