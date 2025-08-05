package com.yagubogu.presentation.stats.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yagubogu.databinding.FragmentAttendanceHistoryBinding

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
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
