package com.yagubogu.presentation.favorite

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yagubogu.databinding.ActivityFavoriteTeamBinding
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.favorite.FavoriteTeamItem
import com.yagubogu.ui.favorite.FavoriteTeamViewModel
import com.yagubogu.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoriteTeamActivity : AppCompatActivity() {
    private val binding: ActivityFavoriteTeamBinding by lazy {
        ActivityFavoriteTeamBinding.inflate(layoutInflater)
    }

    private val viewModel: FavoriteTeamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupRecyclerView()
        // setupObservers()
        setupFragmentResultListener()
    }

    private fun setupView() {
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars = true
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.constraintFavoriteTeamRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        val adapter =
            FavoriteTeamAdapter(
                object : FavoriteTeamViewHolder.Handler {
                    override fun onItemClick(item: FavoriteTeamItem) {
                        val dialog = FavoriteTeamConfirmFragment.newInstance(item)
                        dialog.show(supportFragmentManager, dialog.tag)
                        viewModel.selectTeam(item.team)
                    }
                },
            )
        binding.rvFavoriteTeamList.adapter = adapter
        val favoriteTeamItems: List<FavoriteTeamItem> = Team.entries.map { FavoriteTeamItem.of(it) }
        adapter.submitList(favoriteTeamItems)
    }

//    private fun setupObservers() {
//        viewModel.favoriteTeamUpdateEvent.observe(this) {
//            navigateToMain()
//        }
//    }

    private fun setupFragmentResultListener() {
        supportFragmentManager.setFragmentResultListener(
            FavoriteTeamConfirmFragment.KEY_REQUEST_SUCCESS,
            this,
        ) { _, bundle ->
            val isConfirmed: Boolean = bundle.getBoolean(FavoriteTeamConfirmFragment.KEY_CONFIRM)
            if (isConfirmed) {
                viewModel.saveFavoriteTeam()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(MainActivity.newIntent(this))
        finish()
    }
}
