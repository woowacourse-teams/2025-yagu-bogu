package com.yagubogu.presentation.stats.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yagubogu.R
import com.yagubogu.databinding.FragmentAttendanceHistoryBinding
import com.yagubogu.domain.model.Team
import java.time.LocalDate

@Suppress("ktlint:standard:backing-property-naming")
class AttendanceHistoryFragment : Fragment() {
    private var _binding: FragmentAttendanceHistoryBinding? = null
    private val binding: FragmentAttendanceHistoryBinding get() = _binding!!

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBindings() {
        val attendanceHistoryAdapter = AttendanceHistoryAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.rvAttendanceHistory.apply {
            adapter = attendanceHistoryAdapter
            layoutManager = linearLayoutManager
        }
        attendanceHistoryAdapter.submitList(DUMMY_ATTENDANCE_HISTORY_ITEMS)
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
                            0 -> {}
                            1 -> {}
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }
        }
    }

    companion object {
        private val DUMMY_ATTENDANCE_HISTORY_ITEMS: List<AttendanceHistoryItem> =
            listOf(
                AttendanceHistoryItem(
                    awayTeam =
                        TeamItem(
                            team = Team.HT,
                            name = "KIA",
                            score = 9,
                            isMyTeam = true,
                        ),
                    homeTeam =
                        TeamItem(
                            team = Team.OB,
                            name = "두산",
                            score = 5,
                            isMyTeam = false,
                        ),
                    attendanceDate = LocalDate.of(2025, 8, 1),
                    stadiumName = "잠실 야구장",
                ),
                AttendanceHistoryItem(
                    awayTeam =
                        TeamItem(
                            team = Team.WO,
                            name = "키움",
                            score = 7,
                            isMyTeam = false,
                        ),
                    homeTeam =
                        TeamItem(
                            team = Team.HT,
                            name = "KIA",
                            score = 4,
                            isMyTeam = true,
                        ),
                    attendanceDate = LocalDate.of(2025, 7, 31),
                    stadiumName = "광주 KIA 챔피언스필드",
                ),
                AttendanceHistoryItem(
                    awayTeam =
                        TeamItem(
                            team = Team.HT,
                            name = "KIA",
                            score = 20,
                            isMyTeam = true,
                        ),
                    homeTeam =
                        TeamItem(
                            team = Team.LG,
                            name = "LG",
                            score = 8,
                            isMyTeam = false,
                        ),
                    attendanceDate = LocalDate.of(2025, 7, 22),
                    stadiumName = "잠실 야구장",
                ),
            )
    }
}
