package com.example.data.remote

import android.net.Uri
import android.util.Log
import com.example.data.model.TheoryDossier
import com.example.data.model.TheorySource
import com.example.data.model.TheoryTopic
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "GeminiWebSearchService"

data class GroundingStreamUpdate(
    val accumulatedText: String,
    val isComplete: Boolean,
    val sources: List<TheorySource>,
    val searchQueries: List<String>
)

class GeminiWebSearchService(
    private val mediaFinder: WikimediaImageFinder
) {
    private val searchModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.googleSearch())
        )
    }

    private val dossierListSchema by lazy {
        val dossierItemSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("Concise, journalistic title of the conspiracy theory"),
                "originYear" to Schema.integer("Four-digit year the theory originated or surged"),
                "chronologicalDateLabel" to Schema.string("Short uppercase date stamp like 'NOV 2026' or 'JUL 1947'"),
                "sortTimestamp" to Schema.integer("Chronological integer key in YYYYMMDD format, e.g. 20261120 or 19470708"),
                "category" to Schema.enumeration(
                    values = listOf("History", "Technology", "Space", "Politics", "Science", "Pop Culture"),
                    description = "Primary topic category of the theory"
                ),
                "tractionStatus" to Schema.enumeration(
                    values = listOf("Trending Web", "Viral Lore", "Classic", "Declassified"),
                    description = "Current cultural or web traction"
                ),
                "summary" to Schema.string("Objective 2-3 sentence journalistic summary"),
                "originStory" to Schema.string("How and where the theory began and evolved chronologically"),
                "recentUpdates" to Schema.string("Latest web updates, recent news, declassifications, or viral developments from 2024-2026"),
                "keyClaims" to Schema.array(
                    items = Schema.string(),
                    description = "3 to 4 bulleted core claims made by proponents"
                ),
                "factualContext" to Schema.string("Objective fact-check, documented evidence, or official findings"),
                "dangersAndLaws" to Schema.array(
                    items = Schema.string(),
                    description = "2 to 4 specific laws, private property / no-trespassing notices, restricted zones, or physical/legal dangers tied to this theory"
                ),
                "latitude" to Schema.double("Geographic latitude if tied to a specific physical site, otherwise null", nullable = true),
                "longitude" to Schema.double("Geographic longitude if tied to a specific physical site, otherwise null", nullable = true),
                "locationName" to Schema.string("Physical location name if tied to a site, otherwise null", nullable = true),
                "wikipediaSearchTerm" to Schema.string("Short encyclopedia keyword for fetching a real photo, e.g. 'Area 51'"),
                "isWeeklySpotlight" to Schema.boolean("True ONLY for the single featured Conspiracy of the Week"),
                "weeklySpotlightNote" to Schema.string("Why this theory is spotlighted this week, or null", nullable = true),
                "relevantSourceIndices" to Schema.array(
                    items = Schema.integer(),
                    description = "Zero-based indices from the provided GROUNDED_SOURCES list relevant to this theory"
                )
            ),
            optionalProperties = listOf("latitude", "longitude", "locationName", "weeklySpotlightNote")
        )

        Schema.obj(
            properties = mapOf(
                "dossiers" to Schema.array(
                    items = dossierItemSchema,
                    description = "List of curated conspiracy theory dossiers ordered chronologically"
                )
            )
        )
    }

    private val structuringModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = dossierListSchema
            }
        )
    }

    suspend fun searchAndSummarize(
        query: String,
        includeWeeklySpotlight: Boolean
    ): List<TheoryDossier> {
        val todayLabel = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
        val searchPrompt = buildString {
            appendLine("Today's date is $todayLabel.")
            appendLine("Use Google Search to research conspiracy theories on the web for: \"$query\".")
            if (includeWeeklySpotlight) {
                appendLine("Select ONE prominent theory trending or widely discussed this week as the 'Conspiracy of the Week' spotlight, plus 4 additional distinct conspiracy theories (spanning recent viral theories and relevant historical origins) for a curated chronological feed of 5 total entries.")
            } else {
                appendLine("Find and summarize 4 to 5 distinct conspiracy theories or unexplained phenomena closely matching \"$query\", spanning different dates so they form a rich chronological feed.")
            }
            appendLine("For EACH theory, search the web for and report:")
            appendLine("1. Exact title and origin/peak date (YYYY-MM-DD or YYYY).")
            appendLine("2. Category (strictly one of: History, Technology, Space, Politics, Science, Pop Culture).")
            appendLine("3. Objective journalistic summary, chronological origin story, 3-4 key claims, and verified factual context / reality check (PG-13, neutral investigative tone).")
            appendLine("4. RECENT UPDATES: Latest web updates, news, declassifications, congressional/scientific hearings, or recent viral activity as of 2025-2026.")
            appendLine("5. DANGERS & LAWS: Specific legal restrictions, private property / no-trespassing laws, restricted military or federal airspace/land boundaries (e.g. 18 U.S.C. § 1382, FAA TFRs, local trespassing statutes), environmental/terrain hazards, or cyber/harassment/doxxing legal risks relevant to that specific theory.")
            appendLine("6. Specific GPS coordinates (latitude and longitude in decimal degrees) and site name IF the theory centers on a real physical location (e.g., Groom Lake, Roswell, Denver International Airport, CERN, Dyatlov Pass, Bermuda Triangle, Oak Island). If purely online or non-geographic, state no GPS coordinates.")
            appendLine("7. A clean Wikipedia/encyclopedia keyword to locate archival imagery.")
        }

        val groundedResponse = withTimeout(60_000L) {
            searchModel.generateContent(searchPrompt)
        }

        val researchText = groundedResponse.text
            ?: throw IllegalStateException("Web search returned an empty synthesis.")

        val groundingMetadata = groundedResponse.candidates.firstOrNull()?.groundingMetadata
        val searchQueries = groundingMetadata?.webSearchQueries?.filter { it.isNotBlank() }.orEmpty()
        val rawSources = groundingMetadata?.groundingChunks
            ?.mapNotNull { chunk ->
                val web = chunk.web ?: return@mapNotNull null
                val uri = web.uri?.takeIf { it.startsWith("http") } ?: return@mapNotNull null
                val title = web.title?.takeIf { it.isNotBlank() } ?: extractDomain(uri)
                TheorySource(
                    title = title,
                    url = uri,
                    domain = extractDomain(uri)
                )
            }
            ?.distinctBy { it.url }
            .orEmpty()

        val sourceIndexBlock = if (rawSources.isNotEmpty()) {
            rawSources.mapIndexed { idx, src ->
                "[$idx] ${src.title} (${src.domain}) -> ${src.url}"
            }.joinToString("\n")
        } else {
            "No indexed web chunks returned."
        }

        val structuringPrompt = buildString {
            appendLine("Convert the following web-researched conspiracy dossier notes into the required JSON schema.")
            appendLine("Preserve exact facts, dates, recentUpdates, dangersAndLaws (private property, no trespassing, restricted zone statutes, physical/legal risks), and geographic GPS coordinates when present. Use null for latitude/longitude/locationName when not tied to a physical location.")
            if (includeWeeklySpotlight) {
                appendLine("Mark exactly ONE entry with isWeeklySpotlight = true and fill weeklySpotlightNote with a crisp 1-sentence reason why it is Conspiracy of the Week.")
            } else {
                appendLine("Set isWeeklySpotlight = false and weeklySpotlightNote = null on all entries.")
            }
            appendLine()
            appendLine("=== RESEARCH REPORT ===")
            appendLine(researchText)
            appendLine()
            appendLine("=== GROUNDED_SOURCES ===")
            appendLine(sourceIndexBlock)
        }

        val structuredResponse = withTimeout(45_000L) {
            structuringModel.generateContent(structuringPrompt)
        }

        val jsonText = structuredResponse.text
            ?: throw IllegalStateException("Structuring pass returned empty JSON.")

        val parsedDrafts = parseStructuredDossiers(
            jsonString = jsonText,
            rawSources = rawSources,
            searchQueries = searchQueries
        )

        if (parsedDrafts.isEmpty()) {
            throw IllegalStateException("No dossiers could be parsed from the web search results.")
        }

        return coroutineScope {
            parsedDrafts.map { draft ->
                async {
                    val media = mediaFinder.findMediaForTopic(draft.wikiTerm)
                    val combinedSources = buildList {
                        addAll(draft.dossier.sources)
                        media.wikipediaSource?.let { wiki ->
                            if (none { it.url == wiki.url }) add(wiki)
                        }
                    }
                    draft.dossier.copy(
                        imageUrl = media.imageUrl,
                        sources = combinedSources
                    )
                }
            }.awaitAll()
        }
    }

    fun streamTheoryUpdate(
        dossier: TheoryDossier,
        followUpQuestion: String?
    ): Flow<GroundingStreamUpdate> = flow {
        val todayLabel = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
        val prompt = buildString {
            appendLine("Today's date is $todayLabel.")
            appendLine("Use Google Search to investigate the latest web updates, news, and legal/safety advisories for: \"${dossier.title}\" (originated ${dossier.chronologicalDateLabel}).")
            if (!followUpQuestion.isNullOrBlank()) {
                appendLine("Focus specifically on answering: \"$followUpQuestion\"")
            } else {
                appendLine("Provide two clear sections:")
                appendLine("1) LATEST WEB UPDATES: Recent developments, investigations, or declassifications.")
                appendLine("2) DANGERS, PRIVATE PROPERTY & APPLICABLE LAWS: Specific trespassing laws, restricted federal/military/private boundaries, environmental hazards, or legal risks related to this theory.")
            }
            appendLine("Keep the tone objective, analytical, and PG-13.")
        }

        val accumulated = StringBuilder()
        val collectedSources = linkedMapOf<String, TheorySource>()
        val collectedQueries = linkedSetOf<String>()

        withTimeout(60_000L) {
            searchModel.generateContentStream(prompt).collect { chunk ->
                val textPart = chunk.text.orEmpty()
                if (textPart.isNotEmpty()) {
                    accumulated.append(textPart)
                }
                val meta = chunk.candidates.firstOrNull()?.groundingMetadata
                meta?.webSearchQueries?.filter { it.isNotBlank() }?.forEach {
                    collectedQueries.add(it)
                }
                meta?.groundingChunks?.forEach { gc ->
                    val web = gc.web
                    val uri = web?.uri?.takeIf { it.startsWith("http") }
                    if (uri != null) {
                        val title = web.title?.takeIf { it.isNotBlank() } ?: extractDomain(uri)
                        collectedSources.putIfAbsent(
                            uri,
                            TheorySource(title = title, url = uri, domain = extractDomain(uri))
                        )
                    }
                }
                emit(
                    GroundingStreamUpdate(
                        accumulatedText = accumulated.toString(),
                        isComplete = false,
                        sources = collectedSources.values.toList(),
                        searchQueries = collectedQueries.toList()
                    )
                )
            }
        }

        if (accumulated.isBlank()) {
            throw IllegalStateException("Live update search returned an empty response.")
        }

        emit(
            GroundingStreamUpdate(
                accumulatedText = accumulated.toString(),
                isComplete = true,
                sources = collectedSources.values.toList(),
                searchQueries = collectedQueries.toList()
            )
        )
    }

    private data class DraftDossier(
        val dossier: TheoryDossier,
        val wikiTerm: String
    )

    private fun parseStructuredDossiers(
        jsonString: String,
        rawSources: List<TheorySource>,
        searchQueries: List<String>
    ): List<DraftDossier> {
        val root = JSONObject(jsonString)
        val array = root.optJSONArray("dossiers") ?: return emptyList()
        val now = System.currentTimeMillis()
        val results = mutableListOf<DraftDossier>()

        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val title = obj.optString("title").trim()
            if (title.isBlank()) continue

            val originYear = obj.optInt("originYear", 2024).coerceIn(1000, 2030)
            val dateLabel = obj.optString("chronologicalDateLabel")
                .ifBlank { originYear.toString() }
                .uppercase(Locale.US)
            val sortTs = obj.optLong("sortTimestamp", originYear * 10000L + 101L)
            val category = TheoryTopic.normalize(obj.optString("category", "History"))
            val traction = obj.optString("tractionStatus", "Classic")
            val summary = obj.optString("summary", "")
            val originStory = obj.optString("originStory", summary)
            val recentUpdates = obj.optString("recentUpdates", "")
            val factualContext = obj.optString("factualContext", "")

            val claimsArray = obj.optJSONArray("keyClaims")
            val keyClaims = if (claimsArray != null) {
                (0 until claimsArray.length()).mapNotNull { idx ->
                    claimsArray.optString(idx).takeIf { it.isNotBlank() }
                }
            } else {
                emptyList()
            }

            val lawsArray = obj.optJSONArray("dangersAndLaws")
            val dangersAndLaws = if (lawsArray != null) {
                (0 until lawsArray.length()).mapNotNull { idx ->
                    lawsArray.optString(idx).takeIf { it.isNotBlank() }
                }
            } else {
                emptyList()
            }

            val lat = if (obj.isNull("latitude")) null else obj.optDouble("latitude").takeIf { !it.isNaN() }
            val lon = if (obj.isNull("longitude")) null else obj.optDouble("longitude").takeIf { !it.isNaN() }
            val validLat = lat?.takeIf { it in -90.0..90.0 }
            val validLon = lon?.takeIf { it in -180.0..180.0 }
            val locationName = if (obj.isNull("locationName")) null else obj.optString("locationName").takeIf { it.isNotBlank() }

            val wikiTerm = obj.optString("wikipediaSearchTerm").ifBlank { title }
            val isSpotlight = obj.optBoolean("isWeeklySpotlight", false)
            val spotlightNote = if (obj.isNull("weeklySpotlightNote")) null else obj.optString("weeklySpotlightNote").takeIf { it.isNotBlank() }

            val indicesArray = obj.optJSONArray("relevantSourceIndices")
            val mappedSources = mutableListOf<TheorySource>()
            if (indicesArray != null) {
                for (j in 0 until indicesArray.length()) {
                    val idx = indicesArray.optInt(j, -1)
                    if (idx in rawSources.indices) {
                        mappedSources.add(rawSources[idx])
                    }
                }
            }
            if (mappedSources.isEmpty() && rawSources.isNotEmpty()) {
                mappedSources.addAll(rawSources.take(3))
            }

            val slugId = title.lowercase(Locale.US)
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
                .take(48)
                .ifBlank { "dossier-$i-$now" }

            val caseCode = "CF-$originYear-${(i + 1).toString().padStart(2, '0')}"

            results.add(
                DraftDossier(
                    dossier = TheoryDossier(
                        id = slugId,
                        caseCode = caseCode,
                        title = title,
                        originYear = originYear,
                        chronologicalDateLabel = dateLabel,
                        sortTimestamp = sortTs,
                        category = category,
                        tractionStatus = traction,
                        summary = summary,
                        originStory = originStory,
                        recentUpdates = recentUpdates,
                        keyClaims = keyClaims,
                        factualContext = factualContext,
                        dangersAndLaws = dangersAndLaws,
                        latitude = if (validLat != null && validLon != null) validLat else null,
                        longitude = if (validLat != null && validLon != null) validLon else null,
                        locationName = locationName,
                        imageUrl = null,
                        sources = mappedSources.distinctBy { it.url },
                        searchQueries = searchQueries,
                        isWeeklySpotlight = isSpotlight,
                        weeklySpotlightNote = spotlightNote,
                        isBookmarked = false,
                        fetchedAtMillis = now
                    ),
                    wikiTerm = wikiTerm
                )
            )
        }

        val firstSpotlightIdx = results.indexOfFirst { it.dossier.isWeeklySpotlight }
        if (firstSpotlightIdx >= 0) {
            return results.mapIndexed { idx, draft ->
                if (idx == firstSpotlightIdx) draft
                else draft.copy(dossier = draft.dossier.copy(isWeeklySpotlight = false))
            }
        }
        return results
    }

    private fun extractDomain(url: String): String {
        return runCatching {
            Uri.parse(url).host?.removePrefix("www.") ?: "web"
        }.getOrDefault("web")
    }
}
