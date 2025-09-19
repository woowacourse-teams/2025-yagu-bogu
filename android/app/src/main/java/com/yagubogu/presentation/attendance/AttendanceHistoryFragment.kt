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
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.FragmentAttendanceHistoryBinding
import com.yagubogu.presentation.attendance.model.AttendanceHistoryFilter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.attendance.model.AttendanceHistoryOrder

@Suppress("ktlint:standard:backing-property-naming")
class AttendanceHistoryFragment : Fragment() {
    private var _binding: FragmentAttendanceHistoryBinding? = null
    private val binding: FragmentAttendanceHistoryBinding get() = _binding!!

    private val viewModel: AttendanceHistoryViewModel by viewModels {
        val app = requireActivity().application as YaguBoguApplication
        AttendanceHistoryViewModelFactory(app.checkInsRepository)
    }

    private val attendanceHistoryAdapter by lazy {
        AttendanceHistoryAdapter(
            attendanceHistorySummaryHandler = viewModel,
            attendanceHistoryDetailHandler = viewModel,
        )
    }

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
            updateEmptyHistoryState(value.isEmpty())
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

    private fun updateEmptyHistoryState(isEmpty: Boolean) {
        binding.ivEmptyHistory.isVisible = isEmpty
        binding.tvEmptyHistory.isVisible = isEmpty

        binding.constraintFilter.isVisible = !isEmpty
        binding.constraintOrder.isVisible = !isEmpty
    }
}
