package com.yagubogu.presentation.stats.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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

    companion object {
        private val DUMMY_ATTENDANCE_HISTORY_ITEMS: List<AttendanceHistoryItem> =
            listOf(
                AttendanceHistoryItem(
                    awayTeam =
                        TeamItem(
                            team = Team.KIA,
                            name = "KIA",
                            score = 9,
                        ),
                    homeTeam =
                        TeamItem(
                            team = Team.DOOSAN,
                            name = "두산",
                            score = 5,
                        ),
                    date = LocalDate.of(2025, 8, 1),
                    stadiumName = "잠실 야구장",
                ),
                AttendanceHistoryItem(
                    awayTeam =
                        TeamItem(
                            team = Team.KIWOOM,
                            name = "키움",
                            score = 7,
                        ),
                    homeTeam =
                        TeamItem(
                            team = Team.KIA,
                            name = "KIA",
                            score = 4,
                        ),
                    date = LocalDate.of(2025, 7, 31),
                    stadiumName = "광주 KIA 챔피언스필드",
                ),
                AttendanceHistoryItem(
                    awayTeam =
                        TeamItem(
                            team = Team.KIA,
                            name = "KIA",
                            score = 20,
                        ),
                    homeTeam =
                        TeamItem(
                            team = Team.LG,
                            name = "LG",
                            score = 8,
                        ),
                    date = LocalDate.of(2025, 7, 22),
                    stadiumName = "잠실 야구장",
                ),
            )
    }
}
