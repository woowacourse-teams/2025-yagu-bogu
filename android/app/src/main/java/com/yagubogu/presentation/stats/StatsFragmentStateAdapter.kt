package com.yagubogu.presentation.stats

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yagubogu.presentation.stats.my.MyStatsFragment
import com.yagubogu.presentation.stats.stadium.list.StadiumListFragment

class StatsFragmentStateAdapter(
    hostFragment: Fragment,
) : FragmentStateAdapter(hostFragment) {
    override fun getItemCount(): Int = StatsTab.entries.size

    override fun createFragment(position: Int): Fragment =
        when (StatsTab.entries[position]) {
            StatsTab.MY_STATS -> MyStatsFragment()
            StatsTab.STADIUM_STATS -> StadiumListFragment()
        }
}
