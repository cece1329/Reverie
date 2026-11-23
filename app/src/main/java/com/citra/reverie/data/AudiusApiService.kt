package com.citra.reverie.data

import com.citra.reverie.model.Track
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.URLEncoder

/**
 * Audius REST API Service
 * Mengambil lagu, artis, cover album, durasi, dan DIRECT MP3 AUDIO STREAM URL
 * dari jaringan Audius publik (100% REST API publik, tanpa API key, stream MP3 langsung).
 */
class AudiusApiService {

    companion object {
        private const val APP_NAME = "Reverie"
        val HOSTS = listOf(
            "https://discoveryprovider.audius.co",
            "https://discoveryprovider2.audius.co",
            "https://discoveryprovider3.audius.co",
            "https://audius-dp.culprit.dev",
            "https://discoveryprovider.m5.network"
        )
        private var activeHost = HOSTS[0]

        private val client = NetworkClient.okHttpClient
        private val gson = Gson()
    }

    /**
     * Cari lagu berdasarkan judul, artis, atau genre di Audius
     */
    suspend fun searchTracks(query: String, limit: Int = 25): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val encoded = URLEncoder.encode(query, "UTF-8")

        for (host in HOSTS) {
            try {
                val url = "$host/v1/tracks/search?query=$encoded&app_name=$APP_NAME&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && !body.isNullOrBlank()) {
                    val tracks = parseAudiusResponse(body, host)
                    if (tracks.isNotEmpty()) {
                        activeHost = host
                        return@withContext tracks
                    }
                }
            } catch (_: Exception) {
                continue
            }
        }
        emptyList()
    }

    /**
     * Mengambil daftar lagu trending / populer saat ini
     */
    suspend fun fetchTrendingTracks(limit: Int = 35): List<Track> = withContext(Dispatchers.IO) {
        for (host in HOSTS) {
            try {
                val url = "$host/v1/tracks/trending?app_name=$APP_NAME&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && !body.isNullOrBlank()) {
                    val tracks = parseAudiusResponse(body, host)
                    if (tracks.isNotEmpty()) {
                        activeHost = host
                        return@withContext tracks
                    }
                }
            } catch (_: Exception) {
                continue
            }
        }
        emptyList()
    }

    /**
     * Cari lagu berdasarkan genre
     */
    suspend fun fetchTracksByGenre(genre: String, limit: Int = 25): List<Track> = withContext(Dispatchers.IO) {
        if (genre.isBlank() || genre.equals("Semua", true) || genre.equals("All", true)) {
            return@withContext fetchTrendingTracks(limit)
        }
        val encoded = URLEncoder.encode(genre, "UTF-8")
        for (host in HOSTS) {
            try {
                val url = "$host/v1/tracks/trending?genre=$encoded&app_name=$APP_NAME&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && !body.isNullOrBlank()) {
                    val tracks = parseAudiusResponse(body, host)
                    if (tracks.isNotEmpty()) {
                        activeHost = host
                        return@withContext tracks
                    }
                }
            } catch (_: Exception) {
                continue
            }
        }
        searchTracks(genre, limit)
    }

    /**
     * Mencari stream URL untuk lagu tertentu berdasarkan judul dan artis
     */
    suspend fun resolveStreamUrl(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        val q = "$title $artist".trim()
        val results = searchTracks(q, limit = 5)
        if (results.isNotEmpty()) {
            return@withContext results[0].audioUrl
        }
        if (artist.isNotBlank()) {
            val titleOnlyResults = searchTracks(title, limit = 3)
            if (titleOnlyResults.isNotEmpty()) {
                return@withContext titleOnlyResults[0].audioUrl
            }
        }
        null
    }

    private fun parseAudiusResponse(jsonString: String, host: String): List<Track> {
        val list = mutableListOf<Track>()
        try {
            val root = gson.fromJson(jsonString, JsonObject::class.java) ?: return emptyList()
            val dataArray: JsonArray = root.getAsJsonArray("data") ?: return emptyList()

            for (i in 0 until dataArray.size()) {
                val item = dataArray.get(i).asJsonObject ?: continue
                val id = if (item.has("id")) item.get("id").asString else continue
                if (id.isBlank()) continue

                val title = if (item.has("title")) item.get("title").asString else "Unknown Title"
                val durationSec = if (item.has("duration")) item.get("duration").asLong else 180L
                val genre = if (item.has("genre")) item.get("genre").asString else "Pop"

                var artist = "Audius Artist"
                if (item.has("user") && !item.get("user").isJsonNull) {
                    val userObj = item.getAsJsonObject("user")
                    if (userObj.has("name") && !userObj.get("name").isJsonNull) {
                        artist = userObj.get("name").asString
                    }
                }

                var coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"
                if (item.has("artwork") && !item.get("artwork").isJsonNull) {
                    val artObj = item.getAsJsonObject("artwork")
                    if (artObj.has("480x480") && !artObj.get("480x480").isJsonNull) {
                        coverUrl = artObj.get("480x480").asString
                    } else if (artObj.has("150x150") && !artObj.get("150x150").isJsonNull) {
                        coverUrl = artObj.get("150x150").asString
                    }
                }

                val streamUrl = "$host/v1/tracks/$id/stream?app_name=$APP_NAME"

                list.add(
                    Track(
                        id = "aud_$id",
                        title = title,
                        artist = artist,
                        album = "Audius Music",
                        coverUrl = coverUrl,
                        audioUrl = streamUrl,
                        durationMs = durationSec * 1000L,
                        genre = genre,
                        popularity = 90
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
