package com.yagubogu.presentation.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yagubogu.MainActivity
import com.yagubogu.R
import com.yagubogu.databinding.FragmentChallengeBinding

@Suppress("ktlint:standard:backing-property-naming")
class ChallengeFragment : Fragment() {
    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setToolbarTitle(getString(R.string.bottom_navigation_challenge))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
