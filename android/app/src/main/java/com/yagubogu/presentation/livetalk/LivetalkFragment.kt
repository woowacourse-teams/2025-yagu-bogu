package com.yagubogu.presentation.livetalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.FragmentLiveTalkBinding
import kotlin.getValue

@Suppress("ktlint:standard:backing-property-naming")
class LivetalkFragment : Fragment() {
    private var _binding: FragmentLiveTalkBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LivetalkViewModel by viewModels {
        val app = requireActivity().application as YaguBoguApplication
        LivetalkViewModelFactory(
            app.stadiumRepository,
        )
    }

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
