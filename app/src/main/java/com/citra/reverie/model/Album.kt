package com.citra.reverie.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val releaseYear: String = "2024",
    val trackCount: Int = 10,
    val tracks: List<Track> = emptyList()
)
