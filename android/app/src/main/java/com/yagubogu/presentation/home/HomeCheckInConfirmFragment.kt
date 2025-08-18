package com.yagubogu.presentation.home

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
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
        private const val KEY_REQUEST_SUCCESS = "success"
        private const val KEY_CONFIRM = "confirm"

        fun newInstance(): HomeCheckInConfirmFragment = HomeCheckInConfirmFragment()

        fun setResultListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onResult: (Boolean) -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(
                KEY_REQUEST_SUCCESS,
                lifecycleOwner,
            ) { _, bundle ->
                onResult(bundle.getBoolean(KEY_CONFIRM))
            }
        }
    }
}
