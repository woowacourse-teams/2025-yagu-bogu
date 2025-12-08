package com.yagubogu.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.presentation.util.ScrollToTop
import com.yagubogu.ui.stats.detail.StatsDetailViewModel
import com.yagubogu.ui.stats.my.StatsMyViewModel
import dagger.hilt.android.AndroidEntryPoint

@Suppress("ktlint:standard:backing-property-naming")
@AndroidEntryPoint
class StatsFragment :
    Fragment(),
    ScrollToTop {
    private val statsMyViewModel: StatsMyViewModel by viewModels()
    private val statsDetailViewModel: StatsDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
//                StatsScreen(
//                    statsMyViewModel = statsMyViewModel,
//                    statsDetailViewModel = statsDetailViewModel,
//                )
            }
        }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            statsMyViewModel.fetchAll()
            statsDetailViewModel.fetchAll()
        }
    }

    override fun scrollToTop() {
        statsMyViewModel.scrollToTop()
        statsDetailViewModel.scrollToTop()
    }
}
