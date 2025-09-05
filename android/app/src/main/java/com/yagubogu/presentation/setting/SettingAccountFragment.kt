package com.yagubogu.presentation.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.yagubogu.R
import com.yagubogu.databinding.FragmentSettingAccountBinding
import com.yagubogu.presentation.dialog.DefaultDialogFragment
import com.yagubogu.presentation.dialog.DefaultDialogUiModel

@Suppress("ktlint:standard:backing-property-naming")
class SettingAccountFragment : Fragment() {
    private var _binding: FragmentSettingAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingViewModel by activityViewModels()
    private var logoutDialog: DefaultDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.setSettingTitle(getString(R.string.setting_manage_account))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.layoutLogout.root.setOnClickListener {
            if (logoutDialog == null) {
                val dialogUiModel =
                    DefaultDialogUiModel(
                        title = getString(R.string.setting_logout),
                        message = getString(R.string.setting_logout_dialog_message),
                        positiveText = getString(R.string.setting_logout),
                    )
                logoutDialog =
                    DefaultDialogFragment.newInstance(KEY_LOGOUT_REQUEST_DIALOG, dialogUiModel)
            }

            logoutDialog?.show(parentFragmentManager, KEY_LOGOUT_REQUEST_DIALOG)
        }

        binding.layoutDeleteAccount.root.setOnClickListener {
            showDeleteAccountFragment()
        }
    }

    private fun showDeleteAccountFragment() {
        parentFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcv_setting, SettingDeleteAccountFragment())
            .addToBackStack(null)
            .commit()
    }

    companion object {
        const val KEY_LOGOUT_REQUEST_DIALOG = "logoutRequest"
    }
}
