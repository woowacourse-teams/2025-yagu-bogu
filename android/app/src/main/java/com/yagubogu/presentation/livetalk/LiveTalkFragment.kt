package com.yagubogu.presentation.livetalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yagubogu.databinding.FragmentLiveTalkBinding

@Suppress("ktlint:standard:backing-property-naming")
class LiveTalkFragment : Fragment() {
    private var _binding: FragmentLiveTalkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLiveTalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
