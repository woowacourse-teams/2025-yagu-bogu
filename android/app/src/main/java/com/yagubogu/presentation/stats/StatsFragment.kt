package com.yagubogu.presentation.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.yagubogu.R
import com.yagubogu.databinding.FragmentStatsBinding
import com.yagubogu.databinding.ViewTabStatsBinding
import com.yagubogu.presentation.stats.detail.StatsDetailFragment
import com.yagubogu.presentation.stats.detail.StatsDetailViewModel
import com.yagubogu.presentation.stats.my.StatsMyFragment
import com.yagubogu.presentation.stats.my.StatsMyViewModel
import com.yagubogu.presentation.util.ScrollToTop
import com.yagubogu.ui.stats.StatsScreen
import dagger.hilt.android.AndroidEntryPoint

@Suppress("ktlint:standard:backing-property-naming")
@AndroidEntryPoint
class StatsFragment :
    Fragment(),
    ScrollToTop {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val statsMyViewModel: StatsMyViewModel by viewModels()
    private val statsDetailViewModel: StatsDetailViewModel by viewModels()

    private val statsStateAdapter: StatsFragmentStateAdapter by lazy {
        StatsFragmentStateAdapter(
            this,
            listOf(StatsMyFragment(), StatsDetailFragment()),
        )
    }

    // TODO Compose 마이그레이션 후 필요 없는 코드 삭제하기

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//    ): View {
//        _binding = FragmentStatsBinding.inflate(inflater, container, false)
//        return binding.root
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                StatsScreen(
                    statsMyViewModel = statsMyViewModel,
                    statsDetailViewModel = statsDetailViewModel,
                )
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
//        binding.vpStatsFragment.adapter = statsStateAdapter
//        setupTabLayoutMediator()
//        setupTabLayoutListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun scrollToTop() {
        val position: Int = binding.vpStatsFragment.currentItem
        val currentFragment: Fragment = statsStateAdapter.createFragment(position)
        if (currentFragment is ScrollToTop) {
            currentFragment.scrollToTop()
        }
    }

    private fun setupTabLayoutMediator() {
        TabLayoutMediator(
            binding.tabStats,
            binding.vpStatsFragment,
        ) { tab: TabLayout.Tab, position: Int ->
            val titleResId = StatsTab.entries[position].titleResId
            val customTab = createTab(titleResId)
            val isSelected = position == StatsTab.MY_STATS.ordinal
            updateTabText(customTab, isSelected)
            tab.customView = customTab.customView
        }.attach()
    }

    private fun setupTabLayoutListener() {
        binding.tabStats.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    updateTabText(tab, true)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    updateTabText(tab, false)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            },
        )
    }

    private fun createTab(titleResId: Int): TabLayout.Tab {
        val tab = binding.tabStats.newTab()
        val viewTabStatsBinding = ViewTabStatsBinding.inflate(layoutInflater)
        val textView = viewTabStatsBinding.tvTabText
        textView.setText(titleResId)
        tab.customView = textView
        return tab
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
