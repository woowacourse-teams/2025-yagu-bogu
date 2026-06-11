package com.yagubogu.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberImageSharer(): ImageSharer {
    val viewController = LocalUIViewController.current
    return remember(viewController) {
        IosImageSharer(viewController = viewController)
    }
}

private class IosImageSharer(
    private val viewController: UIViewController,
) : ImageSharer {
    @OptIn(ExperimentalForeignApi::class)
    override fun shareImage(
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String,
    ) {
        val imageData =
            imageBytes.usePinned { pinned ->
                NSData.dataWithBytes(
                    bytes = pinned.addressOf(0),
                    length = imageBytes.size.toULong(),
                )
            }
        val tempDirectory = NSTemporaryDirectory()
        val fileURL = NSURL.fileURLWithPath(tempDirectory + fileName)
        val isSaved = imageData.writeToURL(url = fileURL, atomically = true)
        if (!isSaved) {
            return
        }

        dispatch_async(dispatch_get_main_queue()) {
            val activityViewController =
                UIActivityViewController(
                    activityItems = listOf(fileURL),
                    applicationActivities = null,
                )

            activityViewController.popoverPresentationController?.apply {
                sourceView = viewController.view
                sourceRect = viewController.view.bounds
            }

            viewController.presentViewController(
                viewControllerToPresent = activityViewController,
                animated = true,
                completion = null,
            )
        }
    }
}
