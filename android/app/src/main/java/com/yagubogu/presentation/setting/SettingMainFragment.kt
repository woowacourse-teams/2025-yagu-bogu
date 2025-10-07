package com.yagubogu.presentation.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.yagubogu.R
import com.yagubogu.databinding.FragmentSettingMainBinding
import com.yagubogu.presentation.util.ImageUtils
import com.yagubogu.presentation.util.showToast
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
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
    private val uCropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            resultUri?.let { uri ->
                handleCroppedImage(uri)
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = result.data?.let { UCrop.getError(it) }
            Timber.e(cropError, "uCrop Error")
            requireContext().showToast(getString(R.string.setting_edit_profile_image_crop_failed))
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

        val uCropIntent = UCrop
            .of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1000, 1000)
            .withOptions(options)
            .getIntent(requireContext())

        uCropLauncher.launch(uCropIntent)
    }

    private fun handleCroppedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val compressedUri = ImageUtils.compressImageWithCoil(
                    context = requireContext(),
                    uri = uri,
                    maxSize = 500,
                    quality = 85
                )

                if (compressedUri != null) {
                    val mimeType = requireContext().contentResolver.getType(compressedUri)
                        ?: "image/jpeg"

                    val fileSize = compressedUri.getFileSize(requireContext())

                    if (fileSize > 0) {
                        viewModel.uploadProfileImage(
                            imageUri = compressedUri,
                            mimeType = mimeType,
                            size = fileSize
                        )
                    } else {
                        requireContext().showToast(getString(R.string.setting_edit_profile_get_image_size_failed))
                    }
                } else {
                    requireContext().showToast(
                        getString(R.string.setting_edit_profile_image_processing_failed)
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Image processing error")
                requireContext().showToast(
                    getString(R.string.setting_edit_profile_image_processing_failed)
                )
            }
        }
    }

    private fun Uri.getFileSize(context: Context): Long {
        return try {
            context.contentResolver.query(
                this,
                arrayOf(MediaStore.Images.Media.SIZE),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                    cursor.getLong(sizeIndex)
                } else {
                    -1L
                }
            } ?: run {
                context.contentResolver.openFileDescriptor(this, "r")?.use { pfd ->
                    pfd.statSize
                } ?: -1L
            }
        } catch (e: Exception) {
            Timber.e(e, "파일 사이즈 얻기 실패")
            -1L
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
