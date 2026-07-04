package com.nichefinder.docstash.data

import android.content.Context
import com.nichefinder.docstash.ocr.ScannedPage
import com.nichefinder.scan.naming.DocumentNaming
import com.nichefinder.scan.pdf.OcrWord
import com.nichefinder.scan.pdf.PdfAssembler
import com.nichefinder.scan.pdf.ScanPage
import com.nichefinder.scan.search.ScanDocument
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/**
 * Owns the on-disk document library: a JSON manifest (title, page order, OCR text/word boxes per
 * page) plus one JPEG file per scanned page, all under the app's private storage. Nothing ever
 * leaves the device unless the user explicitly shares an export — no account, no server, no sync,
 * directly answering CamScanner users' biggest complaint about forced cloud upload.
 *
 * Blocking file I/O throughout by design: every call here is dispatched from the ViewModel on
 * `Dispatchers.IO`, so there's no value in this class managing its own threading. Every public
 * method that touches the manifest is fully serialized on [lock] — read-modify-write across two
 * separate synchronized calls (the original shape of this class) can interleave when two
 * mutations land on `Dispatchers.IO`'s thread pool at once, silently dropping whichever wrote
 * second.
 */
class DocumentStore(private val context: Context) {

    private val lock = Any()
    private val json = Json { ignoreUnknownKeys = true }
    private val documentsDir: File get() = File(context.filesDir, "documents").apply { mkdirs() }
    private val manifestFile: File get() = File(documentsDir, "manifest.json")
    private val exportsDir: File get() = File(context.cacheDir, "exports").apply { mkdirs() }

    private var cached: LibraryManifest? = null

    /** Must only be called while holding [lock]. */
    private fun load(): LibraryManifest {
        cached?.let { return it }
        val manifest = if (manifestFile.exists()) {
            val raw = manifestFile.readText()
            runCatching { json.decodeFromString(LibraryManifest.serializer(), raw) }
                .getOrElse {
                    // A library the parser can't read must never be silently replaced with an
                    // empty one -- keep the raw bytes so a corrupt manifest is at least
                    // recoverable by hand, instead of quietly discarding every saved document.
                    runCatching {
                        File(documentsDir, "manifest-corrupt-${System.currentTimeMillis()}.json").writeText(raw)
                    }
                    LibraryManifest()
                }
        } else {
            LibraryManifest()
        }
        cached = manifest
        return manifest
    }

    /** Must only be called while holding [lock]. Writes via a temp file + rename to avoid a half-written manifest. */
    private fun save(manifest: LibraryManifest) {
        cached = manifest
        val tmp = File(documentsDir, "manifest.json.tmp")
        tmp.writeText(json.encodeToString(LibraryManifest.serializer(), manifest))
        if (!tmp.renameTo(manifestFile)) {
            // Some filesystems refuse an atomic rename over an existing file; fall back to a
            // plain copy rather than leaving the manifest unwritten.
            manifestFile.writeText(tmp.readText())
            tmp.delete()
        }
    }

    /** All saved documents, newest first. */
    fun all(): List<DocumentRecord> = synchronized(lock) {
        load().documents.sortedByDescending { it.createdAtEpochMillis }
    }

    fun get(documentId: String): DocumentRecord? = synchronized(lock) {
        load().documents.find { it.id == documentId }
    }

    fun asSearchDocuments(): List<ScanDocument> = synchronized(lock) {
        load().documents.map { doc -> ScanDocument(doc.id, doc.title, doc.createdAtEpochMillis, doc.pages.map { it.text }) }
    }

    fun createDocument(pages: List<ScannedPage>): DocumentRecord {
        require(pages.isNotEmpty()) { "Cannot create a document with zero scanned pages" }
        return synchronized(lock) {
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val folder = File(documentsDir, id).apply { mkdirs() }
            val pageRecords = pages.mapIndexed { index, page -> writePage(folder, index, page) }
            val record = DocumentRecord(
                id = id,
                title = DocumentNaming.defaultTitle(now),
                createdAtEpochMillis = now,
                pages = pageRecords,
            )
            save(LibraryManifest(load().documents + record))
            record
        }
    }

    fun appendPages(documentId: String, morePages: List<ScannedPage>): DocumentRecord {
        require(morePages.isNotEmpty()) { "No pages to append" }
        return synchronized(lock) {
            val manifest = load()
            val existing = manifest.documents.find { it.id == documentId } ?: error("Document $documentId not found")
            val folder = File(documentsDir, documentId).apply { mkdirs() }
            // Page count alone isn't a safe "next index": deleting a page never renumbers the
            // survivors, so after a delete, count-based naming can collide with and overwrite an
            // existing page's file. Continue after the highest index actually still in use.
            val startIndex = nextPageIndex(existing.pages)
            val newRecords = morePages.mapIndexed { i, page -> writePage(folder, startIndex + i, page) }
            val updated = existing.copy(pages = existing.pages + newRecords)
            save(LibraryManifest(manifest.documents.map { if (it.id == documentId) updated else it }))
            updated
        }
    }

