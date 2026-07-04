package com.nichefinder.docstash.ocr

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nichefinder.scan.pdf.OcrWord
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream

/** A freshly scanned + OCR'd page, ready to hand to [com.nichefinder.docstash.data.DocumentStore]. */
class ScannedPage(
    val jpegBytes: ByteArray,
    val pixelWidth: Int,
    val pixelHeight: Int,
    val text: String,
    val words: List<OcrWord>,
)

/**
 * Wraps ML Kit's on-device Text Recognition (Latin script, bundled model — ships in the APK, no
 * Play services download wait, works fully offline). Run once per page scanned by
 * [com.nichefinder.docstash.scanner.rememberDocumentScanner] to build the text layer that makes
 * the exported PDF searchable and powers the library's search-by-OCR-text feature.
 */
class OcrProcessor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognize(context: Context, imageUri: Uri): ScannedPage {
        val jpegBytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: error("Couldn't read scanned page at $imageUri")

        val (width, height) = decodedDimensions(jpegBytes)

        val image = InputImage.fromFilePath(context, imageUri)
        val visionText = recognizer.process(image).await()

        val words = visionText.textBlocks
            .flatMap { it.lines }
            .flatMap { it.elements }
            .mapNotNull { element ->
                val box = element.boundingBox ?: return@mapNotNull null
                OcrWord(
                    text = element.text,
                    left = box.left.toDouble(),
                    top = box.top.toDouble(),
                    width = (box.right - box.left).toDouble(),
                    height = (box.bottom - box.top).toDouble(),
                )
            }

        return ScannedPage(jpegBytes, width, height, visionText.text, words)
    }

    /** Raw decode bounds, corrected for EXIF rotation so they match the upright image ML Kit sees. */
    private fun decodedDimensions(jpegBytes: ByteArray): Pair<Int, Int> {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size, bounds)

        val orientation = ByteArrayInputStream(jpegBytes).use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }
        val rotated90or270 = orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
            orientation == ExifInterface.ORIENTATION_ROTATE_270
        return if (rotated90or270) bounds.outHeight to bounds.outWidth else bounds.outWidth to bounds.outHeight
    }
}
