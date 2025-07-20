package com.yagubogu.presentation.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yagubogu.MainActivity
import com.yagubogu.R
import com.yagubogu.databinding.FragmentStatsBinding

@Suppress("ktlint:standard:backing-property-naming")
class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setToolbarTitle(getString(R.string.bottom_navigation_stats))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
