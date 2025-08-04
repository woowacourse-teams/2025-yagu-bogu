package com.yagubogu.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.yagubogu.R
import com.yagubogu.databinding.ActivityMainBinding
import com.yagubogu.presentation.challenge.ChallengeFragment
import com.yagubogu.presentation.home.HomeFragment
import com.yagubogu.presentation.livetalk.LiveTalkFragment
import com.yagubogu.presentation.stats.StatsFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var isAppInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplash()
        super.onCreate(savedInstanceState)
        setupView()
        setupBottomNavigationView()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        performInitialization(savedInstanceState)
    }

    private fun setupSplash() {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isAppInitialized }
    }

    private fun setupView() {
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.constraintActivityMainRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBottomNavigationView() {
        binding.bnvNavigation.setOnApplyWindowInsetsListener(null)
        binding.bnvNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.item_home ->
                    replaceFragment(HomeFragment::class.java, R.string.app_name)

                R.id.item_stats ->
                    replaceFragment(StatsFragment::class.java, R.string.bottom_navigation_stats)

                R.id.item_livetalk ->
                    replaceFragment(LiveTalkFragment::class.java, R.string.bottom_navigation_livetalk)

                R.id.item_challenge ->
                    replaceFragment(
                        ChallengeFragment::class.java,
                        R.string.bottom_navigation_challenge,
                    )

                else -> false
            }
        }
    }

    private fun replaceFragment(
        fragment: Class<out Fragment>,
        @StringRes titleResId: Int,
    ): Boolean {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(binding.fcvFragment.id, fragment, null)
        }
        setToolbarTitle(titleResId)
        return true
    }

    private fun setToolbarTitle(
        @StringRes titleResId: Int,
    ) {
        binding.tvToolbarTitle.text = getString(titleResId)
    }

    private fun performInitialization(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            try {
                // Todo : 초기화 작업 수행, (MainViewModel에서 초기 Api 요청, 데이터베이스 조회 등)
            } catch (e: Exception) {
                Log.e("MainActivity", "초기화 실패", e)
            } finally {
                isAppInitialized = true
                if (savedInstanceState == null) {
                    binding.bnvNavigation.selectedItemId = R.id.item_home
                }
            }
        }
    }
}
