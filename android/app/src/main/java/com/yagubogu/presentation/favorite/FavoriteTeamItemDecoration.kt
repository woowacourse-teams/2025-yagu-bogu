package com.yagubogu.presentation.favorite

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class FavoriteTeamItemDecoration(
    private val spacingDp: Float = DEFAULT_SPACING_DP,
    private val context: Context,
    private val spanCount: Int = DEFAULT_SPAN_COUNT,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        val spacingPx: Int = dpToPixel(spacingDp)

        if (position >= 0) {
            val column = position % spanCount
            outRect.apply {
                left = spacingPx - column * spacingPx / spanCount
                right = (column + 1) * spacingPx / spanCount
                if (position < spanCount) top = spacingPx
                bottom = spacingPx
            }
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }

    private fun dpToPixel(spacingDp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (spacingDp * density).toInt()
    }

    companion object {
        private const val DEFAULT_SPACING_DP = 12f
        private const val DEFAULT_SPAN_COUNT = 2
    }
}
