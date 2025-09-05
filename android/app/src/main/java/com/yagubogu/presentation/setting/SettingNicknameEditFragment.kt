package com.yagubogu.presentation.setting

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.yagubogu.databinding.FragmentSettingNicknameEditBinding

class SettingNicknameEditFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentNickname: String =
            arguments?.getString(ARG_CURRENT_NICKNAME)
                ?: return super.onCreateDialog(savedInstanceState)

        val binding: FragmentSettingNicknameEditBinding =
            FragmentSettingNicknameEditBinding.inflate(layoutInflater)

        binding.etEditNicknameField.setText(currentNickname)

        binding.tvPositiveBtn.setOnClickListener {
            val newNickname: String = binding.etEditNicknameField.text.toString()
            setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_NICKNAME to newNickname))
            dismiss()
        }
        binding.tvNegativeBtn.setOnClickListener { dismiss() }

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
