package com.yagubogu.presentation.setting

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.ActivitySettingBinding
import com.yagubogu.presentation.dialog.DefaultDialogFragment
import com.yagubogu.presentation.login.LoginActivity
import com.yagubogu.presentation.setting.SettingAccountFragment.Companion.KEY_LOGIN_REQUEST_DIALOG

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
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fcv_setting, SettingMainFragment())
                .commit()
        }

        setupBindings()
        setupListener()
        setupObservers()
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
        binding.lifecycleOwner = this
    }

    private fun setupListener() {
        binding.ivArrowLeft.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }

        SettingNicknameEditFragment.setResultListener(
            supportFragmentManager,
            this,
        ) { newNickname: String ->
            viewModel.updateNickname(newNickname)
        }

        supportFragmentManager.setFragmentResultListener(
            KEY_LOGIN_REQUEST_DIALOG,
            this,
        ) { _, bundle ->
            val isConfirmed = bundle.getBoolean(DefaultDialogFragment.KEY_CONFIRM)
            if (isConfirmed) {
                viewModel.logout()
            }
        }
    }

    private fun setupObservers() {
        viewModel.nicknameEditedEvent.observe(this) { newNickname: String ->
            showSnackbar(getString(R.string.setting_edited_nickname_alert, newNickname))
        }
        viewModel.logoutEvent.observe(this) {
            showToast(getString(R.string.setting_logout_alert))
            navigateToLogin()
        }
        viewModel.deleteAccountEvent.observe(this) {
            showToast(getString(R.string.setting_delete_account_confirm_select_alert))
            navigateToLogin()
        }
        viewModel.deleteAccountCancelEvent.observe(this) {
            showToast(getString(R.string.setting_delete_account_cancel_select_alert))
            finish()
        }
    }

    private fun navigateToLogin() {
        val intent =
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        startActivity(intent)
        finish()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            setBackgroundTint(Color.DKGRAY)
            setTextColor(context.getColor(R.color.white))
            show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, SettingActivity::class.java)
    }
}
