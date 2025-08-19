package com.yagubogu.presentation.setting

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.yagubogu.R
import com.yagubogu.databinding.FragmentSettingMainBinding
import timber.log.Timber

@Suppress("ktlint:standard:backing-property-naming")
class SettingMainFragment : Fragment() {
    private var _binding: FragmentSettingMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSettingMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupBindings()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.setSettingTitle(getString(R.string.setting_main_title))
    }

    private fun setupBindings() {
        binding.viewModel = viewModel
        binding.appVersion = getAppVersion()
        binding.lifecycleOwner = this
    }

    private fun setupListeners() {
        binding.layoutEditNickname.root.setOnClickListener {
            val currentNickname = binding.tvMyNickName.text.toString()
            SettingNicknameEditFragment
                .newInstance(currentNickname)
                .show(parentFragmentManager, "SettingNicknameEditFragment")
        }

        binding.layoutManageAccount.root.setOnClickListener {
            showAccountManagementFragment()
        }
        binding.layoutNotice.root.setOnClickListener {
            openUrl("https://scented-allosaurus-6df.notion.site/251ad073c10b805baf8af1a7badd20e7?pvs=74")
        }
        binding.layoutContactUs.root.setOnClickListener {
            openUrl("https://forms.gle/wBhXjfTLyobZa19K8")
        }
        binding.layoutPrivacyPolicy.root.setOnClickListener {
            openUrl("https://sites.google.com/view/yagubogu-privacy-policy/%ED%99%88?authuser=4")
        }
        binding.layoutOpenSourceLicense.root.setOnClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
        }
    }

    private fun showAccountManagementFragment() {
        parentFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcv_setting, SettingAccountFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }

    private fun getAppVersion(): String =
        try {
            val packageInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            packageInfo.versionName ?: DEFAULT_VERSION_NAME
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.d("앱 버전 로드 실패 ${e.message}")
            DEFAULT_VERSION_NAME
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DEFAULT_VERSION_NAME = "x.x.x"
    }
}
