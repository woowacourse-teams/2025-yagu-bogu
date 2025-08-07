package com.yagubogu.presentation.stats.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.FragmentAttendanceHistoryBinding

@Suppress("ktlint:standard:backing-property-naming")
class AttendanceHistoryFragment : Fragment() {
    private var _binding: FragmentAttendanceHistoryBinding? = null
    private val binding: FragmentAttendanceHistoryBinding get() = _binding!!

    private val viewModel: AttendanceHistoryViewModel by viewModels {
        val app = requireActivity().application as YaguBoguApplication
        AttendanceHistoryViewModelFactory(app.checkInsRepository)
    }

    private val attendanceHistoryAdapter = AttendanceHistoryAdapter()

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBindings() {
        binding.rvAttendanceHistory.adapter = attendanceHistoryAdapter
    }

    private fun setupSpinner() {
        val spinnerItems: Array<String> =
            resources.getStringArray(R.array.stats_attendance_history_filter)
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
                        when (position) {
                            0 -> {
                                viewModel.clearAttendanceHistoryItems()
                                viewModel.fetchAttendanceHistoryItems(2025, RESULT_TYPE_ALL)
                            }

                            1 -> {
                                viewModel.clearAttendanceHistoryItems()
                                viewModel.fetchAttendanceHistoryItems(2025, RESULT_TYPE_WIN)
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }
        }
    }

    private fun setupObservers() {
        viewModel.attendanceHistoryItems.observe(viewLifecycleOwner) { attendanceHistoryItems: List<AttendanceHistoryItem> ->
            attendanceHistoryAdapter.submitList(attendanceHistoryItems)
            binding.tvEmptyHistory.visibility =
                if (attendanceHistoryItems.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    companion object {
        private const val RESULT_TYPE_ALL = "ALL"
        private const val RESULT_TYPE_WIN = "WIN"
    }
}
