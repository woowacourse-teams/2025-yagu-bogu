package com.yagubogu.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.yagubogu.domain.model.Team
import yagubogu.composeapp.generated.resources.Res
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

internal object ScoreWidgetMascotBitmapCache {
    private val bitmaps = LruCache<String, Bitmap>(Team.entries.size)

    suspend fun get(
        context: Context,
        teamCode: String,
    ): Bitmap? {
        val team = runCatching { Team.getByCode(teamCode) }.getOrNull() ?: return null
        synchronized(bitmaps) {
            bitmaps.get(team.name)?.let { return it }
        }

        val bitmap =
            try {
                val bytes = Res.readBytes("drawable/img_mascot_${team.name.lowercase()}.webp")
                decodeForNotification(context, bytes)
            } catch (exception: Exception) {
                if (exception is CancellationException) throw exception
                return null
            }

        synchronized(bitmaps) {
            return bitmaps.get(team.name) ?: bitmap.also { bitmaps.put(team.name, it) }
        }
    }

    private fun decodeForNotification(
        context: Context,
        bytes: ByteArray,
    ): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        val targetSizePx = (MAX_LOGO_SIZE_DP * context.resources.displayMetrics.density).roundToInt()
        val options =
            BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, targetSizePx)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
        return requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options))
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        targetSizePx: Int,
    ): Int {
        val sourceSize = max(width, height)
        var sampleSize = 1
        while (sourceSize / sampleSize > targetSizePx) {
            val nextSampleSize = sampleSize * 2
            if (abs(sourceSize / nextSampleSize - targetSizePx) >= abs(sourceSize / sampleSize - targetSizePx)) {
                break
            }
            sampleSize = nextSampleSize
        }
        return sampleSize
    }

    private const val MAX_LOGO_SIZE_DP = 36
}
