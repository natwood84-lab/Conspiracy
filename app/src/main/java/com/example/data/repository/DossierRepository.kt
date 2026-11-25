package com.example.data.repository

import com.example.data.local.BookmarkDao
import com.example.data.local.BookmarkedDossierEntity
import com.example.data.local.DossierDao
import com.example.data.local.TheoryDossierEntity
import com.example.data.model.ChronologicalOrder
import com.example.data.model.TheoryDossier
import com.example.data.remote.GeminiWebSearchService
import com.example.data.remote.GroundingStreamUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class DossierRepository(
    private val dao: DossierDao,
    private val bookmarkDao: BookmarkDao,
    private val searchService: GeminiWebSearchService
) {
    fun observeChronologicalFeed(order: ChronologicalOrder): Flow<List<TheoryDossier>> {
        val sourceFlow = when (order) {
            ChronologicalOrder.NEWEST_FIRST -> dao.observeAllChronologicalDesc()
            ChronologicalOrder.OLDEST_FIRST -> dao.observeAllChronologicalAsc()
        }
        return combine(
            sourceFlow,
            bookmarkDao.observeBookmarkedIds()
        ) { entities, bookmarkedIds ->
            val idSet = bookmarkedIds.toSet()
            entities.map { entity ->
                entity.toDomain().copy(isBookmarked = entity.id in idSet)
            }
        }
    }

    fun observeBookmarked(order: ChronologicalOrder = ChronologicalOrder.NEWEST_FIRST): Flow<List<TheoryDossier>> {
        val flow = when (order) {
            ChronologicalOrder.NEWEST_FIRST -> bookmarkDao.observeAllBookmarkedDesc()
            ChronologicalOrder.OLDEST_FIRST -> bookmarkDao.observeAllBookmarkedAsc()
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    fun observeWeeklySpotlight(): Flow<TheoryDossier?> {
        return combine(
            dao.observeWeeklySpotlight(),
            bookmarkDao.observeBookmarkedIds()
        ) { entity, bookmarkedIds ->
            entity?.toDomain()?.copy(isBookmarked = entity.id in bookmarkedIds.toSet())
        }
    }

    fun observeDossierById(id: String): Flow<TheoryDossier?> {
        return combine(
            bookmarkDao.observeById(id),
            dao.observeById(id)
        ) { bookmarkedEntity, cachedEntity ->
            when {
                bookmarkedEntity != null -> bookmarkedEntity.toDomain()
                cachedEntity != null -> cachedEntity.toDomain()
                else -> null
            }
        }
    }

    suspend fun hasCachedDossiers(): Boolean =
        dao.getCount() > 0 || bookmarkDao.getAllBookmarkedIds().isNotEmpty()

    suspend fun searchWebAndSave(
        query: String,
        refreshWeeklySpotlight: Boolean
    ): List<TheoryDossier> {
        val fresh = searchService.searchAndSummarize(
            query = query,
            includeWeeklySpotlight = refreshWeeklySpotlight
        )
        val bookmarkedIds = bookmarkDao.getAllBookmarkedIds().toSet()

        if (refreshWeeklySpotlight && fresh.any { it.isWeeklySpotlight }) {
            dao.clearWeeklySpotlightFlag()
        }

        val merged = fresh.map { dossier ->
            if (dossier.id in bookmarkedIds) {
                dossier.copy(isBookmarked = true)
            } else {
                dossier
            }
        }
        dao.upsertAll(merged.map { TheoryDossierEntity.fromDomain(it) })
        return merged
    }

    suspend fun toggleBookmark(dossier: TheoryDossier) {
        if (dossier.isBookmarked) {
            bookmarkDao.deleteBookmarkById(dossier.id)
            dao.setBookmarked(dossier.id, false)
        } else {
            bookmarkDao.insertBookmark(BookmarkedDossierEntity.fromDomain(dossier))
            dao.setBookmarked(dossier.id, true)
        }
    }

    suspend fun saveLiveUpdateOffline(dossier: TheoryDossier, liveNotes: String) {
        val cleanNotes = liveNotes.trim()
        if (cleanNotes.isBlank()) return
        val updated = dossier.copy(
            isBookmarked = true,
            savedLiveNotes = cleanNotes,
            bookmarkedAtMillis = dossier.bookmarkedAtMillis ?: System.currentTimeMillis()
        )
        bookmarkDao.insertBookmark(BookmarkedDossierEntity.fromDomain(updated))
        dao.setBookmarked(dossier.id, true)
    }

    suspend fun updateDossierTopic(id: String, topicLabel: String) {
        dao.updateCategory(id, topicLabel)
        bookmarkDao.updateCategory(id, topicLabel)
    }

    suspend fun clearUnbookmarkedArchive() {
        dao.deleteUnbookmarked()
    }

    fun streamWebUpdate(
        dossier: TheoryDossier,
        followUpQuestion: String?
    ): Flow<GroundingStreamUpdate> {
        return searchService.streamTheoryUpdate(dossier, followUpQuestion)
    }
}