    private fun nextPageIndex(existingPages: List<PageRecord>): Int {
        val usedIndices = existingPages.mapNotNull { pageIndexOf(it.fileName) }
        return (usedIndices.maxOrNull() ?: -1) + 1
    }

    private fun pageIndexOf(fileName: String): Int? =
        PAGE_FILE_NAME_PATTERN.find(fileName)?.groupValues?.get(1)?.toIntOrNull()?.minus(1)

    private fun writePage(folder: File, pageIndex: Int, page: ScannedPage): PageRecord {
        val fileName = DocumentNaming.pageFileName(pageIndex)
        File(folder, fileName).writeBytes(page.jpegBytes)
        return PageRecord(
            fileName = fileName,
            pixelWidth = page.pixelWidth,
            pixelHeight = page.pixelHeight,
            text = page.text,
            words = page.words.map { WordRecord(it.text, it.left, it.top, it.width, it.height) },
        )
    }

    fun rename(documentId: String, newTitle: String): Unit = synchronized(lock) {
        val manifest = load()
        val sanitized = DocumentNaming.sanitize(newTitle)
        save(LibraryManifest(manifest.documents.map { if (it.id == documentId) it.copy(title = sanitized) else it }))
    }

    fun deletePage(documentId: String, pageIndex: Int): Unit = synchronized(lock) {
        val manifest = load()
        val existing = manifest.documents.find { it.id == documentId } ?: return@synchronized
        val removed = existing.pages.getOrNull(pageIndex) ?: return@synchronized
        File(File(documentsDir, documentId), removed.fileName).delete()
        val updated = existing.copy(pages = existing.pages.filterIndexed { i, _ -> i != pageIndex })
        if (updated.pages.isEmpty()) {
            deleteLocked(documentId, manifest)
        } else {
            save(LibraryManifest(manifest.documents.map { if (it.id == documentId) updated else it }))
        }
    }

    fun delete(documentId: String): Unit = synchronized(lock) {
        deleteLocked(documentId, load())
    }

    /** Must only be called while holding [lock]. */
    private fun deleteLocked(documentId: String, manifest: LibraryManifest) {
        File(documentsDir, documentId).deleteRecursively()
        save(LibraryManifest(manifest.documents.filterNot { it.id == documentId }))
    }

    /** The saved JPEG files for a document's pages, in order — used to render previews/thumbnails. */
    fun pageFiles(documentId: String): List<File> = synchronized(lock) {
        val doc = load().documents.find { it.id == documentId } ?: return@synchronized emptyList()
        val folder = File(documentsDir, documentId)
        doc.pages.map { File(folder, it.fileName) }
    }

    /**
     * Copies a document's pages out to the share-ready export area under human-readable,
     * collision-safe names (e.g. "Tax Receipt - page 2.jpg") — the free-tier "export as
     * individual images" path.
     */
    fun exportImages(documentId: String): List<File> {
        val doc = get(documentId) ?: return emptyList()
        val folder = File(documentsDir, documentId)
        val titleBase = DocumentNaming.sanitize(doc.title)
        val namesThisBatch = mutableListOf<String>()
        return doc.pages.mapIndexed { index, page ->
            val desired = if (doc.pages.size == 1) "$titleBase.jpg" else "$titleBase - page ${index + 1}.jpg"
            val existing = exportsDir.list()?.toList().orEmpty() + namesThisBatch
            val fileName = DocumentNaming.uniqueName(desired, existing)
            namesThisBatch += fileName
            File(folder, page.fileName).copyTo(File(exportsDir, fileName), overwrite = true)
        }
    }

    /** Assembles a searchable PDF for [documentId] into the share-ready export area — the Pro export path. */
    fun exportPdf(documentId: String): File {
        val doc = get(documentId) ?: error("Document $documentId not found")
        val folder = File(documentsDir, documentId)
        val scanPages = doc.pages.map { page ->
            ScanPage(
                jpegBytes = File(folder, page.fileName).readBytes(),
                pixelWidth = page.pixelWidth,
                pixelHeight = page.pixelHeight,
                words = page.words.map { OcrWord(it.text, it.left, it.top, it.width, it.height) },
            )
        }
        val pdfBytes = PdfAssembler.assemble(scanPages)
        val outFile = File(exportsDir, "${DocumentNaming.sanitize(doc.title)}.pdf")
        outFile.writeBytes(pdfBytes)
        return outFile
    }

    private companion object {
        val PAGE_FILE_NAME_PATTERN = Regex("""^page_(\d{4,})\.jpg$""")
    }
}
