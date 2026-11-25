package com.example.data.remote

import android.util.Log
import com.example.data.model.TheorySource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

private const val TAG = "WikimediaImageFinder"

data class MediaEnrichment(
    val imageUrl: String?,
    val wikipediaSource: TheorySource?
)

class WikimediaImageFinder {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun findMediaForTopic(searchTerm: String): MediaEnrichment = withContext(Dispatchers.IO) {
        val cleanTerm = searchTerm.trim()
        if (cleanTerm.isBlank()) return@withContext MediaEnrichment(null, null)

        try {
            val encoded = URLEncoder.encode(cleanTerm, "UTF-8")
            val url =
                "https://en.wikipedia.org/w/api.php?action=query&generator=search" +
                    "&gsrsearch=$encoded&gsrlimit=3&prop=pageimages|info" +
                    "&inprop=url&piprop=thumbnail&pithumbsize=900&format=json"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CipherFeedAndroid/1.0 (Editorial Research Reader)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext MediaEnrichment(null, null)
                }
                val body = response.body?.string() ?: return@withContext MediaEnrichment(null, null)
                val root = JSONObject(body)
                val pages = root.optJSONObject("query")?.optJSONObject("pages")
                    ?: return@withContext MediaEnrichment(null, null)

                var bestImageUrl: String? = null
                var bestWikiSource: TheorySource? = null
                var bestIndex = Int.MAX_VALUE

                val keys = pages.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val page = pages.optJSONObject(key) ?: continue
                    val index = page.optInt("index", Int.MAX_VALUE)
                    val thumb = page.optJSONObject("thumbnail")?.optString("source")
                        ?.takeIf { it.startsWith("http") }
                    val fullUrl = page.optString("fullurl").takeIf { it.startsWith("http") }
                    val pageTitle = page.optString("title").takeIf { it.isNotBlank() }

                    if (thumb != null && index < bestIndex) {
                        bestIndex = index
                        bestImageUrl = thumb
                        if (fullUrl != null && pageTitle != null) {
                            bestWikiSource = TheorySource(
                                title = "Wikipedia: $pageTitle",
                                url = fullUrl,
                                domain = "en.wikipedia.org"
                            )
                        }
                    } else if (bestWikiSource == null && fullUrl != null && pageTitle != null) {
                        bestWikiSource = TheorySource(
                            title = "Wikipedia: $pageTitle",
                            url = fullUrl,
                            domain = "en.wikipedia.org"
                        )
                    }
                }

                MediaEnrichment(
                    imageUrl = bestImageUrl,
                    wikipediaSource = bestWikiSource
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Wikimedia lookup failed for '$cleanTerm': ${e.localizedMessage}")
            MediaEnrichment(null, null)
        }
    }
}
