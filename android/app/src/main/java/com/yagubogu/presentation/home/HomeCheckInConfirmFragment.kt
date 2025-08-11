package com.yagubogu.presentation.home

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.yagubogu.databinding.FragmentHomeCheckInConfirmBinding

class HomeCheckInConfirmFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: FragmentHomeCheckInConfirmBinding =
            FragmentHomeCheckInConfirmBinding.inflate(layoutInflater)

        binding.tvPositiveBtn.setOnClickListener { setResultAndDismiss(true) }
        binding.tvNegativeBtn.setOnClickListener { setResultAndDismiss(false) }

        return AlertDialog
            .Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    private fun setResultAndDismiss(confirmResult: Boolean) {
        val result = Bundle().apply { putBoolean(KEY_CONFIRM, confirmResult) }
        setFragmentResult(KEY_REQUEST_SUCCESS, result)
        dismiss()
    }

    companion object {
        const val KEY_REQUEST_SUCCESS = "success"
        const val KEY_CONFIRM = "confirm"

        fun newInstance(): HomeCheckInConfirmFragment = HomeCheckInConfirmFragment()
    }
}
