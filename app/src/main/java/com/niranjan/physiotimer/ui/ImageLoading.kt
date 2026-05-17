package com.niranjan.physiotimer.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun loadBoundedImageBitmap(
    context: Context,
    imageUri: String?,
    maxSizePx: Int
): ImageBitmap? {
    if (imageUri.isNullOrBlank()) return null

    return withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(imageUri)
            val resolver = context.applicationContext.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }

            resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return@runCatching null
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = thumbnailSampleSize(
                    width = bounds.outWidth,
                    height = bounds.outHeight,
                    maxSizePx = maxSizePx
                )
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)?.asImageBitmap()
            }
        }.getOrNull()
    }
}

private fun thumbnailSampleSize(
    width: Int,
    height: Int,
    maxSizePx: Int
): Int {
    val boundedMax = maxSizePx.coerceAtLeast(1)
    var sampleSize = 1

    while (width / sampleSize > boundedMax || height / sampleSize > boundedMax) {
        sampleSize *= 2
    }

    return sampleSize
}
