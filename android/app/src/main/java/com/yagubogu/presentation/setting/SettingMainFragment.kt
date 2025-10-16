package com.yagubogu.presentation.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.database.getLongOrNull
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.yagubogu.R
import com.yagubogu.databinding.FragmentSettingMainBinding
import com.yagubogu.presentation.favorite.FavoriteTeamActivity
import com.yagubogu.presentation.util.showSnackbar
import com.yagubogu.presentation.util.showToast
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

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
    private val uCropLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val intent: Intent = result.data ?: return@registerForActivityResult
                    val resultUri: Uri = UCrop.getOutput(intent) ?: return@registerForActivityResult
                    handleCroppedImage(resultUri)
                }

                UCrop.RESULT_ERROR -> {
                    val cropError: Throwable? = result.data?.let { UCrop.getError(it) }
                    Timber.e(cropError, "uCrop Error")
                    requireContext().showToast(getString(R.string.setting_edit_profile_image_crop_failed))
                }
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
        binding.layoutEditFavoriteTeam.root.setOnClickListener {
            startActivity(Intent(requireContext(), FavoriteTeamActivity::class.java))
        }

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
            openUrl(NOTICE_URL)
        }
        binding.layoutContactUs.root.setOnClickListener {
            openUrl(CONTACT_URL)
        }
        binding.layoutOpenSourceLicense.root.setOnClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
        }
    }

    private fun launchUCropActivity(sourceUri: Uri) {
        val fileName = "cropped_image_${System.currentTimeMillis()}"
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, fileName))

        val options: UCrop.Options =
            UCrop.Options().apply {
                setFreeStyleCropEnabled(false)
                setHideBottomControls(false)
                setCircleDimmedLayer(true)
                setToolbarColor(requireContext().getColor(R.color.gray050))
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(85)
            }

        val uCropIntent: Intent? =
            UCrop
                .of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(500, 500)
                .withOptions(options)
                .getIntent(requireContext())

        uCropLauncher.launch(uCropIntent)
    }

    private fun handleCroppedImage(uri: Uri) {
        lifecycleScope.launch {
            runCatching {
                val mimeType: String =
                    requireContext()
                        .contentResolver
                        .getType(uri) ?: "image/jpeg"

                val fileSize: Long =
                    uri
                        .fileSize(requireContext())
                        .getOrNull()
                        ?: error("파일 사이즈 획득 실패")
                viewModel.uploadProfileImage(
                    uri,
                    mimeType,
                    fileSize,
                )
            }.fold(
                onSuccess = { result: Result<Unit> ->
                    result.onFailure { e ->
                        if (e is CancellationException) throw e
                        requireContext().showToast(getString(R.string.setting_edit_profile_image_upload_failed))
                    }
                },
                onFailure = { e: Throwable ->
                    if (e is CancellationException) throw e
                    Timber.e(e, "프로필 이미지 전처리 실패")
                    requireContext().showToast(getString(R.string.setting_edit_profile_image_processing_failed))
                },
            )
        }
    }

    private fun Uri.fileSize(context: Context): Result<Long?> =
        runCatching {
            context.contentResolver
                .query(this, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)
                        cursor.getLongOrNull(idx)
                    } else {
                        null
                    }
                }
                ?: context.contentResolver
                    .openFileDescriptor(this, "r")
                    ?.use { parcelFileDescriptor: ParcelFileDescriptor ->
                        parcelFileDescriptor.statSize.takeIf { it >= 0 }
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
        private const val NOTICE_URL =
            "https://scented-allosaurus-6df.notion.site/251ad073c10b805baf8af1a7badd20e7?pvs=74"
        private const val CONTACT_URL = "https://forms.gle/wBhXjfTLyobZa19K8"
        private const val DEFAULT_VERSION_NAME = "x.x.x"
    }
}
