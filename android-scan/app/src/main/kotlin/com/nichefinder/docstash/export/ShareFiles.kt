package com.nichefinder.docstash.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

private const val AUTHORITY = "com.nichefinder.docstash.fileprovider"

/** Shares one or more exported files (page images or a PDF) via the system share sheet. */
fun shareFiles(context: Context, files: List<File>, mimeType: String) {
    if (files.isEmpty()) return
    val uris = files.map { FileProvider.getUriForFile(context, AUTHORITY, it) }
    val intent = if (uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uris.first())
        }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }
    }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Share"))
}
