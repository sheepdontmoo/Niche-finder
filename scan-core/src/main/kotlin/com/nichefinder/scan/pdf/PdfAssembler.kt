package com.nichefinder.scan.pdf

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.Locale

/**
 * Assembles a multi-page searchable PDF from scanned page JPEGs and their OCR text layers —
 * no external PDF library, because none is needed for what this app actually does.
 *
 * Each page embeds its JPEG directly as a `/DCTDecode` image XObject (the camera/ML Kit's bytes
 * go in unmodified — no re-encoding, no quality loss) plus an invisible text layer
 * (PDF text-render-mode `3 Tr`) positioned from OCR word boxes. The result opens like a normal
 * scanned PDF in any viewer, but the text underneath is selectable and searchable — the standard
 * "searchable PDF" technique, hand-written against the PDF 1.4 spec (objects, content streams,
 * cross-reference table, trailer).
 *
 * Text-layer positioning is approximate (word origin + a font size matched to box height, no
 * per-glyph horizontal scaling) — good enough that a text selection lands on the right word, not
 * pixel-perfect character-by-character alignment. That trade-off is deliberate: exact glyph
 * alignment needs font metrics tables for zero product value here, since the text is invisible
 * and only used for search/copy, never displayed.
 */
object PdfAssembler {

    /** Pixels are assumed captured at this resolution when converting to PDF points (1/72 in). */
    private const val ASSUMED_SCAN_DPI = 200.0
    private const val POINTS_PER_INCH = 72.0
    private val LATIN1: Charset = Charset.forName("ISO-8859-1")

    fun assemble(pages: List<ScanPage>): ByteArray {
        require(pages.isNotEmpty()) { "Cannot assemble a PDF with zero pages" }

        val scale = POINTS_PER_INCH / ASSUMED_SCAN_DPI
        val geometries = pages.map { page ->
            val widthPt = page.pixelWidth * scale
            val heightPt = page.pixelHeight * scale
            Geometry(widthPt, heightPt, buildContentStream(page, widthPt, heightPt, scale).toByteArray(LATIN1))
        }

        // Object numbers: 1=Catalog, 2=Pages, 3=Font, then per page i (0-based):
        // image=4+3i, page=5+3i, content=6+3i. No gaps, so the highest number is also the count.
        val fontObj = 3
        fun imageObj(i: Int) = 4 + i * 3
        fun pageObj(i: Int) = 5 + i * 3
        fun contentObj(i: Int) = 6 + i * 3
        val objectCount = 3 + pages.size * 3

        val out = ByteArrayOutputStream()
        val offsets = IntArray(objectCount + 1)

        fun write(s: String) = out.write(s.toByteArray(LATIN1))
        fun write(b: ByteArray) = out.write(b)
        fun beginObject(num: Int) {
            offsets[num] = out.size()
            write("$num 0 obj\n")
        }
        fun endObject() = write("endobj\n")

        write("%PDF-1.4\n")
        write("%\u00E2\u00E3\u00CF\u00D3\n") // conventional binary-content marker comment

        beginObject(1)
        write("<< /Type /Catalog /Pages 2 0 R >>\n")
        endObject()

        beginObject(2)
        val kids = pages.indices.joinToString(" ") { "${pageObj(it)} 0 R" }
        write("<< /Type /Pages /Kids [$kids] /Count ${pages.size} >>\n")
        endObject()

        beginObject(fontObj)
        write("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\n")
        endObject()

        pages.forEachIndexed { i, page ->
            val geo = geometries[i]

            beginObject(imageObj(i))
            write(
                "<< /Type /XObject /Subtype /Image /Width ${page.pixelWidth} " +
                    "/Height ${page.pixelHeight} /ColorSpace /DeviceRGB /BitsPerComponent 8 " +
                    "/Filter /DCTDecode /Length ${page.jpegBytes.size} >>\nstream\n"
            )
            write(page.jpegBytes)
            write("\nendstream\n")
            endObject()

            beginObject(pageObj(i))
            write(
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${fmt(geo.widthPt)} ${fmt(geo.heightPt)}] " +
                    "/Resources << /XObject << /Im0 ${imageObj(i)} 0 R >> /Font << /F0 $fontObj 0 R >> >> " +
                    "/Contents ${contentObj(i)} 0 R >>\n"
            )
            endObject()

            beginObject(contentObj(i))
            write("<< /Length ${geo.contentBytes.size} >>\nstream\n")
            write(geo.contentBytes)
            write("\nendstream\n")
            endObject()
        }

        val xrefStart = out.size()
        write("xref\n0 ${objectCount + 1}\n0000000000 65535 f \n")
        for (n in 1..objectCount) {
            write("${offsets[n].toString().padStart(10, '0')} 00000 n \n")
        }
        write("trailer\n<< /Size ${objectCount + 1} /Root 1 0 R >>\nstartxref\n$xrefStart\n%%EOF")

        return out.toByteArray()
    }

    private class Geometry(val widthPt: Double, val heightPt: Double, val contentBytes: ByteArray)

    private fun buildContentStream(page: ScanPage, widthPt: Double, heightPt: Double, scale: Double): String {
        val sb = StringBuilder()
        sb.append("q\n${fmt(widthPt)} 0 0 ${fmt(heightPt)} 0 0 cm\n/Im0 Do\nQ\n")
        if (page.words.isNotEmpty()) {
            sb.append("BT\n3 Tr\n/F0 1 Tf\n") // render mode 3 = invisible: the classic OCR text-layer trick
            for (word in page.words) {
                val fontSize = (word.height * scale).coerceAtLeast(1.0)
                val x = word.left * scale
                val y = heightPt - (word.top + word.height) * scale
                sb.append("${fmt(fontSize)} 0 0 ${fmt(fontSize)} ${fmt(x)} ${fmt(y)} Tm\n")
                sb.append("(${escapePdfText(word.text)}) Tj\n")
            }
            sb.append("ET\n")
        }
        return sb.toString()
    }

    /** PDF numbers must avoid scientific notation; keep integers bare for smaller/cleaner output. */
    private fun fmt(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString()
        else String.format(Locale.ROOT, "%.3f", v)

    /** Escapes PDF literal-string syntax and keeps output inside WinAnsiEncoding's safe range. */
    private fun escapePdfText(text: String): String {
        val sb = StringBuilder()
        for (c in text) {
            when {
                c == '(' || c == ')' || c == '\\' -> sb.append('\\').append(c)
                c.code in 32..126 -> sb.append(c)
                c.code in 160..255 -> sb.append(c) // WinAnsiEncoding maps Latin-1 supplement 1:1
                else -> sb.append('?') // outside the easy 8-bit range; searchability degrades gracefully
            }
        }
        return sb.toString()
    }
}
