package com.yagubogu.presentation.stats.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yagubogu.databinding.FragmentMyStatsBinding

@Suppress("ktlint:standard:backing-property-naming")
class MyStatsFragment : Fragment() {
    private var _binding: FragmentMyStatsBinding? = null
    private val binding: FragmentMyStatsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
