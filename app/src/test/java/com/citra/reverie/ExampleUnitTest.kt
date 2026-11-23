package com.citra.reverie

import com.citra.reverie.data.AudiusApiService
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun testAudiusApiService() = runBlocking {
        println("Testing AudiusApiService...")
        val audiusService = AudiusApiService()
        val trending = audiusService.fetchTrendingTracks(10)
        println("Trending tracks count: ${trending.size}")
        if (trending.isNotEmpty()) {
            println("Trending #1: ${trending[0].title} by ${trending[0].artist} (${trending[0].audioUrl})")
        }

        val search = audiusService.searchTracks("Rock", 5)
        println("Search results count: ${search.size}")
        if (search.isNotEmpty()) {
            println("Search #1: ${search[0].title} by ${search[0].artist}")
        }

        assertTrue("Audius API should return trending tracks", trending.isNotEmpty())
    }
}
