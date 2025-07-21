package com.yagubogu.presentation.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
import com.yagubogu.R
import com.yagubogu.databinding.FragmentStatsBinding
import com.yagubogu.databinding.ViewTabStatsBinding
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
    }

    override fun onStart() {
        super.onStart()
        replaceFragment(MyStatsFragment::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupTabs() {
        binding.tabStats.removeAllTabs()

        StatsTab.entries.forEachIndexed { index: Int, statsTab: StatsTab ->
            val tab = createTab(statsTab.titleResId)
            binding.tabStats.addTab(tab)

            val isSelected = index == StatsTab.MY_STATS.ordinal
            updateTabText(tab, isSelected)
        }
    }

    private fun createTab(titleResId: Int): TabLayout.Tab {
        val tab = binding.tabStats.newTab()
        val viewTabStatsBinding = ViewTabStatsBinding.inflate(layoutInflater)
        val textView = viewTabStatsBinding.tvTabText
        textView.setText(titleResId)
        tab.customView = textView
        return tab
    }

    private fun setupTabLayoutListener() {
        binding.tabStats.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    updateTabText(tab, true)
                    when (StatsTab.entries[tab?.position ?: 0]) {
                        StatsTab.MY_STATS -> replaceFragment(MyStatsFragment::class.java)
                        StatsTab.STADIUM_STATS -> replaceFragment(StadiumListFragment::class.java)
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
        val spSize = if (selected) SELECTED_TAB_TEXT_SIZE else UNSELECTED_TAB_TEXT_SIZE
        val textColorResId = if (selected) R.color.white else R.color.primary700

        (tab?.customView as? TextView)?.apply {
            textSize = spSize
            setTextColor(context.getColor(textColorResId))
        }
    }

    companion object {
        private const val SELECTED_TAB_TEXT_SIZE = 18f
        private const val UNSELECTED_TAB_TEXT_SIZE = 16f
    }
}
