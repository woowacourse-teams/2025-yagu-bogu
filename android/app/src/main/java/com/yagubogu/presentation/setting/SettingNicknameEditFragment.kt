package com.yagubogu.presentation.setting

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.yagubogu.databinding.FragmentEditNicknameConfirmBinding

class SettingNicknameEditFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentNickname = arguments?.getString(ARG_CURRENT_NICKNAME) ?: ""

        val binding: FragmentEditNicknameConfirmBinding =
            FragmentEditNicknameConfirmBinding.inflate(layoutInflater)

        binding.etEditNicknameField.setText(currentNickname)

        binding.tvNegativeBtn.setOnClickListener {
            dismiss()
        }

        binding.tvPositiveBtn.setOnClickListener {
            val newNickname = binding.etEditNicknameField.text.toString()
            setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_NICKNAME to newNickname))
            dismiss()
        }

        return AlertDialog
            .Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    companion object {
        private const val REQUEST_KEY = "nicknameEditRequest"
        private const val BUNDLE_KEY_NICKNAME = "newNickname"
        private const val ARG_CURRENT_NICKNAME = "current_nickname"

        fun newInstance(currentNickname: String): SettingNicknameEditFragment =
            SettingNicknameEditFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(ARG_CURRENT_NICKNAME, currentNickname)
                    }
            }

        fun setResultListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (String) -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, bundle ->
                bundle.getString(BUNDLE_KEY_NICKNAME)?.let(listener)
            }
        }
    }
}
