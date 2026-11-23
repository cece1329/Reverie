package com.citra.reverie.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citra.reverie.data.AudiusApiService
import com.citra.reverie.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicSearchViewModel : ViewModel() {

    private val audiusService = AudiusApiService()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchVideos() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty() || query.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        Log.d("MusicSearchViewModel", "Searching Audius for: $query")

        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null

            try {
                val response = audiusService.searchTracks(query)
                Log.d("MusicSearchViewModel", "Audius Response: ${response.size} results")
                _searchResults.value = response
            } catch (e: Exception) {
                Log.e("MusicSearchViewModel", "Search error", e)
                _searchError.value = "Gagal mencari lagu: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearchError() {
        _searchError.value = null
    }
}
