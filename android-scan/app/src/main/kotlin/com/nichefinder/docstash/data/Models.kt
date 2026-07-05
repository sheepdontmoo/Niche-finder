package com.nichefinder.docstash.data

import kotlinx.serialization.Serializable

/** One OCR word's bounding box on its page image, in source-image pixel coordinates. */
@Serializable
data class WordRecord(
    val text: String,
    val left: Double,
    val top: Double,
    val width: Double,
    val height: Double,
)

/** One scanned page as persisted to disk: the JPEG lives alongside as [fileName]. */
@Serializable
data class PageRecord(
    val fileName: String,
    val pixelWidth: Int,
    val pixelHeight: Int,
    val text: String,
    val words: List<WordRecord> = emptyList(),
)

/** A saved document: a title, a creation time, and its pages in order. */
@Serializable
data class DocumentRecord(
    val id: String,
    val title: String,
    val createdAtEpochMillis: Long,
    val pages: List<PageRecord>,
)

/** The whole on-disk library — one JSON file, newest documents first. */
@Serializable
data class LibraryManifest(
    val documents: List<DocumentRecord> = emptyList(),
)
