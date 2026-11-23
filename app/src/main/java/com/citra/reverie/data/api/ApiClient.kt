package com.citra.reverie.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val LRCLIB_BASE_URL = "https://lrclib.net/api/"

    private val lrclibRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(LRCLIB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val lrclibApiService: LrcLibApiService by lazy {
        lrclibRetrofit.create(LrcLibApiService::class.java)
    }
}
