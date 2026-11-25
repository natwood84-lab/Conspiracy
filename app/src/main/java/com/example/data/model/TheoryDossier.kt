package com.example.data.model

import java.util.Locale
import kotlin.math.abs

enum class TheoryTopic(val label: String) {
    ALL("All"),
    HISTORY("History"),
    TECHNOLOGY("Technology"),
    SPACE("Space"),
    POLITICS("Politics"),
    SCIENCE("Science"),
    POP_CULTURE("Pop Culture");

    companion object {
        val selectableTopics: List<TheoryTopic> = entries.filter { it != ALL }

        fun normalize(rawCategory: String): String {
            val cleaned = rawCategory.trim().lowercase(Locale.US)
            return when {
                cleaned == "history" || cleaned.contains("histor") || cleaned.contains("ancient") -> HISTORY.label
                cleaned == "technology" || cleaned.contains("tech") || cleaned.contains("ai") || cleaned.contains("cyber") -> TECHNOLOGY.label
                cleaned == "space" || cleaned.contains("uap") || cleaned.contains("ufo") || cleaned.contains("lunar") || cleaned.contains("alien") -> SPACE.label
                cleaned == "politics" || cleaned.contains("politic") || cleaned.contains("declassif") || cleaned.contains("gov") || cleaned.contains("cia") -> POLITICS.label
                cleaned == "science" || cleaned.contains("scien") || cleaned.contains("physics") || cleaned.contains("bio") -> SCIENCE.label
                cleaned == "pop culture" || cleaned.contains("pop") || cleaned.contains("internet") || cleaned.contains("media") || cleaned.contains("lore") -> POP_CULTURE.label
                else -> HISTORY.label
            }
        }
    }
}

data class TheorySource(
    val title: String,
    val url: String,
    val domain: String
)

data class TheoryDossier(
    val id: String,
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
    val keyClaims: List<String>,
    val factualContext: String,
    val dangersAndLaws: List<String>,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val imageUrl: String?,
    val sources: List<TheorySource>,
    val searchQueries: List<String>,
    val isWeeklySpotlight: Boolean,
    val weeklySpotlightNote: String?,
    val isBookmarked: Boolean,
    val fetchedAtMillis: Long,
    val savedLiveNotes: String? = null,
    val bookmarkedAtMillis: Long? = null
) {
    val normalizedTopic: String
        get() = TheoryTopic.normalize(category)

    val hasGpsCoordinates: Boolean
        get() = latitude != null && longitude != null &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0

    val formattedGps: String?
        get() = if (latitude != null && longitude != null &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0
        ) {
            formatGpsCoordinates(latitude, longitude)
        } else {
            null
        }
}

enum class ChronologicalOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

fun formatGpsCoordinates(latitude: Double, longitude: Double): String {
    val latDir = if (latitude >= 0) "N" else "S"
    val lonDir = if (longitude >= 0) "E" else "W"
    return String.format(
        Locale.US,
        "%.4f° %s, %.4f° %s",
        abs(latitude),
        latDir,
        abs(longitude),
        lonDir
    )
}
