package com.nichefinder.scan.search

import java.util.Locale

/** A saved document as far as search is concerned: its OCR text layer, one entry per page. */
data class ScanDocument(
    val id: String,
    val title: String,
    val createdAtEpochMillis: Long,
    val pageText: List<String>,
) {
    val fullText: String get() = pageText.joinToString("\n")
}

/** A document that matched a search, with which pages matched and a preview snippet. */
data class SearchResult(
    val document: ScanDocument,
    val matchingPageIndices: List<Int>,
    val snippet: String,
)

/**
 * Search across saved documents' OCR text — the "search-by-OCR-text" library feature. All query
 * terms must appear somewhere in the document (title or any page) for it to match, matching the
 * loose multi-term search behavior of most desktop OCR tools; the matched *pages* highlighted in
 * the UI are whichever pages contain at least one of the terms, since a multi-word query can
 * legitimately span pages (e.g. one term on the invoice header page, another lower down).
 */
object DocumentSearch {

    private val WHITESPACE = Regex("\\s+")
    private const val SNIPPET_RADIUS = 40

    fun search(documents: List<ScanDocument>, query: String): List<SearchResult> {
        val terms = query.trim().lowercase(Locale.ROOT).split(WHITESPACE).filter { it.isNotBlank() }
        if (terms.isEmpty()) return emptyList()

        return documents.mapNotNull { doc ->
            val titleLower = doc.title.lowercase(Locale.ROOT)
            val pagesLower = doc.pageText.map { it.lowercase(Locale.ROOT) }
            val haystack = titleLower + "\n" + pagesLower.joinToString("\n")

            val matchesAllTerms = terms.all { it in haystack }
            if (!matchesAllTerms) return@mapNotNull null

            val matchingPages = pagesLower.indices.filter { i -> terms.any { term -> term in pagesLower[i] } }
            val snippetSource = matchingPages.firstOrNull()?.let { doc.pageText[it] } ?: doc.title
            SearchResult(
                document = doc,
                matchingPageIndices = matchingPages,
                snippet = buildSnippet(snippetSource, terms.first()),
            )
        }.sortedWith(
            compareByDescending<SearchResult> { it.matchingPageIndices.size }
                .thenByDescending { it.document.createdAtEpochMillis },
        )
    }

    private fun buildSnippet(text: String, term: String): String {
        val idx = text.lowercase(Locale.ROOT).indexOf(term)
        if (idx < 0) return text.take(SNIPPET_RADIUS * 2).trim()
        val start = (idx - SNIPPET_RADIUS).coerceAtLeast(0)
        val end = (idx + term.length + SNIPPET_RADIUS).coerceAtMost(text.length)
        val prefix = if (start > 0) "…" else ""
        val suffix = if (end < text.length) "…" else ""
        return "$prefix${text.substring(start, end).trim()}$suffix"
    }
}
