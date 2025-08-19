package com.yagubogu.presentation.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.yagubogu.databinding.FragmentDefaultConfirmBinding
import com.yagubogu.presentation.util.getParcelableCompat

class DefaultDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogDefaultUiModel: DialogDefaultUiModel =
            arguments?.getParcelableCompat(KEY_DEFAULT_DIALOG_UI_MODEL)
                ?: return super.onCreateDialog(savedInstanceState)

        val binding: FragmentDefaultConfirmBinding =
            FragmentDefaultConfirmBinding.inflate(layoutInflater)

        binding.dialogDefaultUiModel = dialogDefaultUiModel
        binding.tvPositiveBtn.setOnClickListener { setResultAndDismiss(true) }
        binding.tvNegativeBtn.setOnClickListener { setResultAndDismiss(false) }

        return AlertDialog
            .Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    private fun setResultAndDismiss(isConfirmed: Boolean) {
        val bundle = Bundle().apply { putBoolean(KEY_CONFIRM, isConfirmed) }
        setFragmentResult(KEY_REQUEST_SUCCESS, bundle)
        dismiss()
    }

    companion object {
        private const val KEY_DEFAULT_DIALOG_UI_MODEL = "defaultDialogUiModel"
        private const val BUNDLE_RESULT_YN = "resultBoolean"
        private const val REQUEST_KEY = "defaultDialogRequest"
        const val KEY_REQUEST_SUCCESS = "success"
        const val KEY_CONFIRM = "confirm"

        fun newInstance(dialogDefaultUiModel: DialogDefaultUiModel): DefaultDialogFragment =
            DefaultDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putParcelable(KEY_DEFAULT_DIALOG_UI_MODEL, dialogDefaultUiModel)
                    }
            }

        fun setResultListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (Boolean) -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, bundle ->
                bundle.getBoolean(BUNDLE_RESULT_YN).let(listener)
            }
        }
    }
}
