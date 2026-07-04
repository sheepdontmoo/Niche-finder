package com.nichefinder.docstash

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nichefinder.docstash.export.shareFiles
import com.nichefinder.docstash.scanner.rememberDocumentScanner
import com.nichefinder.docstash.ui.DocumentScreen
import com.nichefinder.docstash.ui.LibraryScreen
import com.nichefinder.docstash.ui.SettingsScreen
import com.nichefinder.docstash.ui.theme.DocStashTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: DocStashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocStashTheme {
                DocStashApp(vm = vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Play's own recommendation: re-check purchases on every foreground, not just at launch —
        // the safety net for purchases completed while the app wasn't running or was offline.
        vm.refreshPurchases()
    }
}

private enum class Tab(val label: String) { Library("Library"), Settings("Settings") }

@Composable
private fun DocStashApp(vm: DocStashViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var tab by remember { mutableStateOf(Tab.Library) }
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()

    val startScan = rememberDocumentScanner(
        onScanned = { uris -> vm.onPagesScanned(uris) },
        onError = { message -> vm.onScanError(message) },
    )

    val selectedDocument = state.documents.find { it.id == state.selectedDocumentId }

    if (selectedDocument != null) {
        val pageFiles = remember(selectedDocument.id, selectedDocument.pages.size) {
            vm.pageFiles(selectedDocument.id)
        }
        DocumentScreen(
            document = selectedDocument,
            pageFiles = pageFiles,
            proUnlocked = state.proUnlocked,
            onBack = vm::closeDocument,
            onRename = { title -> vm.renameDocument(selectedDocument.id, title) },
            onDeletePage = { index -> vm.deletePage(selectedDocument.id, index) },
            onDeleteDocument = { vm.deleteDocument(selectedDocument.id) },
            onAddPages = { vm.beginAddPages(selectedDocument.id); startScan() },
            onExportImages = {
                scope.launch {
                    val files = vm.exportImages(selectedDocument.id)
                    shareFiles(context, files, "image/jpeg")
                }
            },
            onExportPdf = {
                if (state.proUnlocked) {
                    scope.launch {
                        val file = vm.exportPdf(selectedDocument.id)
                        shareFiles(context, listOf(file), "application/pdf")
                    }
                } else {
                    vm.closeDocument()
                    tab = Tab.Settings
                }
            },
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        label = { Text(t.label) },
                        icon = {
                            Icon(
                                when (t) {
                                    Tab.Library -> Icons.AutoMirrored.Filled.List
                                    Tab.Settings -> Icons.Default.Settings
                                },
                                contentDescription = t.label,
                            )
                        },
                    )
                }
            }
        },
    ) { padding ->
        val mod = Modifier.padding(padding)
        when (tab) {
            Tab.Library -> LibraryScreen(
                state = state,
                modifier = mod,
                onSearchQueryChange = vm::setSearchQuery,
                onOpenDocument = vm::openDocument,
                onScan = { vm.beginNewScan(); startScan() },
            )
            Tab.Settings -> SettingsScreen(
                proUnlocked = state.proUnlocked,
                priceLabel = state.proPriceLabel,
                modifier = mod,
                onUnlockClick = { vm.purchasePro(activity) },
            )
        }
    }

    state.message?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::dismissMessage,
            confirmButton = { TextButton(onClick = vm::dismissMessage) { Text("OK") } },
            title = { Text("DocStash") },
            text = { Text(msg) },
        )
    }
}
