package com.nichefinder.docstash

import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nichefinder.docstash.billing.BillingManager
import com.nichefinder.docstash.data.DocumentRecord
import com.nichefinder.docstash.data.DocumentStore
import com.nichefinder.docstash.ocr.OcrProcessor
import com.nichefinder.scan.search.DocumentSearch
import com.nichefinder.scan.search.SearchResult
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val documents: List<DocumentRecord> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<SearchResult>? = null,
    val selectedDocumentId: String? = null,
    val proUnlocked: Boolean = false,
    val proPriceLabel: String? = null,
    val busy: Boolean = false,
    val message: String? = null,
)

class DocStashViewModel(app: Application) : AndroidViewModel(app) {

    private val store = DocumentStore(app)
    private val ocr = OcrProcessor()
    val billing = BillingManager(app)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Set just before launching the scanner: null = new document, else append to this one. */
    private var pendingScanTarget: String? = null

    init {
        _state.update { it.copy(documents = store.all()) }
        viewModelScope.launch { billing.proUnlocked.collect { unlocked -> _state.update { it.copy(proUnlocked = unlocked) } } }
        viewModelScope.launch { billing.priceLabel.collect { label -> _state.update { it.copy(proPriceLabel = label) } } }
    }

    fun beginNewScan() { pendingScanTarget = null }

    fun beginAddPages(documentId: String) { pendingScanTarget = documentId }

    fun onPagesScanned(uris: List<Uri>) {
        val target = pendingScanTarget
        pendingScanTarget = null
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val pages = withContext(Dispatchers.IO) { uris.map { uri -> ocr.recognize(context, uri) } }
                val updated = withContext(Dispatchers.IO) {
                    if (target == null) store.createDocument(pages) else store.appendPages(target, pages)
                }
                _state.update { it.copy(documents = store.all(), busy = false, selectedDocumentId = updated.id) }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, message = "Scan failed: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun onScanError(message: String) {
        _state.update { it.copy(busy = false, message = message) }
    }

    fun openDocument(id: String) = _state.update { it.copy(selectedDocumentId = id) }

    fun closeDocument() = _state.update { it.copy(selectedDocumentId = null) }

    fun renameDocument(id: String, title: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { store.rename(id, title) }
            _state.update { it.copy(documents = store.all()) }
        }
    }

    fun deletePage(documentId: String, pageIndex: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { store.deletePage(documentId, pageIndex) }
            val stillExists = store.get(documentId) != null
            _state.update {
                it.copy(documents = store.all(), selectedDocumentId = if (stillExists) documentId else null)
            }
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { store.delete(id) }
            _state.update { it.copy(documents = store.all(), selectedDocumentId = null) }
        }
    }

    fun setSearchQuery(query: String) {
        val results = if (query.isBlank()) null else DocumentSearch.search(store.asSearchDocuments(), query)
        _state.update { it.copy(searchQuery = query, searchResults = results) }
    }

    fun pageFiles(documentId: String): List<File> = store.pageFiles(documentId)

    suspend fun exportImages(documentId: String): List<File> =
        withContext(Dispatchers.IO) { store.exportImages(documentId) }

    suspend fun exportPdf(documentId: String): File =
        withContext(Dispatchers.IO) { store.exportPdf(documentId) }

    fun purchasePro(activity: Activity) = billing.launchPurchase(activity)

    fun refreshPurchases() = viewModelScope.launch { billing.refreshPurchases() }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    override fun onCleared() {
        billing.close()
    }
}
