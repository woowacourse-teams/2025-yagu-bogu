package com.yagubogu.presentation.stats.stadium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yagubogu.databinding.FragmentStadiumListBinding

@Suppress("ktlint:standard:backing-property-naming")
class StadiumListFragment : Fragment() {
    private var _binding: FragmentStadiumListBinding? = null
    private val binding: FragmentStadiumListBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStadiumListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
