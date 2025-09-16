package com.yagubogu.presentation.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.yagubogu.databinding.FragmentDefaultDialogBinding
import com.yagubogu.presentation.util.getParcelableCompat

class DefaultDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val defaultDialogUiModel: DefaultDialogUiModel =
            arguments?.getParcelableCompat(KEY_DEFAULT_DIALOG_UI_MODEL)
                ?: return super.onCreateDialog(savedInstanceState)

        val binding: FragmentDefaultDialogBinding =
            FragmentDefaultDialogBinding.inflate(layoutInflater)

        binding.dialogUiModel = defaultDialogUiModel
        binding.tvPositiveBtn.setOnClickListener { setResultAndDismiss(true) }
        binding.tvNegativeBtn.setOnClickListener { setResultAndDismiss(false) }

        return AlertDialog
            .Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    private fun setResultAndDismiss(isConfirmed: Boolean) {
        val requestKey: String = arguments?.getString(KEY_REQUEST) ?: return
        val bundle = Bundle().apply { putBoolean(KEY_CONFIRM, isConfirmed) }
        setFragmentResult(requestKey, bundle)
        dismiss()
    }

    companion object {
        private const val KEY_DEFAULT_DIALOG_UI_MODEL = "defaultDialogUiModel"
        private const val KEY_REQUEST = "requestKey"
        const val KEY_CONFIRM = "confirm"

        fun newInstance(
            requestKey: String,
            defaultDialogUiModel: DefaultDialogUiModel,
        ): DefaultDialogFragment =
            DefaultDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(KEY_REQUEST, requestKey)
                        putParcelable(KEY_DEFAULT_DIALOG_UI_MODEL, defaultDialogUiModel)
                    }
            }
    }
}
