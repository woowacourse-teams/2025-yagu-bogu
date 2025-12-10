package com.yagubogu.presentation.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.databinding.FragmentAttendanceHistoryBinding
import com.yagubogu.presentation.util.ScrollToTop
import com.yagubogu.ui.attendance.AttendanceHistoryScreen
import dagger.hilt.android.AndroidEntryPoint

@Suppress("ktlint:standard:backing-property-naming")
@AndroidEntryPoint
class AttendanceHistoryFragment :
    Fragment(),
    ScrollToTop {
    private var _binding: FragmentAttendanceHistoryBinding? = null
    private val binding: FragmentAttendanceHistoryBinding get() = _binding!!

    private val viewModel: AttendanceHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AttendanceHistoryScreen(viewModel)
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
//        setupBindings()
//        setupSpinner()
//        setupObservers()
//        setupListeners()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.fetchAttendanceHistoryItems()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun scrollToTop() {
        binding.rvAttendanceHistory.smoothScrollToPosition(0)
    }

//    private fun setupObservers() {
//        viewModel.attendanceHistoryItems.observe(viewLifecycleOwner) { value: List<AttendanceHistoryItem> ->
//            attendanceHistoryAdapter.submitList(value) {
//                viewModel.detailItemPosition.value?.let {
//                    binding.rvAttendanceHistory.smoothScrollToPosition(it)
//                }
//            }
//
//            val isEmpty: Boolean = value.isEmpty()
//            binding.ivEmptyHistory.isVisible = isEmpty
//            binding.tvEmptyHistory.isVisible = isEmpty
//        }
//
//        viewModel.attendanceHistoryOrder.observe(viewLifecycleOwner) { value: AttendanceHistoryOrder ->
//            binding.tvAttendanceHistoryOrder.text =
//                getString(
//                    when (value) {
//                        AttendanceHistoryOrder.LATEST -> R.string.attendance_history_latest
//                        AttendanceHistoryOrder.OLDEST -> R.string.attendance_history_oldest
//                    },
//                )
//        }
//    }
}
