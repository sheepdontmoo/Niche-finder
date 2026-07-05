package com.nichefinder.scan.naming

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Default titles, filesystem-safe names, and collision-safe uniquing for saved documents/pages. */
object DocumentNaming {

    private val TITLE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.US)

    /** Characters unsafe on at least one of Android's filesystem, Windows, or macOS. */
    private val ILLEGAL_CHARS = Regex("[/\\\\:*?\"<>|\\p{Cntrl}]")
    private val WHITESPACE_RUN = Regex("\\s+")
    private const val MAX_NAME_LENGTH = 100

    /** e.g. "Scan Jul 4, 2026 4:32 PM" — the default title given to a freshly captured document. */
    fun defaultTitle(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String {
        val time = Instant.ofEpochMilli(epochMillis).atZone(zone)
        return "Scan ${TITLE_FORMAT.format(time)}"
    }

    /** Strips characters that are unsafe across Android/Windows/macOS filesystems. */
    fun sanitize(raw: String): String {
        val cleaned = raw.replace(ILLEGAL_CHARS, "_").replace(WHITESPACE_RUN, " ").trim()
        // A name that was nothing but illegal characters (e.g. "///") sanitizes to a string of
        // underscores with no real content — that's as meaningless as an empty name.
        val safe = if (cleaned.isBlank() || cleaned.all { it == '_' }) "Untitled" else cleaned
        return if (safe.length > MAX_NAME_LENGTH) safe.take(MAX_NAME_LENGTH).trim() else safe
    }

    /**
     * Appends " (2)", " (3)"… before the extension until [desired] no longer collides with
     * [existing] (case-insensitive, matching how Android/Windows/macOS filesystems compare names).
     */
    fun uniqueName(desired: String, existing: Collection<String>): String {
        val existingLower = existing.mapTo(HashSet()) { it.lowercase(Locale.ROOT) }
        if (desired.lowercase(Locale.ROOT) !in existingLower) return desired

        val dot = desired.lastIndexOf('.')
        val base = if (dot > 0) desired.substring(0, dot) else desired
        val ext = if (dot > 0) desired.substring(dot) else ""

        var n = 2
        while (true) {
            val candidate = "$base ($n)$ext"
            if (candidate.lowercase(Locale.ROOT) !in existingLower) return candidate
            n++
        }
    }

    /** Zero-padded, 1-based per-page image filename — sorts correctly up to 9999 pages. */
    fun pageFileName(pageIndex: Int): String {
        require(pageIndex >= 0) { "Page index must be >= 0, got $pageIndex" }
        return "page_%04d.jpg".format(Locale.ROOT, pageIndex + 1)
    }
}
