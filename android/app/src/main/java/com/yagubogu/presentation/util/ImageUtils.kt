package com.yagubogu.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ImageUtils {
    suspend fun compressImageWithCoil(
        context: Context,
        uri: Uri,
        maxSize: Int = 500,
        quality: Int = 85
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val imageLoader = ImageLoader(context)

            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(maxSize, maxSize)
                .allowHardware(false)
                .build()

            val result = imageLoader.execute(request)

            val bitmap = result.image?.toBitmap()
                ?: return@withContext null

            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpeg")
            outputFile.outputStream().use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    quality,
                    out
                )
            }

            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
