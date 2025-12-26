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
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.database.getLongOrNull
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.crashlytics.internal.common.IdManager.DEFAULT_VERSION_NAME
import com.yagubogu.R
import com.yagubogu.databinding.FragmentSettingMainBinding
import com.yagubogu.presentation.favorite.FavoriteTeamActivity
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.setting.SettingScreen
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

@Suppress("ktlint:standard:backing-property-naming")
class SettingMainFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent { SettingScreen() }
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

    companion object {
        private const val NOTICE_URL =
            "https://scented-allosaurus-6df.notion.site/251ad073c10b805baf8af1a7badd20e7?pvs=74"
        private const val CONTACT_URL = "https://forms.gle/wBhXjfTLyobZa19K8"
        private const val DEFAULT_VERSION_NAME = "x.x.x"
    }
}
