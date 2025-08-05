package com.yagubogu.presentation

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
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
import com.yagubogu.presentation.livetalk.LiveTalkFragment
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

    override fun onResume() {
        super.onResume()
        setToolbarTitle(binding.bnvNavigation.selectedItemId)
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
        binding.bnvNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (val itemId: Int = item.itemId) {
                R.id.item_home -> {
                    switchFragment(HomeFragment::class.java, itemId)
                    true
                }

                R.id.item_stats -> {
                    switchFragment(StatsFragment::class.java, itemId)
                    true
                }

                R.id.item_livetalk -> {
                    switchFragment(LiveTalkFragment::class.java, itemId)
                    true
                }

                R.id.item_challenge -> {
                    switchFragment(ChallengeFragment::class.java, itemId)
                    true
                }

                else -> false
            }
        }
    }

    private fun switchFragment(
        fragmentClass: Class<out Fragment>,
        selectedItemId: Int,
    ) {
        val tag: String = fragmentClass.name
        val targetFragment: Fragment? = supportFragmentManager.findFragmentByTag(tag)
        if (targetFragment?.isVisible == true) return

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            supportFragmentManager.fragments.forEach { if (it.isVisible) hide(it) }

            if (targetFragment == null) {
                add(binding.fcvFragment.id, fragmentClass, null, tag)
            } else {
                show(targetFragment)
            }
        }
        setToolbarTitle(selectedItemId)
    }

    private fun setToolbarTitle(
        @IdRes selectedItemId: Int,
    ) {
        @StringRes
        val titleResId: Int =
            when (selectedItemId) {
                R.id.item_home -> R.string.app_name
                R.id.item_stats -> R.string.bottom_navigation_stats
                R.id.item_livetalk -> R.string.bottom_navigation_livetalk
                R.id.item_challenge -> R.string.bottom_navigation_challenge
                else -> R.string.app_name
            }
        binding.tvToolbarTitle.text = getString(titleResId)
    }
}
