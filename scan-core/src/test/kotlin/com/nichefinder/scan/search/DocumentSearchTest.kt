package com.nichefinder.scan.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentSearchTest {

    private fun doc(id: String, title: String, createdAt: Long, vararg pages: String) =
        ScanDocument(id, title, createdAt, pages.toList())

    @Test fun `finds a document by a word on one page`() {
        val invoice = doc("1", "Scan Jul 1", 100L, "Total due: 42.50", "Thank you")
        val results = DocumentSearch.search(listOf(invoice), "total")
        assertEquals(1, results.size)
        assertEquals("1", results.single().document.id)
        assertEquals(listOf(0), results.single().matchingPageIndices)
    }

    @Test fun `search is case insensitive`() {
        val doc = doc("1", "Receipt", 100L, "GRAND TOTAL")
        assertEquals(1, DocumentSearch.search(listOf(doc), "grand total").size)
        assertEquals(1, DocumentSearch.search(listOf(doc), "Grand Total").size)
    }

    @Test fun `multi-word query can match terms split across different pages`() {
        val contract = doc("1", "Contract", 100L, "Party A: Acme Corp", "Signed by: Jane Doe")
        val results = DocumentSearch.search(listOf(contract), "acme jane")
        assertEquals(1, results.size)
        assertEquals(listOf(0, 1), results.single().matchingPageIndices)
    }

    @Test fun `document missing one of the query terms does not match`() {
        val doc = doc("1", "Notes", 100L, "Acme Corp meeting notes")
        assertTrue(DocumentSearch.search(listOf(doc), "acme nonexistentword").isEmpty())
    }

    @Test fun `matches on title alone even without page text`() {
        val doc = doc("1", "Tax Return 2025", 100L, "")
        val results = DocumentSearch.search(listOf(doc), "tax return")
        assertEquals(1, results.size)
    }

    @Test fun `blank query returns no results`() {
        val doc = doc("1", "Anything", 100L, "content")
        assertTrue(DocumentSearch.search(listOf(doc), "").isEmpty())
        assertTrue(DocumentSearch.search(listOf(doc), "   ").isEmpty())
    }

    @Test fun `empty document list returns no results`() {
        assertTrue(DocumentSearch.search(emptyList(), "anything").isEmpty())
    }

    @Test fun `ranks documents with more matching pages first`() {
        val weak = doc("weak", "Weak match", 100L, "budget", "unrelated")
        val strong = doc("strong", "Strong match", 100L, "budget report", "budget summary")
        val results = DocumentSearch.search(listOf(weak, strong), "budget")
        assertEquals(listOf("strong", "weak"), results.map { it.document.id })
    }

    @Test fun `ties in match strength break newest first`() {
        val older = doc("older", "A", 100L, "invoice")
        val newer = doc("newer", "B", 200L, "invoice")
        val results = DocumentSearch.search(listOf(older, newer), "invoice")
        assertEquals(listOf("newer", "older"), results.map { it.document.id })
    }

    @Test fun `snippet includes surrounding context with ellipsis`() {
        val longText = "x".repeat(60) + " needle " + "y".repeat(60)
        val doc = doc("1", "Doc", 100L, longText)
        val snippet = DocumentSearch.search(listOf(doc), "needle").single().snippet
        assertTrue(snippet.startsWith("…"))
        assertTrue(snippet.endsWith("…"))
        assertTrue("needle" in snippet)
    }

    @Test fun `snippet has no leading ellipsis when match is near the start`() {
        val doc = doc("1", "Doc", 100L, "needle right at the start of a short page")
        val snippet = DocumentSearch.search(listOf(doc), "needle").single().snippet
        assertTrue(snippet.startsWith("needle"))
    }

    @Test fun `full text joins all pages`() {
        val doc = doc("1", "Doc", 100L, "page one", "page two")
        assertEquals("page one\npage two", doc.fullText)
    }
}
