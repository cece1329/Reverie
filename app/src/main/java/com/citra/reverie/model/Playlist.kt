package com.citra.reverie.model

data class Playlist(
    val id: String,
    val name: String,
    val title: String = name,
    val description: String,
    val coverUrl: String,
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList()
)
