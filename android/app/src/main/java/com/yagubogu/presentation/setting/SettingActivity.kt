package com.yagubogu.presentation.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.ActivitySettingBinding

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
            finish()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, SettingActivity::class.java)
    }
}
