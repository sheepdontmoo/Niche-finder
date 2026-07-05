package com.nichefinder.scan.pdf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun fakeJpeg(vararg extra: Int): ByteArray =
    byteArrayOf(0xFF.toByte(), 0xD8.toByte(), *extra.map { it.toByte() }.toByteArray(), 0xFF.toByte(), 0xD9.toByte())

private fun page(width: Int = 850, height: Int = 1100, words: List<OcrWord> = emptyList()) =
    ScanPage(jpegBytes = fakeJpeg(1, 2, 3), pixelWidth = width, pixelHeight = height, words = words)

/** Reads the byte offset recorded in the xref table for object [objNum], from a Latin-1 decode of the PDF. */
private fun xrefOffsetFor(pdfText: String, objNum: Int): Int {
    val xrefPos = pdfText.lastIndexOf("\nxref\n")
    val body = pdfText.substring(xrefPos + "\nxref\n".length)
    val lines = body.lines()
    val entryLine = lines[1 + objNum] // lines[0] = "0 <count>" header, lines[1] = object 0's entry
    return entryLine.substring(0, 10).toInt()
}

class PdfAssemblerTest {

    @Test fun `single page pdf has valid header and eof marker`() {
        val pdf = PdfAssembler.assemble(listOf(page()))
        val text = String(pdf, Charsets.ISO_8859_1)
        assertTrue(text.startsWith("%PDF-1.4"))
        assertTrue(text.trimEnd().endsWith("%%EOF"))
    }

    @Test fun `page count in catalog matches input pages`() {
        val pdf = PdfAssembler.assemble(listOf(page(), page(), page()))
        val text = String(pdf, Charsets.ISO_8859_1)
        assertTrue("/Count 3" in text)
    }

    @Test fun `three pages produce three Type Page objects`() {
        val pdf = PdfAssembler.assemble(listOf(page(), page(), page()))
        val text = String(pdf, Charsets.ISO_8859_1)
        val pageObjectCount = Regex("/Type /Page[^s]").findAll(text).count()
        assertEquals(3, pageObjectCount)
    }

    @Test fun `jpeg bytes are embedded verbatim as DCTDecode stream`() {
        val jpeg = fakeJpeg(0xAB, 0xCD, 0x00, 0x7F)
        val pdf = PdfAssembler.assemble(listOf(ScanPage(jpeg, 100, 200)))
        val text = String(pdf, Charsets.ISO_8859_1)
        assertTrue("/Filter /DCTDecode" in text)
        assertTrue(String(jpeg, Charsets.ISO_8859_1) in text, "raw JPEG bytes should appear unmodified in the PDF")
    }

    @Test fun `ocr text appears in content stream as literal Tj operator`() {
        val words = listOf(OcrWord("Invoice", 10.0, 20.0, 80.0, 14.0), OcrWord("Total", 10.0, 40.0, 60.0, 14.0))
        val pdf = PdfAssembler.assemble(listOf(page(words = words)))
        val text = String(pdf, Charsets.ISO_8859_1)
        assertTrue("(Invoice) Tj" in text)
        assertTrue("(Total) Tj" in text)
    }

    @Test fun `invisible render mode is set when a page has ocr words`() {
        val pdf = PdfAssembler.assemble(listOf(page(words = listOf(OcrWord("hi", 0.0, 0.0, 10.0, 10.0)))))
        val text = String(pdf, Charsets.ISO_8859_1)
        assertTrue("3 Tr" in text, "expected invisible text render mode for the OCR layer")
    }

    @Test fun `page with no ocr words has no text operators`() {
        val pdf = PdfAssembler.assemble(listOf(page(words = emptyList())))
        val text = String(pdf, Charsets.ISO_8859_1)
        assertFalse("BT" in text)
        assertFalse("Tj" in text)
    }

    @Test fun `special characters in ocr text are escaped for pdf syntax`() {
        val words = listOf(OcrWord("Say (hi) \\now", 0.0, 0.0, 50.0, 10.0))
        val pdf = PdfAssembler.assemble(listOf(page(words = words)))
        val text = String(pdf, Charsets.ISO_8859_1)
        assertTrue("Say \\(hi\\) \\\\now" in text)
    }

    @Test fun `xref offsets point to correct object headers across multiple pages`() {
        val words = listOf(OcrWord("hello", 5.0, 5.0, 40.0, 12.0))
        val pdf = PdfAssembler.assemble(listOf(page(words = words), page(), page(words = words)))
        val text = String(pdf, Charsets.ISO_8859_1)

        val size = Regex("/Size (\\d+)").find(text)!!.groupValues[1].toInt()
        for (n in 1 until size) {
            val offset = xrefOffsetFor(text, n)
            val expectedHeader = "$n 0 obj"
            assertEquals(
                expectedHeader,
                text.substring(offset, offset + expectedHeader.length),
                "object $n's xref offset should point exactly at its own header",
            )
        }
    }

    @Test fun `assembling zero pages throws`() {
        assertFailsWith<IllegalArgumentException> { PdfAssembler.assemble(emptyList()) }
    }

    @Test fun `scan page rejects empty jpeg bytes`() {
        assertFailsWith<IllegalArgumentException> { ScanPage(ByteArray(0), 100, 100) }
    }

    @Test fun `scan page rejects non positive dimensions`() {
        assertFailsWith<IllegalArgumentException> { ScanPage(fakeJpeg(), 0, 100) }
        assertFailsWith<IllegalArgumentException> { ScanPage(fakeJpeg(), 100, -5) }
    }

    @Test fun `ocr word rejects empty text`() {
        assertFailsWith<IllegalArgumentException> { OcrWord("", 0.0, 0.0, 10.0, 10.0) }
    }

    @Test fun `ocr word rejects non positive size`() {
        assertFailsWith<IllegalArgumentException> { OcrWord("x", 0.0, 0.0, 0.0, 10.0) }
        assertFailsWith<IllegalArgumentException> { OcrWord("x", 0.0, 0.0, 10.0, -1.0) }
    }
}
