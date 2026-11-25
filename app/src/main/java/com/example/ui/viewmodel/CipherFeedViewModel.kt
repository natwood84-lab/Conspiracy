package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChronologicalOrder
import com.example.data.model.TheoryDossier
import com.example.data.model.TheorySource
import com.example.data.model.TheoryTopic
import com.example.data.remote.GeminiWebSearchService
import com.example.data.remote.NetworkMonitor
import com.example.data.remote.WikimediaImageFinder
import com.example.data.repository.DossierRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "CipherFeedViewModel"

sealed interface WebScanState {
    data object Idle : WebScanState
    data class Scanning(val query: String) : WebScanState
    data class Success(val count: Int, val query: String) : WebScanState
    data class Error(val message: String) : WebScanState
}

sealed interface LiveStreamState {
    data object Idle : LiveStreamState
    data object Connecting : LiveStreamState
    data class Streaming(
        val text: String,
        val sources: List<TheorySource>,
        val searchQueries: List<String>
    ) : LiveStreamState
    data class Success(
        val text: String,
        val sources: List<TheorySource>,
        val searchQueries: List<String>
    ) : LiveStreamState
    data class Error(val message: String) : LiveStreamState
}

@OptIn(ExperimentalCoroutinesApi::class)
class CipherFeedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DossierRepository by lazy {
        val db = AppDatabase.getInstance(application)
        val mediaFinder = WikimediaImageFinder()
        val searchService = GeminiWebSearchService(mediaFinder)
        DossierRepository(db.dossierDao(), db.bookmarkDao(), searchService)
    }

    private val networkMonitor by lazy { NetworkMonitor(application) }

    val isOnline: StateFlow<Boolean> = networkMonitor.observeIsOnline()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _sortOrder = MutableStateFlow(ChronologicalOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<ChronologicalOrder> = _sortOrder.asStateFlow()

    private val _selectedCategory = MutableStateFlow(TheoryTopic.ALL.label)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _onlyGpsMapped = MutableStateFlow(false)
    val onlyGpsMapped: StateFlow<Boolean> = _onlyGpsMapped.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _vaultSearchQuery = MutableStateFlow("")
    val vaultSearchQuery: StateFlow<String> = _vaultSearchQuery.asStateFlow()

    private val _vaultSelectedTopic = MutableStateFlow(TheoryTopic.ALL.label)
    val vaultSelectedTopic: StateFlow<String> = _vaultSelectedTopic.asStateFlow()

    private val _vaultSortOrder = MutableStateFlow(ChronologicalOrder.NEWEST_FIRST)
    val vaultSortOrder: StateFlow<ChronologicalOrder> = _vaultSortOrder.asStateFlow()

    private val _scanState = MutableStateFlow<WebScanState>(WebScanState.Idle)
    val scanState: StateFlow<WebScanState> = _scanState.asStateFlow()

    private val _detailStreamState = MutableStateFlow<LiveStreamState>(LiveStreamState.Idle)
    val detailStreamState: StateFlow<LiveStreamState> = _detailStreamState.asStateFlow()

    private val _bookmarkFeedback = MutableStateFlow<String?>(null)
    val bookmarkFeedback: StateFlow<String?> = _bookmarkFeedback.asStateFlow()

    val weeklySpotlight: StateFlow<TheoryDossier?> = repository.observeWeeklySpotlight()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allBookmarksSorted = _vaultSortOrder
        .flatMapLatest { order -> repository.observeBookmarked(order) }

    val vaultTopicCounts: StateFlow<Map<String, Int>> = allBookmarksSorted
        .map { list -> computeTopicCounts(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val bookmarkedDossiers: StateFlow<List<TheoryDossier>> = combine(
        allBookmarksSorted,
        _vaultSelectedTopic,
        _vaultSearchQuery
    ) { list, topic, query ->
        val q = query.trim().lowercase()
        list.filter { item ->
            val matchesTopic = topic == TheoryTopic.ALL.label ||
                item.normalizedTopic.equals(topic, ignoreCase = true)
            val matchesQuery = q.isBlank() ||
                item.title.lowercase().contains(q) ||
                item.summary.lowercase().contains(q) ||
                item.normalizedTopic.lowercase().contains(q) ||
                (item.locationName?.lowercase()?.contains(q) == true)
            matchesTopic && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBookmarkedCount: StateFlow<Int> = repository
        .observeBookmarked(ChronologicalOrder.NEWEST_FIRST)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val allFeedSorted = _sortOrder
        .flatMapLatest { order -> repository.observeChronologicalFeed(order) }

    val allDossiersCount: StateFlow<Int> = allFeedSorted
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val feedTopicCounts: StateFlow<Map<String, Int>> = allFeedSorted
        .map { list -> computeTopicCounts(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val feedDossiers: StateFlow<List<TheoryDossier>> = combine(
        allFeedSorted,
        _selectedCategory,
        _onlyGpsMapped
    ) { list, category, gpsOnly ->
        list.filter { item ->
            val matchesTopic = category == TheoryTopic.ALL.label ||
                item.normalizedTopic.equals(category, ignoreCase = true)
            val matchesGps = !gpsOnly || item.hasGpsCoordinates
            matchesTopic && matchesGps
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (!repository.hasCachedDossiers() && isOnline.value) {
                runWebScan(
                    query = "Conspiracy of the week and notable web conspiracy theories across History, Technology, Space, and Politics",
                    refreshWeeklySpotlight = true
                )
            }
        }
    }

    fun observeDossier(id: String): Flow<TheoryDossier?> = repository.observeDossierById(id)

    fun updateSearchQuery(newValue: String) {
        _searchQuery.value = newValue
    }

    fun updateVaultSearchQuery(newValue: String) {
        _vaultSearchQuery.value = newValue
    }

    fun selectVaultTopic(topic: String) {
        _vaultSelectedTopic.value = topic
    }

    fun toggleVaultSortOrder() {
        _vaultSortOrder.value = if (_vaultSortOrder.value == ChronologicalOrder.NEWEST_FIRST) {
            ChronologicalOrder.OLDEST_FIRST
        } else {
            ChronologicalOrder.NEWEST_FIRST
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleOnlyGpsMapped() {
        _onlyGpsMapped.value = !_onlyGpsMapped.value
    }

    fun resetFeedFilters() {
        _selectedCategory.value = TheoryTopic.ALL.label
        _onlyGpsMapped.value = false
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == ChronologicalOrder.NEWEST_FIRST) {
            ChronologicalOrder.OLDEST_FIRST
        } else {
            ChronologicalOrder.NEWEST_FIRST
        }
    }

    fun clearBookmarkFeedback() {
        _bookmarkFeedback.value = null
    }

    fun scanForTopic(topicLabel: String) {
        val query = when (topicLabel) {
            TheoryTopic.HISTORY.label -> "Historical conspiracy theories, secret operations, and mysteries across eras"
            TheoryTopic.TECHNOLOGY.label -> "Technology, AI, cyber surveillance, and Dead Internet conspiracy theories"
            TheoryTopic.SPACE.label -> "Space, UAP, aerospace, and lunar conspiracy theories with physical locations"
            TheoryTopic.POLITICS.label -> "Political and declassified government conspiracy theories and investigations"
            TheoryTopic.SCIENCE.label -> "Science, physics, CERN, and unexplained experiment conspiracy theories"
            TheoryTopic.POP_CULTURE.label -> "Pop culture, viral internet lore, and media conspiracy theories"
            else -> "Trending conspiracy theories and mysteries on the web 2025 2026"
        }
        _selectedCategory.value = topicLabel
        runWebScan(query = query, refreshWeeklySpotlight = false)
    }

    fun updateDossierTopic(dossier: TheoryDossier, topic: TheoryTopic) {
        viewModelScope.launch {
            repository.updateDossierTopic(dossier.id, topic.label)
            _bookmarkFeedback.value = "Categorized \"${dossier.title}\" under ${topic.label}"
        }
    }

    fun runWebScan(
        query: String,
        refreshWeeklySpotlight: Boolean = false
    ) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return
        if (!isOnline.value) {
            _scanState.value = WebScanState.Error(
                "You are offline. Browse your saved dossiers in the Vault or reconnect to search the web."
            )
            return
        }

        viewModelScope.launch {
            _scanState.value = WebScanState.Scanning(cleanQuery)
            try {
                val results = repository.searchWebAndSave(
                    query = cleanQuery,
                    refreshWeeklySpotlight = refreshWeeklySpotlight
                )
                _scanState.value = WebScanState.Success(
                    count = results.size,
                    query = cleanQuery
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Web search timed out", e)
                _scanState.value = WebScanState.Error(
                    "Web search timed out after 60 seconds. Check your connection and try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Web search failed", e)
                _scanState.value = WebScanState.Error(
                    e.localizedMessage ?: "Failed to search and summarize theories."
                )
            }
        }
    }

    fun refreshSpotlight() {
        runWebScan(
            query = "Most viral conspiracy theory of the week November 2026 and trending mysteries",
            refreshWeeklySpotlight = true
        )
    }

    fun toggleBookmark(dossier: TheoryDossier) {
        viewModelScope.launch {
            val willBookmark = !dossier.isBookmarked
            repository.toggleBookmark(dossier)
            _bookmarkFeedback.value = if (willBookmark) {
                "Saved \"${dossier.title}\" for offline reading"
            } else {
                "Removed \"${dossier.title}\" from Offline Vault"
            }
        }
    }

    fun saveStreamedUpdateOffline(dossier: TheoryDossier, liveNotes: String) {
        viewModelScope.launch {
            repository.saveLiveUpdateOffline(dossier, liveNotes)
            _bookmarkFeedback.value = "Saved live update & dossier for offline reading"
        }
    }

    fun clearUnbookmarkedCache() {
        viewModelScope.launch {
            repository.clearUnbookmarkedArchive()
        }
    }

    fun resetDetailStream() {
        _detailStreamState.value = LiveStreamState.Idle
    }

    fun streamLiveWebUpdate(dossier: TheoryDossier, followUpQuestion: String? = null) {
        if (!isOnline.value) {
            _detailStreamState.value = LiveStreamState.Error(
                "Offline mode active. Saved dossier content remains readable without internet."
            )
            return
        }
        viewModelScope.launch {
            _detailStreamState.value = LiveStreamState.Connecting
            try {
                repository.streamWebUpdate(dossier, followUpQuestion).collect { update ->
                    if (update.isComplete) {
                        if (dossier.isBookmarked) {
                            repository.saveLiveUpdateOffline(dossier, update.accumulatedText)
                        }
                        _detailStreamState.value = LiveStreamState.Success(
                            text = update.accumulatedText,
                            sources = update.sources,
                            searchQueries = update.searchQueries
                        )
                    } else {
                        _detailStreamState.value = LiveStreamState.Streaming(
                            text = update.accumulatedText,
                            sources = update.sources,
                            searchQueries = update.searchQueries
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Live stream timed out", e)
                _detailStreamState.value = LiveStreamState.Error("Live web update timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Live stream failed", e)
                _detailStreamState.value = LiveStreamState.Error(
                    e.localizedMessage ?: "Failed to fetch live web updates."
                )
            }
        }
    }

    private fun computeTopicCounts(list: List<TheoryDossier>): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        counts[TheoryTopic.ALL.label] = list.size
        TheoryTopic.selectableTopics.forEach { counts[it.label] = 0 }
        list.forEach { dossier ->
            val key = dossier.normalizedTopic
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts
    }
}
