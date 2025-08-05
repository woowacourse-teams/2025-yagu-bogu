package com.yagubogu.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.yagubogu.R
import com.yagubogu.databinding.ActivityMainBinding
import com.yagubogu.presentation.challenge.ChallengeFragment
import com.yagubogu.presentation.home.HomeFragment
import com.yagubogu.presentation.livetalk.LivetalkFragment
import com.yagubogu.presentation.stats.StatsFragment

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupBottomNavigationView()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if (savedInstanceState == null) {
            binding.bnvNavigation.selectedItemId = R.id.item_home
        }
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
                    replaceFragment(LivetalkFragment::class.java, R.string.bottom_navigation_livetalk)

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
}
