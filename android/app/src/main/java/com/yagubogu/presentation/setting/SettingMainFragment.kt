package com.yagubogu.presentation.setting

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.yagubogu.R
import com.yagubogu.databinding.FragmentSettingMainBinding
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.io.File

@Suppress("ktlint:standard:backing-property-naming")
class SettingMainFragment : Fragment() {
    private var _binding: FragmentSettingMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingViewModel by activityViewModels()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                launchUCropActivity(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri: Uri? = data?.let { UCrop.getOutput(it) }
            resultUri?.let {
                // todo: 뷰모델 전달 필요
                Toast.makeText(requireContext(), "이미지 처리 시작: $it", Toast.LENGTH_SHORT).show()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = data?.let { UCrop.getError(it) }
            Timber.e(cropError, "uCrop Error")
            Toast.makeText(requireContext(), "이미지 자르기에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBindings() {
        binding.viewModel = viewModel
        binding.appVersion = getAppVersion()
        binding.lifecycleOwner = this
    }

    private fun setupListeners() {
        binding.layoutEditProfileImage.root.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.layoutEditNickname.root.setOnClickListener {
            val currentNickname: String =
                viewModel.myMemberInfoItem.value
                    ?.nickName
                    .toString()
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
        binding.layoutOpenSourceLicense.root.setOnClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
        }
    }

    private fun launchUCropActivity(sourceUri: Uri) {
        val fileName = "cropped_image_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, fileName))

        val options = UCrop.Options()
        options.setFreeStyleCropEnabled(false)
        options.setHideBottomControls(false)
        options.setCircleDimmedLayer(true)
        options.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.primary500))

        UCrop
            .of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1080, 1080)
            .withOptions(options)
            .start(requireActivity(), this)
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
            val packageInfo: PackageInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            packageInfo.versionName ?: DEFAULT_VERSION_NAME
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.d("앱 버전 로드 실패 ${e.message}")
            DEFAULT_VERSION_NAME
        }

    companion object {
        private const val DEFAULT_VERSION_NAME = "x.x.x"
    }
}
