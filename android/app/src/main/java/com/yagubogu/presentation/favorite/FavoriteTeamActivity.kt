package com.yagubogu.presentation.favorite

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.ActivityFavoriteTeamBinding
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.MainActivity

class FavoriteTeamActivity : AppCompatActivity() {
    private val binding: ActivityFavoriteTeamBinding by lazy {
        ActivityFavoriteTeamBinding.inflate(layoutInflater)
    }

    private val viewModel: FavoriteTeamViewModel by viewModels {
        val app = application as YaguBoguApplication
        FavoriteTeamViewModelFactory(app.memberRepository)
    }

    private var selectedTeam: Team? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupRecyclerView()
        setupFragmentResultListener()
    }

    private fun setupView() {
        enableEdgeToEdge()
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
                        selectedTeam = item.team
                    }
                },
            )
        binding.rvFavoriteTeamList.adapter = adapter
        val favoriteTeamItems: List<FavoriteTeamItem> = Team.entries.map { FavoriteTeamItem.of(it) }
        adapter.submitList(favoriteTeamItems)
    }

    private fun setupFragmentResultListener() {
        supportFragmentManager.setFragmentResultListener(
            FavoriteTeamConfirmFragment.KEY_REQUEST_SUCCESS,
            this,
        ) { _, bundle ->
            val isConfirmed: Boolean = bundle.getBoolean(FavoriteTeamConfirmFragment.KEY_CONFIRM)
            if (isConfirmed) {
                selectedTeam?.let { viewModel.saveFavoriteTeam(it) }
                navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
