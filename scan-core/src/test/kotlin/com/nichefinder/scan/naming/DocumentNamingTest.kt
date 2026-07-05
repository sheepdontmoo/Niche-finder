package com.nichefinder.scan.naming

import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DocumentNamingTest {

    private val utc = ZoneId.of("UTC")

    @Test fun `default title formats a fixed instant deterministically`() {
        val millis = ZonedDateTime.of(2026, 7, 4, 16, 32, 0, 0, utc).toInstant().toEpochMilli()
        assertEquals("Scan Jul 4, 2026 4:32 PM", DocumentNaming.defaultTitle(millis, utc))
    }

    @Test fun `default title pads single digit minutes`() {
        val millis = ZonedDateTime.of(2026, 1, 9, 9, 5, 0, 0, utc).toInstant().toEpochMilli()
        assertEquals("Scan Jan 9, 2026 9:05 AM", DocumentNaming.defaultTitle(millis, utc))
    }

    @Test fun `sanitize replaces filesystem-illegal characters`() {
        assertEquals("Invoice_2026_07_04", DocumentNaming.sanitize("Invoice/2026:07*04"))
        assertEquals("Weird_File_Name_", DocumentNaming.sanitize("Weird?File<Name>"))
    }

    @Test fun `sanitize collapses whitespace and trims`() {
        assertEquals("My Document", DocumentNaming.sanitize("  My    Document  "))
    }

    @Test fun `sanitize falls back to Untitled for blank input`() {
        assertEquals("Untitled", DocumentNaming.sanitize(""))
        assertEquals("Untitled", DocumentNaming.sanitize("   "))
        assertEquals("Untitled", DocumentNaming.sanitize("///"))
    }

    @Test fun `sanitize caps very long names`() {
        val long = "a".repeat(500)
        val result = DocumentNaming.sanitize(long)
        assertTrue(result.length <= 100)
    }

    @Test fun `unique name returns desired when no collision`() {
        assertEquals("Invoice.pdf", DocumentNaming.uniqueName("Invoice.pdf", emptyList()))
    }

    @Test fun `unique name appends counter before extension on collision`() {
        assertEquals("Invoice (2).pdf", DocumentNaming.uniqueName("Invoice.pdf", listOf("Invoice.pdf")))
    }

    @Test fun `unique name increments past multiple collisions`() {
        val existing = listOf("Invoice.pdf", "Invoice (2).pdf", "Invoice (3).pdf")
        assertEquals("Invoice (4).pdf", DocumentNaming.uniqueName("Invoice.pdf", existing))
    }

    @Test fun `unique name handles names without an extension`() {
        assertEquals("Receipt (2)", DocumentNaming.uniqueName("Receipt", listOf("Receipt")))
    }

    @Test fun `unique name collision check is case-insensitive`() {
        assertEquals("Invoice (2).pdf", DocumentNaming.uniqueName("Invoice.pdf", listOf("INVOICE.PDF")))
    }

    @Test fun `page file name is 1-based and zero-padded`() {
        assertEquals("page_0001.jpg", DocumentNaming.pageFileName(0))
        assertEquals("page_0002.jpg", DocumentNaming.pageFileName(1))
        assertEquals("page_0150.jpg", DocumentNaming.pageFileName(149))
    }

    @Test fun `page file name rejects negative index`() {
        assertFailsWith<IllegalArgumentException> { DocumentNaming.pageFileName(-1) }
    }
}
