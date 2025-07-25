package com.yagubogu.presentation.stats

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatsFragmentStateAdapter(
    hostFragment: Fragment,
    private val tabFragments: List<Fragment>,
) : FragmentStateAdapter(hostFragment) {
    override fun getItemCount(): Int = tabFragments.size

    override fun createFragment(position: Int): Fragment = tabFragments[position]
}
