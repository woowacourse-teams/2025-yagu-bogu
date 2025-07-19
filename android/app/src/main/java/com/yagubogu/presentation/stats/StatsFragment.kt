package com.yagubogu.presentation.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
import com.yagubogu.databinding.FragmentStatsBinding
import com.yagubogu.presentation.stats.my.MyStatsFragment
import com.yagubogu.presentation.stats.stadium.StadiumListFragment

@Suppress("ktlint:standard:backing-property-naming")
class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        setupTabLayoutListener()
        replaceFragment(MyStatsFragment::class.java)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupTabLayoutListener() {
        binding.tabStats.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        0 -> replaceFragment(MyStatsFragment::class.java)
                        1 -> replaceFragment(StadiumListFragment::class.java)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            },
        )
    }

    private fun replaceFragment(fragment: Class<out Fragment>): Boolean {
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace(binding.fcvStatsFragment.id, fragment, null)
        }
        return true
    }
}
