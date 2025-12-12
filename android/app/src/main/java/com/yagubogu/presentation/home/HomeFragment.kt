package com.yagubogu.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.presentation.util.ScrollToTop
import com.yagubogu.ui.home.HomeScreen
import dagger.hilt.android.AndroidEntryPoint

@Suppress("ktlint:standard:backing-property-naming")
@AndroidEntryPoint
class HomeFragment :
    Fragment(),
    ScrollToTop {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HomeScreen(viewModel)
            }
        }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.fetchAll()
        }
    }

//    override fun onStart() {
//        super.onStart()
//        viewModel.startStreaming()
//    }

//    override fun onStop() {
//        super.onStop()
//        viewModel.stopStreaming()
//    }

    override fun scrollToTop() {
        viewModel.scrollToTop()
    }

    private fun setupObservers() {
        // TODO: MainActivity 마이그레이션 시 로딩 처리
//        viewModel.isCheckInLoading.observe(viewLifecycleOwner) { value: Boolean ->
//            (requireActivity() as MainActivity).setLoadingScreen(value)
//        }
    }
}
