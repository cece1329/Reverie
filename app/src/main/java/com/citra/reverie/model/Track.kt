package com.citra.reverie.model

import java.util.Locale

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String,
    val audioUrl: String = "",
    val durationMs: Long = 180000L,
    val isLiked: Boolean = false,
    val genre: String = "Pop",
    val lyrics: List<String> = emptyList(),
    val syncedLyrics: List<LyricLine> = emptyList(),
    val youtubeId: String = "",
    val hasPreview: Boolean = false,
    val popularity: Int = 0
) {
    fun formattedDuration(): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    /**
     * Mengembalikan list baris lirik dengan timestamp.
     * Jika syncedLyrics belum ada, secara cerdas mendistribusikan baris lyrics biasa ke sepanjang durasi lagu.
     */
    fun getEffectiveSyncedLyrics(): List<LyricLine> {
        if (syncedLyrics.isNotEmpty()) return syncedLyrics
        if (lyrics.isEmpty()) return emptyList()

        val intervalMs = if (lyrics.size > 1) (durationMs - 10000L).coerceAtLeast(1000L) / lyrics.size else 5000L
        return lyrics.mapIndexed { index, text ->
            LyricLine(timeMs = 3000L + index * intervalMs, text = text)
        }
    }
}
