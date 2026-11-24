package com.citra.reverie.data.lrclib

import com.google.gson.annotations.SerializedName

data class LrcLibResponse(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("trackId")
    val trackId: String?,
    @SerializedName("albumName")
    val albumName: String?,
    @SerializedName("artistName")
    val artistName: String?,
    @SerializedName("trackName")
    val trackName: String?,
    @SerializedName("duration")
    val duration: Double?,
    @SerializedName("plainLyrics")
    val plainLyrics: String?,
    @SerializedName("syncedLyrics")
    val syncedLyrics: String?,
    @SerializedName("instrumental")
    val instrumental: Boolean?,
    @SerializedName("lang")
    val lang: String?
)
