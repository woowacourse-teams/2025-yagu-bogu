package com.yagubogu.presentation.favorite

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yagubogu.databinding.ActivityFavoriteTeamBinding
import com.yagubogu.domain.model.Team

class FavoriteTeamActivity : AppCompatActivity() {
    private val binding: ActivityFavoriteTeamBinding by lazy {
        ActivityFavoriteTeamBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupRecyclerView()
    }

    private fun setupView() {
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        val adapter = FavoriteTeamListAdapter()
        binding.rvFavoriteTeamList.adapter = adapter
        adapter.submitList(DUMMY_FAVORITE_TEAMS)
        Log.d("FavoriteTeamActivity", DUMMY_FAVORITE_TEAMS.toString())

        binding.rvFavoriteTeamList.addItemDecoration(FavoriteTeamItemDecoration(context = this))
    }

    companion object {
        // TODO 어떻게 생성할 것인지 결정
        private val DUMMY_FAVORITE_TEAMS = Team.entries.map { FavoriteTeamUiModel(it) }
    }
}
