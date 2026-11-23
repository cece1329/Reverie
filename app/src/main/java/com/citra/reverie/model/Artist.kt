package com.citra.reverie.model

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String,
    val genre: String = "Pop",
    val followers: Int = 0
)
