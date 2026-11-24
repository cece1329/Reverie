package com.citra.reverie.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import com.citra.reverie.MainActivity
import com.citra.reverie.data.AudiusApiService
import com.citra.reverie.data.NetworkClient
import com.citra.reverie.model.Playlist
import com.citra.reverie.model.Track
import com.citra.reverie.service.MusicService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
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
    private var mediaSession: MediaSession? = null

    fun getMediaSession(): MediaSession? {
        if (mediaSession == null) {
            initExoPlayer()
        }
        return mediaSession
    }

    fun getExoPlayer(): ExoPlayer? {
        if (exoPlayer == null) {
            initExoPlayer()
        }
        return exoPlayer
    }

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

    private val _customTracks = MutableStateFlow<List<Track>>(emptyList())
    val customTracks: StateFlow<List<Track>> = _customTracks.asStateFlow()

    private val prefs = applicationContext.getSharedPreferences("reverie_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

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
        loadSavedState()
        loadPopularAudiusMusic()
    }

    private fun loadSavedState() {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val username = prefs.getString("username", "Citra Maharani") ?: "Citra Maharani"
        val email = prefs.getString("email", "citra@reverie.app") ?: "citra@reverie.app"
        _userProfile.value = UserProfile(
            username = username,
            email = email,
            isPremium = true,
            isLoggedIn = isLoggedIn
        )

        val savedPlaylistsJson = prefs.getString("custom_playlists", null)
        if (!savedPlaylistsJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<Playlist>>() {}.type
                val list: List<Playlist>? = gson.fromJson(savedPlaylistsJson, type)
                if (list != null) _customPlaylists.value = list
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayerManager", "Error loading saved playlists: ${e.message}")
            }
        }

        val savedLikedJson = prefs.getString("liked_tracks", null)
        if (!savedLikedJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<Track>>() {}.type
                val list: List<Track>? = gson.fromJson(savedLikedJson, type)
                if (list != null) _likedTracks.value = list
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayerManager", "Error loading saved liked tracks: ${e.message}")
            }
        }

        val savedCustomTracksJson = prefs.getString("custom_tracks", null)
        if (!savedCustomTracksJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<Track>>() {}.type
                val list: List<Track>? = gson.fromJson(savedCustomTracksJson, type)
                if (list != null) _customTracks.value = list
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayerManager", "Error loading saved custom tracks: ${e.message}")
            }
        }
    }

    private fun saveUserProfileState() {
        val profile = _userProfile.value
        prefs.edit()
            .putBoolean("is_logged_in", profile.isLoggedIn)
            .putString("username", profile.username)
            .putString("email", profile.email)
            .apply()
    }

    private fun savePlaylistsState() {
        try {
            val json = gson.toJson(_customPlaylists.value)
            prefs.edit().putString("custom_playlists", json).apply()
        } catch (e: Exception) {
            android.util.Log.e("MusicPlayerManager", "Error saving playlists: ${e.message}")
        }
    }

    private fun saveLikedTracksState() {
        try {
            val json = gson.toJson(_likedTracks.value)
            prefs.edit().putString("liked_tracks", json).apply()
        } catch (e: Exception) {
            android.util.Log.e("MusicPlayerManager", "Error saving liked tracks: ${e.message}")
        }
    }

    private fun saveCustomTracksState() {
        try {
            val json = gson.toJson(_customTracks.value)
            prefs.edit().putString("custom_tracks", json).apply()
        } catch (e: Exception) {
            android.util.Log.e("MusicPlayerManager", "Error saving custom tracks: ${e.message}")
        }
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
        if (exoPlayer == null) {
            val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
            val okHttpDataSourceFactory = OkHttpDataSource.Factory(NetworkClient.okHttpClient)
                .setUserAgent(userAgent)

            val defaultDataSourceFactory = DefaultDataSource.Factory(
                applicationContext,
                okHttpDataSourceFactory
            )

            val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory)

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

        val currentPlayer = exoPlayer ?: return

        if (mediaSession == null) {
            val sessionActivityIntent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                sessionActivityIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val forwardingPlayer = object : ForwardingPlayer(currentPlayer) {
                override fun getAvailableCommands(): Player.Commands {
                    return super.getAvailableCommands().buildUpon()
                        .add(COMMAND_SEEK_TO_NEXT)
                        .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .add(COMMAND_SEEK_TO_PREVIOUS)
                        .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .build()
                }

                override fun isCommandAvailable(command: Int): Boolean {
                    return command == COMMAND_SEEK_TO_NEXT ||
                            command == COMMAND_SEEK_TO_NEXT_MEDIA_ITEM ||
                            command == COMMAND_SEEK_TO_PREVIOUS ||
                            command == COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM ||
                            super.isCommandAvailable(command)
                }

                override fun seekToNext() {
                    scope.launch { nextTrack() }
                }

                override fun seekToNextMediaItem() {
                    scope.launch { nextTrack() }
                }

                override fun seekToPrevious() {
                    scope.launch { previousTrack() }
                }

                override fun seekToPreviousMediaItem() {
                    scope.launch { previousTrack() }
                }
            }

            mediaSession = MediaSession.Builder(applicationContext, forwardingPlayer)
                .setSessionActivity(pendingIntent)
                .setId("ReverieMediaSession")
                .build()
        }
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
            var artworkBytes: ByteArray? = null

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

                // Unduh cover artwork bytes untuk tampilan notifikasi instan berkualitas tinggi
                if (track.coverUrl.isNotBlank() && track.coverUrl.startsWith("http")) {
                    try {
                        val request = okhttp3.Request.Builder().url(track.coverUrl).build()
                        NetworkClient.okHttpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val bytes = response.body?.bytes()
                                if (bytes != null && bytes.size < 800 * 1024) {
                                    artworkBytes = bytes
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("MusicPlayerManager", "Cover download skipped: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayerManager", "resolveAndPlayExoPlayer error: ${e.message}")
            }

            withContext(Dispatchers.Main) {
                if (_currentTrack.value?.id != trackId) return@withContext

                if (!directAudioUrl.isNullOrBlank()) {
                    android.util.Log.d("MusicPlayerManager", "Playing stream via ExoPlayer: $directAudioUrl")
                    playWithExoPlayer(directAudioUrl, resolvedDurationMs, track, artworkBytes)
                } else {
                    android.util.Log.e("MusicPlayerManager", "Failed to resolve direct audio stream for ${track.title}")
                    _isBuffering.value = false
                    _isPlaying.value = false
                }
            }
        }
    }

    private fun playWithExoPlayer(
        audioUrl: String,
        resolvedDurationMs: Long = 0L,
        track: Track? = _currentTrack.value,
        artworkBytes: ByteArray? = null
    ) {
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

            val current = track ?: _currentTrack.value
            val metadataBuilder = MediaMetadata.Builder()
                .setTitle(current?.title ?: "Reverie Music")
                .setArtist(current?.artist ?: "Unknown Artist")
                .setAlbumTitle(current?.album ?: "Reverie")

            if (!current?.coverUrl.isNullOrBlank()) {
                try {
                    metadataBuilder.setArtworkUri(Uri.parse(current.coverUrl))
                } catch (_: Exception) {}
            }
            if (artworkBytes != null) {
                metadataBuilder.setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            }

            val mediaItem = MediaItem.Builder()
                .setUri(audioUrl)
                .setMediaId(current?.id ?: "reverie_track_${System.currentTimeMillis()}")
                .setMediaMetadata(metadataBuilder.build())
                .build()

            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.playWhenReady = true
            _isPlaying.value = true

            try {
                val serviceIntent = Intent(applicationContext, MusicService::class.java)
                ContextCompat.startForegroundService(applicationContext, serviceIntent)
            } catch (e: Exception) {
                android.util.Log.w("MusicPlayerManager", "startForegroundService exception: ${e.message}")
                try {
                    val serviceIntent = Intent(applicationContext, MusicService::class.java)
                    applicationContext.startService(serviceIntent)
                } catch (_: Exception) {}
            }
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
                try {
                    val serviceIntent = Intent(applicationContext, MusicService::class.java)
                    ContextCompat.startForegroundService(applicationContext, serviceIntent)
                } catch (_: Exception) {}
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
        saveLikedTracksState()
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

        _customTracks.value = listOf(newTrack) + _customTracks.value
        _queue.value = listOf(newTrack) + _queue.value
        saveCustomTracksState()
        playTrack(newTrack, _customTracks.value)
    }

    fun addLocalAudioTrack(title: String, artist: String, coverUrl: String, fileUri: String) {
        scope.launch(Dispatchers.IO) {
            val uri = Uri.parse(fileUri)
            val localAudioPath = copyLocalAudioFile(applicationContext, uri)
            val measuredDuration = getAudioDurationMs(applicationContext, localAudioPath)

            val cover = if (coverUrl.isNotBlank()) coverUrl
            else "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"

            val newTrack = Track(
                id = "local_${System.currentTimeMillis()}",
                title = title.ifBlank { "Lagu Lokal" },
                artist = artist.ifBlank { "Perangkat Saya" },
                album = "Lagu Saya",
                coverUrl = cover,
                audioUrl = localAudioPath,
                durationMs = measuredDuration
            )

            withContext(Dispatchers.Main) {
                _customTracks.value = listOf(newTrack) + _customTracks.value
                _queue.value = listOf(newTrack) + _queue.value
                saveCustomTracksState()
                playTrack(newTrack, _customTracks.value)
            }
        }
    }

    fun deleteCustomTrack(trackId: String) {
        val track = _customTracks.value.find { it.id == trackId }
        _customTracks.value = _customTracks.value.filterNot { it.id == trackId }
        saveCustomTracksState()

        _likedTracks.value = _likedTracks.value.filterNot { it.id == trackId }
        saveLikedTracksState()

        if (_currentTrack.value?.id == trackId) {
            nextTrack()
        }

        track?.let { t ->
            if (t.audioUrl.startsWith("file://")) {
                try {
                    val fileUri = Uri.parse(t.audioUrl)
                    fileUri.path?.let { path ->
                        val file = File(path)
                        if (file.exists()) file.delete()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MusicPlayerManager", "Failed to delete local audio file: ${e.message}")
                }
            }
        }
    }

    private fun copyLocalAudioFile(context: Context, sourceUri: Uri): String {
        return try {
            val dir = File(context.filesDir, "custom_music")
            if (!dir.exists()) dir.mkdirs()
            val destFile = File(dir, "track_${System.currentTimeMillis()}.mp3")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (destFile.exists() && destFile.length() > 0) {
                Uri.fromFile(destFile).toString()
            } else {
                sourceUri.toString()
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicPlayerManager", "Failed to copy local audio file: ${e.message}")
            sourceUri.toString()
        }
    }

    private fun getAudioDurationMs(context: Context, audioUriStr: String): Long {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            val uri = Uri.parse(audioUriStr)
            if (audioUriStr.startsWith("content://") || audioUriStr.startsWith("file://")) {
                retriever.setDataSource(context, uri)
            } else {
                retriever.setDataSource(audioUriStr, HashMap())
            }
            val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLongOrNull() ?: 180000L
        } catch (e: Exception) {
            android.util.Log.w("MusicPlayerManager", "Failed to retrieve duration: ${e.message}")
            180000L
        }
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
        savePlaylistsState()
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
        savePlaylistsState()
    }

    fun addTrackToPlaylist(playlistId: String, track: Track) {
        _customPlaylists.value = _customPlaylists.value.map { pl ->
            if (pl.id == playlistId && pl.tracks.none { it.id == track.id }) {
                val updated = pl.tracks + track
                pl.copy(tracks = updated, trackCount = updated.size)
            } else pl
        }
        savePlaylistsState()
    }

    fun deletePlaylist(playlistId: String) {
        _customPlaylists.value = _customPlaylists.value.filterNot { it.id == playlistId }
        savePlaylistsState()
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    fun updateUserProfile(username: String, email: String) {
        _userProfile.value = _userProfile.value.copy(
            username = username.ifBlank { _userProfile.value.username },
            email = email.ifBlank { _userProfile.value.email }
        )
        saveUserProfileState()
    }

    fun login(username: String, email: String) {
        _userProfile.value = UserProfile(username = username, email = email, isLoggedIn = true)
        saveUserProfileState()
    }

    fun logout() {
        _userProfile.value = UserProfile(username = "Tamu", email = "", isLoggedIn = false)
        _currentTrack.value = null
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _isPlaying.value = false
        saveUserProfileState()
    }

    fun releaseMediaSession() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
    }

    fun release() {
        stopProgressTracker()
        resolveJob?.cancel()
        releaseMediaSession()
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
