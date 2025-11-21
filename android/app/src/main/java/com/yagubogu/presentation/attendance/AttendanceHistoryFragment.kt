package com.yagubogu.presentation.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.databinding.FragmentAttendanceHistoryBinding
import com.yagubogu.presentation.attendance.model.AttendanceHistoryFilter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.attendance.model.AttendanceHistoryOrder
import com.yagubogu.presentation.util.ScrollToTop
import dagger.hilt.android.AndroidEntryPoint

@Suppress("ktlint:standard:backing-property-naming")
@AndroidEntryPoint
class AttendanceHistoryFragment :
    Fragment(),
    AttendanceHistorySummaryViewHolder.Handler,
    AttendanceHistoryDetailViewHolder.Handler,
    ScrollToTop {
    private var _binding: FragmentAttendanceHistoryBinding? = null
    private val binding: FragmentAttendanceHistoryBinding get() = _binding!!

    private val viewModel: AttendanceHistoryViewModel by viewModels()

    private val attendanceHistoryAdapter by lazy {
        AttendanceHistoryAdapter(
            attendanceHistorySummaryHandler = this,
            attendanceHistoryDetailHandler = this,
        )
    }

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAttendanceHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupBindings()
        setupSpinner()
        setupObservers()
        setupListeners()
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

    override fun onSummaryItemClick(item: AttendanceHistoryItem.Summary) {
        viewModel.onSummaryItemClick(item)
        firebaseAnalytics.logEvent("attendance_history_item_click", null)
    }

    override fun onDetailItemClick(item: AttendanceHistoryItem.Detail) {
        viewModel.onDetailItemClick(item)
        firebaseAnalytics.logEvent("attendance_history_item_click", null)
    }

    override fun scrollToTop() {
        binding.rvAttendanceHistory.smoothScrollToPosition(0)
    }

    private fun setupBindings() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.rvAttendanceHistory.apply {
            adapter = attendanceHistoryAdapter
            itemAnimator = null
        }
    }

    private fun setupSpinner() {
        val spinnerItems: Array<String> =
            resources.getStringArray(R.array.attendance_history_filter)
        val spinnerAdapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), R.layout.item_spinner_attendance_history, spinnerItems)
        binding.spinnerAttendanceHistoryFilter.apply {
            adapter = spinnerAdapter
            onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        val filter = AttendanceHistoryFilter.entries[position]
                        viewModel.updateAttendanceHistoryFilter(filter)
                        firebaseAnalytics.logEvent("attendance_history_change_filter", null)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }
        }
    }

    private fun setupObservers() {
        viewModel.attendanceHistoryItems.observe(viewLifecycleOwner) { value: List<AttendanceHistoryItem> ->
            attendanceHistoryAdapter.submitList(value) {
                viewModel.detailItemPosition.value?.let {
                    binding.rvAttendanceHistory.smoothScrollToPosition(it)
                }
            }

            val isEmpty: Boolean = value.isEmpty()
            binding.ivEmptyHistory.isVisible = isEmpty
            binding.tvEmptyHistory.isVisible = isEmpty
        }

        viewModel.attendanceHistoryOrder.observe(viewLifecycleOwner) { value: AttendanceHistoryOrder ->
            binding.tvAttendanceHistoryOrder.text =
                getString(
                    when (value) {
                        AttendanceHistoryOrder.LATEST -> R.string.attendance_history_latest
                        AttendanceHistoryOrder.OLDEST -> R.string.attendance_history_oldest
                    },
                )
        }
    }

    private fun setupListeners() {
        binding.tvAttendanceHistoryOrder.setOnClickListener {
            viewModel.switchAttendanceHistoryOrder()
            firebaseAnalytics.logEvent("attendance_history_change_order", null)
        }
    }
}
