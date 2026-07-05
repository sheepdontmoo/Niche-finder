package com.nichefinder.scan.pdf

/**
 * A single OCR word/token with its pixel-space bounding box on the source page image.
 *
 * Coordinates use the same convention as ML Kit's `Text.Element.boundingBox` (and Android's
 * `Rect`): origin at the image's top-left corner, `top`/`left` in pixels, y increasing downward.
 */
data class OcrWord(
    val text: String,
    val left: Double,
    val top: Double,
    val width: Double,
    val height: Double,
) {
    init {
        require(text.isNotEmpty()) { "OCR word text must not be empty" }
        require(width > 0 && height > 0) { "OCR word '$text' has non-positive size" }
    }
}

/**
 * One scanned page ready to be embedded in a PDF: the JPEG bytes exactly as produced by the
 * scanner (never re-encoded), its pixel dimensions, and the OCR text layer recognized on it.
 */
class ScanPage(
    val jpegBytes: ByteArray,
    val pixelWidth: Int,
    val pixelHeight: Int,
    val words: List<OcrWord> = emptyList(),
) {
    init {
        require(jpegBytes.isNotEmpty()) { "Page image bytes must not be empty" }
        require(pixelWidth > 0 && pixelHeight > 0) { "Page dimensions must be positive, got ${pixelWidth}x$pixelHeight" }
    }
}
