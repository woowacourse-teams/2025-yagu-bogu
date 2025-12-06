package com.yagubogu.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.yagubogu.R
import com.yagubogu.databinding.ActivityMainBinding
import com.yagubogu.presentation.attendance.AttendanceHistoryFragment
import com.yagubogu.presentation.home.HomeFragment
import com.yagubogu.presentation.livetalk.LivetalkFragment
import com.yagubogu.presentation.setting.SettingActivity
import com.yagubogu.presentation.util.ScrollToTop
import com.yagubogu.presentation.util.showSnackbar
import com.yagubogu.ui.badge.BadgeActivity
import com.yagubogu.ui.stats.StatsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var lastBackPressedTime: Long = 0L

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupBindings()
        setupBottomNavigationView()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        handleBackPress()

        if (savedInstanceState == null) {
            binding.bnvNavigation.selectedItemId = R.id.item_home
            val homeMenuItem = binding.bnvNavigation.menu.findItem(R.id.item_home)
            switchFragment(
                HomeFragment::class.java,
                homeMenuItem,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle(binding.bnvNavigation.selectedItemId)
    }

    fun setLoadingScreen(isLoading: Boolean) {
        val visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.viewOverlay.visibility = visibility
        binding.cpiCheckInLoading.visibility = visibility
    }

    private fun setupView() {
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars = true
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.constraintActivityMainRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBindings() {
        binding.ivBadge.setOnClickListener {
            val intent = BadgeActivity.newIntent(this)
            startActivity(intent)
        }

        binding.ivSettings.setOnClickListener {
            val intent = SettingActivity.newIntent(this)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigationView() {
        binding.bnvNavigation.setOnApplyWindowInsetsListener(null)
        binding.bnvNavigation.setOnItemSelectedListener { item: MenuItem ->
            val fragmentClass =
                when (item.itemId) {
                    R.id.item_home -> HomeFragment::class.java
                    R.id.item_stats -> StatsFragment::class.java
                    R.id.item_livetalk -> LivetalkFragment::class.java
                    R.id.item_attendance_history -> AttendanceHistoryFragment::class.java
                    else -> null
                }

            if (fragmentClass != null) {
                switchFragment(fragmentClass, item)
                true
            } else {
                false
            }
        }

        binding.bnvNavigation.setOnItemReselectedListener {
            val currentFragment: Fragment? =
                supportFragmentManager.fragments.firstOrNull { it.isVisible }

            if (currentFragment is ScrollToTop) {
                currentFragment.scrollToTop()
            }
        }
    }

    private fun switchFragment(
        fragmentClass: Class<out Fragment>,
        item: MenuItem,
    ) {
        val tag: String = fragmentClass.name
        val targetFragment: Fragment? = supportFragmentManager.findFragmentByTag(tag)
        if (targetFragment?.isVisible == true) return

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            supportFragmentManager.fragments.forEach { if (it != targetFragment) hide(it) }

            if (targetFragment == null) {
                add(binding.fcvFragment.id, fragmentClass, null, tag)
            } else {
                show(targetFragment)
            }
        }
        setToolbarTitle(item.itemId)

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "${item.title} Fragment")
        }
    }

    private fun setToolbarTitle(
        @IdRes selectedItemId: Int,
    ) {
        @StringRes
        val titleResId: Int =
            when (selectedItemId) {
                R.id.item_home -> R.string.app_name
                R.id.item_stats -> R.string.bottom_navigation_stats
                R.id.item_attendance_history -> R.string.bottom_navigation_attendance_history
                R.id.item_livetalk -> R.string.bottom_navigation_livetalk
                else -> R.string.app_name
            }
        binding.tvToolbarTitle.text = getString(titleResId)
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentTime: Long = System.currentTimeMillis()
                    if (currentTime - lastBackPressedTime > BACK_PRESS_INTERVAL) {
                        lastBackPressedTime = currentTime
                        binding.root.showSnackbar(
                            R.string.main_back_press_to_exit,
                            R.id.bnv_navigation,
                        )
                    } else {
                        finish()
                    }
                }
            },
        )
    }

    companion object {
        private const val BACK_PRESS_INTERVAL = 1500L

        fun newIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }
}
