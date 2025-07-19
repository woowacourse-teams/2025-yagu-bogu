package com.yagubogu.presentation.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
import com.yagubogu.R
import com.yagubogu.databinding.FragmentStatsBinding
import com.yagubogu.databinding.TabStatsBinding
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
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupTabLayoutListener()
        replaceFragment(MyStatsFragment::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupTabs() {
        val tabTitles = listOf(R.string.tab_stats_my_stats, R.string.tab_stats_stadium_stats)

        val tabLayout = binding.tabStats
        tabLayout.removeAllTabs()

        tabTitles.forEach { titleResId: Int ->
            val tab = tabLayout.newTab()
            val tabView = TabStatsBinding.inflate(layoutInflater)
            val textView = tabView.tabText
            textView.setText(titleResId)
            tab.customView = textView
            tabLayout.addTab(tab)
        }

        repeat(tabLayout.tabCount) { index: Int ->
            val tab = tabLayout.getTabAt(index)
            val isSelected = index == 0
            updateTabText(tab, selected = isSelected)
        }
    }

    private fun setupTabLayoutListener() {
        binding.tabStats.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    updateTabText(tab, true)
                    when (tab?.position) {
                        0 -> replaceFragment(MyStatsFragment::class.java)
                        1 -> replaceFragment(StadiumListFragment::class.java)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    updateTabText(tab, false)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            },
        )
    }

    private fun replaceFragment(fragment: Class<out Fragment>) {
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace(binding.fcvStatsFragment.id, fragment, null)
        }
    }

    private fun updateTabText(
        tab: TabLayout.Tab?,
        selected: Boolean,
    ) {
        val spSize = if (selected) 18f else 16f
        val textColorRes = if (selected) R.color.white else R.color.primary700

        (tab?.customView as? TextView)?.apply {
            textSize = spSize
            setTextColor(ContextCompat.getColor(context, textColorRes))
        }
    }
}
