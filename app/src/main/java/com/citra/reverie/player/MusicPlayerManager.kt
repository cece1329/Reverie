package com.citra.reverie.player

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.citra.reverie.data.AudiusApiService
import com.citra.reverie.data.NetworkClient
import com.citra.reverie.model.Playlist
import com.citra.reverie.model.Track
import com.citra.reverie.service.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RepeatMode {
    OFF, ALL, ONE
}

data class UserProfile(
    val username: String = "Citra Maharani",
    val email: String = "citra@reverie.app",
    val isPremium: Boolean = true,
    val isLoggedIn: Boolean = false
)

/**
 * MusicPlayerManager
 * Pemutar musik native Android menggunakan Media3 ExoPlayer & Audius REST API
 */
class MusicPlayerManager(context: Context) {

    private val applicationContext = context.applicationContext
    private val audiusService = AudiusApiService()

    private var exoPlayer: ExoPlayer? = null

    private var fallbackAttempts = 0
    private val maxFallbackAttempts = 3

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(180000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _likedTracks = MutableStateFlow<List<Track>>(emptyList())
    val likedTracks: StateFlow<List<Track>> = _likedTracks.asStateFlow()

    private val _history = MutableStateFlow<List<Track>>(emptyList())
    val history: StateFlow<List<Track>> = _history.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _customPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val customPlaylists: StateFlow<List<Playlist>> = _customPlaylists.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var resolveJob: Job? = null
    private var bufferingTimeoutJob: Job? = null

    private fun resetBufferingTimeout() {
        bufferingTimeoutJob?.cancel()
        bufferingTimeoutJob = scope.launch {
            delay(10000L)
            if (_isBuffering.value && !_isPlaying.value) {
                android.util.Log.w("MusicPlayerManager", "Buffering timeout reached, clearing spinner")
                _isBuffering.value = false
            }
        }
    }

    init {
        initExoPlayer()
        _currentTrack.value = null
        loadPopularAudiusMusic()
    }

    private fun loadPopularAudiusMusic() {
        scope.launch {
            try {
                val audiusTracks = audiusService.fetchTrendingTracks(35)
                if (audiusTracks.isNotEmpty()) {
                    _queue.value = audiusTracks
                }
            } catch (e: Exception) {
                android.util.Log.w("MusicPlayerManager", "Failed to load Audius music: ${e.message}")
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initExoPlayer() {
        if (exoPlayer != null) return

        val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
        val okHttpDataSourceFactory = OkHttpDataSource.Factory(NetworkClient.okHttpClient)
            .setUserAgent(userAgent)

        val mediaSourceFactory = DefaultMediaSourceFactory(applicationContext)
            .setDataSourceFactory(okHttpDataSourceFactory)

        val player = ExoPlayer.Builder(applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    _isBuffering.value = false
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> _isBuffering.value = true
                    Player.STATE_READY -> {
                        _isBuffering.value = false
                        val d = player.duration
                        if (d > 0) _durationMs.value = d
                    }
                    Player.STATE_ENDED -> {
                        _isBuffering.value = false
                        _isPlaying.value = false
                        handleTrackEnded()
                    }
                    Player.STATE_IDLE -> _isBuffering.value = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("MusicPlayer", "ExoPlayer Error code: ${error.errorCodeName}, Message: ${error.message}", error)
                _isBuffering.value = false
                _isPlaying.value = false

                if (fallbackAttempts >= maxFallbackAttempts) {
                    scope.launch {
                        delay(1000L)
                        nextTrack()
                    }
                    return
                }
                fallbackAttempts++

                val current = _currentTrack.value ?: return
                scope.launch {
                    delay(600L)
                    resolveAndPlayExoPlayer(current)
                }
            }
        })
        exoPlayer = player
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (true) {
                exoPlayer?.let { p ->
                    val pos = p.currentPosition.coerceAtLeast(0L)
                    _playbackPositionMs.value = pos
                    val d = p.duration
                    if (d > 0) _durationMs.value = d
                }
                delay(200L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    /**
     * Memutar lagu yang dipilih menggunakan ExoPlayer Native
     */
    fun playTrack(track: Track, newQueue: List<Track> = _queue.value) {
        resolveJob?.cancel()
        fallbackAttempts = 0

        _queue.value = newQueue
        val idx = newQueue.indexOfFirst { it.id == track.id }
        if (idx >= 0) _queueIndex.value = idx

        _currentTrack.value = track
        _playbackPositionMs.value = 0L
        if (track.durationMs > 0) _durationMs.value = track.durationMs
        _isBuffering.value = true
        resetBufferingTimeout()

        // Tambah ke riwayat
        val hist = _history.value.toMutableList()
        hist.removeAll { it.id == track.id }
        hist.add(0, track)
        _history.value = hist

        resolveAndPlayExoPlayer(track)
    }

    private fun resolveAndPlayExoPlayer(track: Track) {
        val trackId = track.id

        resetBufferingTimeout()
        resolveJob = scope.launch(Dispatchers.IO) {
            var directAudioUrl: String? = null
            var resolvedDurationMs: Long = if (track.durationMs > 0) track.durationMs else 0L

            try {
                // 1. Jika audioUrl dari track berupa URL HTTP/Content URI direct, langsung gunakan
                if (track.audioUrl.isNotBlank()) {
                    directAudioUrl = track.audioUrl
                }

                // 2. Jika masih kosong, resolusi dari Audius API secara dinamis berdasarkan judul & artis
                if (directAudioUrl.isNullOrBlank()) {
                    directAudioUrl = audiusService.resolveStreamUrl(track.title, track.artist)
                }

                // 3. Fallback: Ambil lagu trending Audius jika pencarian spesifik belum dapat
                if (directAudioUrl.isNullOrBlank()) {
                    val trending = audiusService.fetchTrendingTracks(10)
                    if (trending.isNotEmpty()) {
                        directAudioUrl = trending.random().audioUrl
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayerManager", "resolveAndPlayExoPlayer error: ${e.message}")
            }

            withContext(Dispatchers.Main) {
                if (_currentTrack.value?.id != trackId) return@withContext

                if (!directAudioUrl.isNullOrBlank()) {
                    android.util.Log.d("MusicPlayerManager", "Playing stream via ExoPlayer: $directAudioUrl")
                    playWithExoPlayer(directAudioUrl, resolvedDurationMs)
                } else {
                    android.util.Log.e("MusicPlayerManager", "Failed to resolve direct audio stream for ${track.title}")
                    _isBuffering.value = false
                    _isPlaying.value = false
                }
            }
        }
    }

    private fun playWithExoPlayer(audioUrl: String, resolvedDurationMs: Long = 0L) {
        initExoPlayer()
        try {
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
            _playbackPositionMs.value = 0L
            if (resolvedDurationMs > 0) {
                _durationMs.value = resolvedDurationMs
            }
            _isBuffering.value = true
            resetBufferingTimeout()

            val mediaItem = MediaItem.fromUri(audioUrl)
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.playWhenReady = true
            _isPlaying.value = true

            try {
                val serviceIntent = Intent(applicationContext, MusicService::class.java)
                applicationContext.startService(serviceIntent)
            } catch (_: Exception) {}
        } catch (e: Exception) {
            android.util.Log.e("MusicPlayerManager", "Error playing $audioUrl", e)
            _isPlaying.value = false
            _isBuffering.value = false
        }
    }

    fun togglePlayPause() {
        val track = _currentTrack.value ?: _queue.value.firstOrNull() ?: return

        val player = exoPlayer ?: run {
            playTrack(track)
            return
        }

        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            if (player.currentMediaItem == null) {
                playTrack(track)
            } else {
                player.play()
                _isPlaying.value = true
            }
        }
    }

    fun seekTo(positionMs: Long) {
        _playbackPositionMs.value = positionMs
        exoPlayer?.seekTo(positionMs)
    }

    fun nextTrack() {
        val q = _queue.value
        if (q.isEmpty()) return
        val next = if (_shuffleMode.value) q.indices.random() else (_queueIndex.value + 1) % q.size
        playTrack(q[next], q)
    }

    fun previousTrack() {
        val q = _queue.value
        if (q.isEmpty()) return
        if (_playbackPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }
        val prev = if (_queueIndex.value - 1 < 0) q.size - 1 else _queueIndex.value - 1
        playTrack(q[prev], q)
    }

    private fun handleTrackEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> _currentTrack.value?.let { playTrack(it) }
            RepeatMode.ALL -> nextTrack()
            RepeatMode.OFF -> {
                if (_queueIndex.value < _queue.value.size - 1) nextTrack()
                else {
                    _isPlaying.value = false
                    _playbackPositionMs.value = 0L
                }
            }
        }
    }

    fun toggleShuffle() {
        _shuffleMode.value = !_shuffleMode.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleLike(track: Track) {
        val currentList = _likedTracks.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == track.id }
        if (existingIndex >= 0) {
            currentList.removeAt(existingIndex)
        } else {
            currentList.add(0, track)
        }
        _likedTracks.value = currentList
    }

    fun addCustomTrack(title: String, artist: String, coverUrl: String, songUrl: String) {
        val rawUrl = songUrl.trim()
        val cover = if (coverUrl.isNotBlank()) coverUrl
        else "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"

        val newTrack = Track(
            id = "custom_${System.currentTimeMillis()}",
            title = title,
            artist = artist,
            album = "Koleksi Saya",
            coverUrl = cover,
            audioUrl = rawUrl,
            durationMs = 210000L
        )

        _queue.value = listOf(newTrack) + _queue.value
        _likedTracks.value = listOf(newTrack) + _likedTracks.value
        playTrack(newTrack)
    }

    fun addLocalAudioTrack(title: String, artist: String, coverUrl: String, fileUri: String) {
        val cover = if (coverUrl.isNotBlank()) coverUrl
        else "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"

        val newTrack = Track(
            id = "local_${System.currentTimeMillis()}",
            title = title.ifBlank { "Lagu Lokal" },
            artist = artist.ifBlank { "Perangkat Saya" },
            album = "File Perangkat",
            coverUrl = cover,
            audioUrl = fileUri,
            durationMs = 210000L
        )

        _queue.value = listOf(newTrack) + _queue.value
        _likedTracks.value = listOf(newTrack) + _likedTracks.value
        playTrack(newTrack)
    }

    fun createPlaylist(name: String, description: String, coverUrl: String = "") {
        val cover = if (coverUrl.isNotBlank()) coverUrl
        else "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80"

        val newPl = Playlist(
            id = "cpl_${System.currentTimeMillis()}",
            name = name,
            description = description.ifBlank { "Playlist buatan pengguna" },
            coverUrl = cover,
            trackCount = 0,
            tracks = emptyList()
        )
        _customPlaylists.value = _customPlaylists.value + newPl
    }

    fun editPlaylist(playlistId: String, newName: String, newDesc: String, newCoverUrl: String) {
        _customPlaylists.value = _customPlaylists.value.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(
                    name = newName.ifBlank { pl.name },
                    description = newDesc.ifBlank { pl.description },
                    coverUrl = if (newCoverUrl.isNotBlank()) newCoverUrl else pl.coverUrl
                )
            } else pl
        }
    }

    fun addTrackToPlaylist(playlistId: String, track: Track) {
        _customPlaylists.value = _customPlaylists.value.map { pl ->
            if (pl.id == playlistId && pl.tracks.none { it.id == track.id }) {
                val updated = pl.tracks + track
                pl.copy(tracks = updated, trackCount = updated.size)
            } else pl
        }
    }

    fun deletePlaylist(playlistId: String) {
        _customPlaylists.value = _customPlaylists.value.filterNot { it.id == playlistId }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    fun updateUserProfile(username: String, email: String) {
        _userProfile.value = _userProfile.value.copy(
            username = username.ifBlank { _userProfile.value.username },
            email = email.ifBlank { _userProfile.value.email }
        )
    }

    fun login(username: String, email: String) {
        _userProfile.value = UserProfile(username = username, email = email, isLoggedIn = true)
        _customPlaylists.value = emptyList()
        _likedTracks.value = emptyList()
    }

    fun logout() {
        _userProfile.value = UserProfile(username = "Tamu", email = "", isLoggedIn = false)
        _customPlaylists.value = emptyList()
        _likedTracks.value = emptyList()
        _currentTrack.value = null
        exoPlayer?.stop()
        _isPlaying.value = false
    }

    fun release() {
        stopProgressTracker()
        resolveJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        @Volatile
        private var instance: MusicPlayerManager? = null

        fun getInstance(context: Context): MusicPlayerManager =
            instance ?: synchronized(this) {
                instance ?: MusicPlayerManager(context).also { instance = it }
            }
    }
}
