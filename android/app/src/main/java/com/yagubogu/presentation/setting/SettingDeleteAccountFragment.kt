package com.yagubogu.presentation.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.databinding.FragmentSettingDeleteAccountBinding
import com.yagubogu.presentation.dialog.DefaultDialogFragment
import com.yagubogu.presentation.dialog.DefaultDialogUiModel

@Suppress("ktlint:standard:backing-property-naming")
class SettingDeleteAccountFragment : Fragment() {
    private var _binding: FragmentSettingDeleteAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingViewModel by activityViewModels()

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingDeleteAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupBindings()
        setupListeners()
        setupFragmentResultListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBindings() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun setupListeners() {
        binding.btnConfirm.setOnClickListener {
            if (parentFragmentManager.findFragmentByTag(KEY_DELETE_ACCOUNT_REQUEST_DIALOG) == null) {
                val dialogUiModel =
                    DefaultDialogUiModel(
                        title = getString(R.string.setting_delete_account_dialog_title),
                        message = getString(R.string.setting_delete_account_dialog_message),
                        positiveText = getString(R.string.setting_delete_account),
                    )
                val dialog =
                    DefaultDialogFragment
                        .newInstance(KEY_DELETE_ACCOUNT_REQUEST_DIALOG, dialogUiModel)
                dialog.show(parentFragmentManager, KEY_DELETE_ACCOUNT_REQUEST_DIALOG)
            }
            firebaseAnalytics.logEvent("delete_account_confirm", null)
        }

        binding.btnCancel.setOnClickListener {
            viewModel.cancelDeleteAccount()
            firebaseAnalytics.logEvent("delete_account_cancel", null)
        }
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            KEY_DELETE_ACCOUNT_REQUEST_DIALOG,
            viewLifecycleOwner,
        ) { _, bundle ->
            val isConfirmed = bundle.getBoolean(DefaultDialogFragment.KEY_CONFIRM)
            if (isConfirmed) {
                viewModel.deleteAccount()
            }
        }
    }

    companion object {
        private const val KEY_DELETE_ACCOUNT_REQUEST_DIALOG = "deleteAccountRequest"
    }
}
