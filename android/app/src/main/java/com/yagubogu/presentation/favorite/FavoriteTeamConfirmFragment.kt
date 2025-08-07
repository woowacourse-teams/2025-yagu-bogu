package com.yagubogu.presentation.favorite

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.yagubogu.databinding.FragmentFavoriteTeamConfirmBinding
import com.yagubogu.presentation.util.getParcelableCompat

class FavoriteTeamConfirmFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val favoriteTeam: FavoriteTeamUiModel =
            arguments?.getParcelableCompat(ARG_FAVORITE_TEAM)
                ?: return super.onCreateDialog(savedInstanceState)

        val binding: FragmentFavoriteTeamConfirmBinding =
            FragmentFavoriteTeamConfirmBinding.inflate(layoutInflater)

        binding.favoriteTeamUiModel = favoriteTeam
        binding.tvNegativeBtn.setOnClickListener { dialog?.cancel() }

        return AlertDialog
            .Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    companion object {
        private const val ARG_FAVORITE_TEAM = "favorite_team"

        fun newInstance(favoriteTeam: FavoriteTeamUiModel): FavoriteTeamConfirmFragment =
            FavoriteTeamConfirmFragment().apply {
                arguments =
                    Bundle().apply {
                        putParcelable(ARG_FAVORITE_TEAM, favoriteTeam)
                    }
            }
    }
}
