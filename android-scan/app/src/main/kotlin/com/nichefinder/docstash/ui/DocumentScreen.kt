package com.nichefinder.docstash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.nichefinder.docstash.data.DocumentRecord
import java.io.File

@Composable
fun DocumentScreen(
    document: DocumentRecord,
    pageFiles: List<File>,
    proUnlocked: Boolean,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onDeletePage: (Int) -> Unit,
    onDeleteDocument: () -> Unit,
    onAddPages: () -> Unit,
    onExportImages: () -> Unit,
    onExportPdf: () -> Unit,
) {
    var renaming by remember(document.id) { mutableStateOf(false) }
    var renameText by remember(document.id) { mutableStateOf(document.title) }
    var confirmDelete by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to library") }
            Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(document.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    "${document.pages.size} page${if (document.pages.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { renameText = document.title; renaming = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Rename document")
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete document")
            }
        }
        HorizontalDivider()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(pageFiles, key = { _, file -> file.absolutePath }) { index, file ->
                PageThumbnailCard(file = file, pageNumber = index + 1, onDelete = { onDeletePage(index) })
            }
        }

        HorizontalDivider()
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onAddPages, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Add more pages")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onExportImages, modifier = Modifier.weight(1f)) {
                    Text("Export images")
                }
                Button(onClick = onExportPdf, modifier = Modifier.weight(1f)) {
                    Text(if (proUnlocked) "Export PDF" else "Export PDF \u00b7 Pro")
                }
            }
        }
    }

    if (renaming) {
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text("Rename document") },
            text = {
                OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = { onRename(renameText); renaming = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renaming = false }) { Text("Cancel") } },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this document?") },
            text = { Text("This deletes all ${document.pages.size} scanned page(s). This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteDocument() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun PageThumbnailCard(file: File, pageNumber: Int, onDelete: () -> Unit) {
    Card {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(0.75f)) {
                val bitmap = rememberPageThumbnail(file)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Page $pageNumber",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(20.dp))
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Default.Close, contentDescription = "Delete page $pageNumber", tint = Color.White)
                }
            }
            Text(
                "Page $pageNumber",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
