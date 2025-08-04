package com.yagubogu.presentation.favorite

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.yagubogu.databinding.DialogSelectionConfirmBinding
import com.yagubogu.presentation.util.getParcelableValue

class SelectionConfirmDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        activity?.let {
            val favoriteTeam: FavoriteTeamUiModel =
                arguments?.getParcelableValue(ARG_FAVORITE_TEAM) ?: return@let null

            val binding: DialogSelectionConfirmBinding =
                DialogSelectionConfirmBinding.inflate(layoutInflater)

            binding.favoriteTeamUiModel = favoriteTeam
            binding.tvNegativeBtn.setOnClickListener { dialog?.cancel() }

            val builder =
                AlertDialog
                    .Builder(requireActivity())
                    .setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    companion object {
        private const val ARG_FAVORITE_TEAM = "favorite_team"

        fun newInstance(favoriteTeam: FavoriteTeamUiModel): SelectionConfirmDialogFragment =
            SelectionConfirmDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putParcelable(ARG_FAVORITE_TEAM, favoriteTeam)
                    }
            }
    }
}
