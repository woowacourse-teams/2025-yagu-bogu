package com.yagubogu.presentation.favorite

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.yagubogu.R
import com.yagubogu.databinding.FragmentFavoriteTeamConfirmBinding
import com.yagubogu.presentation.util.getParcelableCompat

class FavoriteTeamConfirmFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val favoriteTeam: FavoriteTeamItem =
            arguments?.getParcelableCompat(KEY_FAVORITE_TEAM)
                ?: return super.onCreateDialog(savedInstanceState)

        val binding: FragmentFavoriteTeamConfirmBinding =
            FragmentFavoriteTeamConfirmBinding.inflate(layoutInflater)

        binding.favoriteTeamItem = favoriteTeam
        binding.tvPositiveBtn.setOnClickListener { setResultAndDismiss(true) }
        binding.tvNegativeBtn.setOnClickListener { setResultAndDismiss(false) }

        val dialog =
            AlertDialog
                .Builder(requireActivity())
                .setView(binding.root)
                .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_white_radius_12dp)

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    private fun setResultAndDismiss(isConfirmed: Boolean) {
        val bundle = Bundle().apply { putBoolean(KEY_CONFIRM, isConfirmed) }
        setFragmentResult(KEY_REQUEST_SUCCESS, bundle)
        dismiss()
    }

    companion object {
        private const val KEY_FAVORITE_TEAM = "favorite_team"
        const val KEY_REQUEST_SUCCESS = "success"
        const val KEY_CONFIRM = "confirm"

        fun newInstance(favoriteTeam: FavoriteTeamItem): FavoriteTeamConfirmFragment =
            FavoriteTeamConfirmFragment().apply {
                arguments =
                    Bundle().apply {
                        putParcelable(KEY_FAVORITE_TEAM, favoriteTeam)
                    }
            }
    }
}
