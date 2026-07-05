package com.nichefinder.docstash.scanner

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

/**
 * Wraps ML Kit's Document Scanner (`GmsDocumentScanner`): the camera viewfinder, automatic edge
 * detection, perspective correction, page cleanup and multi-page capture are entirely Google's
 * own UI flow — this app only launches it and receives back page image URIs. Deliberately no
 * [GmsDocumentScannerOptions.Builder.setPageLimit] call: per the docs, omitting it means scanning
 * is bounded only by device hardware, never by us — honoring the "no page limits, ever" rule.
 *
 * Returns a plain `() -> Unit` to start a scan; the result (or failure) is delivered to
 * [onScanned] / [onError] asynchronously once the user finishes with the scanner UI.
 */
@Composable
fun rememberDocumentScanner(
    onScanned: (List<Uri>) -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val activity = LocalContext.current as Activity

    val scanner = remember {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val uris = scanResult?.pages?.map { it.imageUri }.orEmpty()
            if (uris.isNotEmpty()) onScanned(uris) else onError("No pages were scanned")
        }
        // RESULT_CANCELED: the user backed out of the scanner UI — not an error, just a no-op.
    }

    return {
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                launcher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e -> onError(e.message ?: "Couldn't start the scanner") }
    }
}
