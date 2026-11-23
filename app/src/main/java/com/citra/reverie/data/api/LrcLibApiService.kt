package com.citra.reverie.data.api

import com.citra.reverie.data.lrclib.LrcLibResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LrcLibApiService {
    @GET("search")
    suspend fun searchLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String
    ): List<LrcLibResponse>
}
