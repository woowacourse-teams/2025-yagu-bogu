package com.yagubogu.presentation.setting

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.ActivitySettingBinding
import com.yagubogu.presentation.favorite.FavoriteTeamActivity
import timber.log.Timber

class SettingActivity : AppCompatActivity() {
    private val binding: ActivitySettingBinding by lazy {
        ActivitySettingBinding.inflate(layoutInflater)
    }

    private val viewModel: SettingViewModel by viewModels {
        val app = application as YaguBoguApplication
        SettingViewModelFactory(app.memberRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupBindings()
        setupListener()
        setupObservers()
        setupIntents()
    }

    private fun setupView() {
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.constraintActivitySettingRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBindings() {
        binding.viewModel = viewModel
        binding.appVersion = getAppVersion()
        binding.lifecycleOwner = this
    }

    private fun setupListener() {
        binding.ivArrowLeft.setOnClickListener {
            finish()
        }

        SettingNicknameEditFragment.setResultListener(
            supportFragmentManager,
            this,
        ) { newNickname: String ->
            viewModel.updateNickname(newNickname)
        }
    }

    private fun setupObservers() {
        viewModel.nicknameEditedEvent.observe(this) { newNickname: String ->
            showSnackbar(getString(R.string.setting_edited_nickname_alert, newNickname))
        }
    }

    private fun setupIntents() {
        binding.layoutEditFavoriteTeam.root.setOnClickListener {
            startActivity(Intent(this, FavoriteTeamActivity::class.java))
        }
        binding.layoutEditNickname.root.setOnClickListener {
            val currentNickname = binding.tvMyNickName.text.toString()
            SettingNicknameEditFragment
                .newInstance(currentNickname)
                .show(supportFragmentManager, "SettingNicknameEditFragment")
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
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }

    private fun getAppVersion(): String =
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: DEFAULT_VERSION_NAME
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.d("앱 버전 로드 실패 ${e.message}")
            DEFAULT_VERSION_NAME
        }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            setBackgroundTint(Color.DKGRAY)
            setTextColor(context.getColor(R.color.white))
            show()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, SettingActivity::class.java)

        private const val DEFAULT_VERSION_NAME = "x.x.x"
    }
}
