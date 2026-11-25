package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TheoryDossier
import com.example.data.model.TheorySource
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "dossiers")
data class TheoryDossierEntity(
    @PrimaryKey val id: String,
    val caseCode: String,
    val title: String,
    val originYear: Int,
    val chronologicalDateLabel: String,
    val sortTimestamp: Long,
    val category: String,
    val tractionStatus: String,
    val summary: String,
    val originStory: String,
    val recentUpdates: String,
    val keyClaimsJson: String,
    val factualContext: String,
    val dangersAndLawsJson: String,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val imageUrl: String?,
    val sourcesJson: String,
    val searchQueriesJson: String,
    val isWeeklySpotlight: Boolean,
    val weeklySpotlightNote: String?,
    val isBookmarked: Boolean,
    val fetchedAtMillis: Long
) {
    fun toDomain(): TheoryDossier {
        return TheoryDossier(
            id = id,
            caseCode = caseCode,
            title = title,
            originYear = originYear,
            chronologicalDateLabel = chronologicalDateLabel,
            sortTimestamp = sortTimestamp,
            category = category,
            tractionStatus = tractionStatus,
            summary = summary,
            originStory = originStory,
            recentUpdates = recentUpdates,
            keyClaims = decodeStringList(keyClaimsJson),
            factualContext = factualContext,
            dangersAndLaws = decodeStringList(dangersAndLawsJson),
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            imageUrl = imageUrl,
            sources = decodeSources(sourcesJson),
            searchQueries = decodeStringList(searchQueriesJson),
            isWeeklySpotlight = isWeeklySpotlight,
            weeklySpotlightNote = weeklySpotlightNote,
            isBookmarked = isBookmarked,
            fetchedAtMillis = fetchedAtMillis
        )
    }

    companion object {
        fun fromDomain(dossier: TheoryDossier): TheoryDossierEntity {
            return TheoryDossierEntity(
                id = dossier.id,
                caseCode = dossier.caseCode,
                title = dossier.title,
                originYear = dossier.originYear,
                chronologicalDateLabel = dossier.chronologicalDateLabel,
                sortTimestamp = dossier.sortTimestamp,
                category = dossier.category,
                tractionStatus = dossier.tractionStatus,
                summary = dossier.summary,
                originStory = dossier.originStory,
                recentUpdates = dossier.recentUpdates,
                keyClaimsJson = encodeStringList(dossier.keyClaims),
                factualContext = dossier.factualContext,
                dangersAndLawsJson = encodeStringList(dossier.dangersAndLaws),
                latitude = dossier.latitude,
                longitude = dossier.longitude,
                locationName = dossier.locationName,
                imageUrl = dossier.imageUrl,
                sourcesJson = encodeSources(dossier.sources),
                searchQueriesJson = encodeStringList(dossier.searchQueries),
                isWeeklySpotlight = dossier.isWeeklySpotlight,
                weeklySpotlightNote = dossier.weeklySpotlightNote,
                isBookmarked = dossier.isBookmarked,
                fetchedAtMillis = dossier.fetchedAtMillis
            )
        }

        private fun encodeStringList(list: List<String>): String {
            val array = JSONArray()
            list.forEach { array.put(it) }
            return array.toString()
        }

        private fun decodeStringList(json: String): List<String> {
            return runCatching {
                val array = JSONArray(json)
                (0 until array.length()).mapNotNull { idx ->
                    array.optString(idx).takeIf { it.isNotBlank() }
                }
            }.getOrDefault(emptyList())
        }

        private fun encodeSources(sources: List<TheorySource>): String {
            val array = JSONArray()
            sources.forEach { source ->
                val obj = JSONObject()
                obj.put("title", source.title)
                obj.put("url", source.url)
                obj.put("domain", source.domain)
                array.put(obj)
            }
            return array.toString()
        }

        private fun decodeSources(json: String): List<TheorySource> {
            return runCatching {
                val array = JSONArray(json)
                (0 until array.length()).mapNotNull { idx ->
                    val obj = array.optJSONObject(idx) ?: return@mapNotNull null
                    val url = obj.optString("url")
                    if (url.isBlank()) return@mapNotNull null
                    TheorySource(
                        title = obj.optString("title", url),
                        url = url,
                        domain = obj.optString("domain", "")
                    )
                }
            }.getOrDefault(emptyList())
        }
    }
}
