package com.yagubogu.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.R
import com.yagubogu.presentation.MainActivity
import com.yagubogu.presentation.home.model.CheckInUiEvent
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
//        binding.nsvRoot.smoothScrollTo(0, 0)
    }

    private fun setupObservers() {
        viewModel.checkInUiEvent.observe(viewLifecycleOwner) { value: CheckInUiEvent ->
            val message: String =
                when (value) {
                    is CheckInUiEvent.Success ->
                        getString(R.string.home_check_in_success_message, value.stadium.name)

                    CheckInUiEvent.NoGame -> getString(R.string.home_check_in_no_game_message)
                    CheckInUiEvent.OutOfRange -> getString(R.string.home_check_in_out_of_range_message)
                    CheckInUiEvent.AlreadyCheckedIn -> getString(R.string.home_already_checked_in_message)
                    CheckInUiEvent.LocationFetchFailed -> getString(R.string.home_check_in_location_fetch_failed_message)
                    CheckInUiEvent.NetworkFailed -> getString(R.string.home_check_in_network_failed_message)
                }
//            binding.root.showSnackbar(message, R.id.bnv_navigation)
        }

        viewModel.isCheckInLoading.observe(viewLifecycleOwner) { value: Boolean ->
            (requireActivity() as MainActivity).setLoadingScreen(value)
        }
    }
}
