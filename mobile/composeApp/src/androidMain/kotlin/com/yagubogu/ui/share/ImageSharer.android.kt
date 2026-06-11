package com.yagubogu.ui.share

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberImageSharer(): ImageSharer {
    val context: Context = LocalContext.current

    return remember(context) {
        AndroidImageSharer(context = context)
    }
}

private class AndroidImageSharer(
    private val context: Context,
) : ImageSharer {
    override fun shareImage(
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String,
    ) {
        val shareDir = File(context.cacheDir, SHARE_DIRECTORY_NAME).apply { mkdirs() }
        val shareFile = File(shareDir, fileName).apply { writeBytes(imageBytes) }
        val shareUri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.share.fileprovider",
                shareFile,
            )

        val sendIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                clipData = ClipData.newUri(context.contentResolver, fileName, shareUri)
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        val chooserIntent =
            Intent.createChooser(sendIntent, null).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        context.startActivity(chooserIntent)
    }

    companion object {
        private const val SHARE_DIRECTORY_NAME = "share"
    }
}
