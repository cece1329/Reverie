package com.citra.reverie.data.repository

import android.util.Log
import com.citra.reverie.data.AudiusApiService
import com.citra.reverie.data.api.ApiClient
import com.citra.reverie.model.LyricLine
import com.citra.reverie.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object YouTubeRepository {

    private val audiusService = AudiusApiService()
    private val lrcApi = ApiClient.lrclibApiService

    /**
     * Mengambil daftar lagu terpopuler secara global dari Audius API
     */
    suspend fun getPopularMusic(): List<Track> = withContext(Dispatchers.IO) {
        try {
            Log.d("MusicRepository", "Fetching popular music from Audius API...")
            val tracks = audiusService.fetchTrendingTracks(35)
            if (tracks.isNotEmpty()) {
                return@withContext tracks
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error fetching popular music: ${e.message}")
        }
        emptyList()
    }

    /**
     * Mengambil lirik lagu bersinkronisasi dari LRCLIB API
     */
    suspend fun fetchSyncedLyrics(title: String, artist: String): List<LyricLine> = withContext(Dispatchers.IO) {
        try {
            val cleanTitle = cleanTitle(title)
            val cleanArtist = cleanArtist(artist)
            val response = lrcApi.searchLyrics(trackName = cleanTitle, artistName = cleanArtist)
            if (response.isNotEmpty()) {
                val item = response.first()
                val syncedStr = item.syncedLyrics
                if (!syncedStr.isNullOrBlank()) {
                    return@withContext parseLrcLyrics(syncedStr)
                }
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "Failed to fetch lyrics for $title: ${e.message}")
        }
        emptyList()
    }

    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("(?i)\\(official.*?\\)"), "")
            .replace(Regex("(?i)\\[official.*?\\]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun cleanArtist(artist: String): String {
        return artist
            .replace(Regex("(?i)- topic"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun parseLrcLyrics(lrcContent: String): List<LyricLine> {
        val list = mutableListOf<LyricLine>()
        val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
        for (line in lrcContent.lines()) {
            val match = regex.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val msPart = match.groupValues[3].toLongOrNull() ?: 0L
                val ms = (min * 60 * 1000) + (sec * 1000) + (if (msPart < 100) msPart * 10 else msPart)
                val text = match.groupValues[4].trim()
                if (text.isNotBlank()) {
                    list.add(LyricLine(ms, text))
                }
            }
        }
        return list.sortedBy { it.timeMs }
    }
}
