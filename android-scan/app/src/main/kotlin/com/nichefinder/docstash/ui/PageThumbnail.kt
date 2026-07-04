package com.nichefinder.docstash.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Decodes a downsampled thumbnail off the main thread — scanner JPEGs can be several MB full-size. */
@Composable
fun rememberPageThumbnail(file: File, reqWidthPx: Int = 400): ImageBitmap? {
    var bitmap by remember(file.absolutePath, file.lastModified()) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(file.absolutePath, file.lastModified()) {
        bitmap = withContext(Dispatchers.IO) { decodeSampled(file, reqWidthPx)?.asImageBitmap() }
    }
    return bitmap
}

private fun decodeSampled(file: File, reqWidth: Int): Bitmap? {
    if (!file.exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0) return null
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= reqWidth) sample *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(file.absolutePath, opts)
}
