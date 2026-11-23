package com.citra.reverie.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.citra.reverie.data.LanguageManager
import com.citra.reverie.model.Playlist
import com.citra.reverie.model.Track
import com.citra.reverie.player.MusicPlayerManager
import com.citra.reverie.player.UserProfile
import com.citra.reverie.ui.components.MiniPlayer
import com.citra.reverie.ui.components.ReverieBottomBar
import com.citra.reverie.ui.components.ReverieNavRail
import com.citra.reverie.ui.components.ReverieTab
import com.citra.reverie.ui.components.SplashScreen
import com.citra.reverie.ui.screens.HomeScreen
import com.citra.reverie.ui.screens.LibraryScreen
import com.citra.reverie.ui.screens.MusicSearchScreen
import com.citra.reverie.ui.screens.NowPlayingScreen
import com.citra.reverie.ui.screens.ProfileScreen
import com.citra.reverie.ui.theme.ReverieTheme

@Composable
fun ReverieApp(
    onEnterPiP: () -> Unit = {}
) {
    val context = LocalContext.current
    val playerManager = remember { MusicPlayerManager.getInstance(context) }

    var showSplashScreen by remember { mutableStateOf(true) }

    if (showSplashScreen) {
        ReverieTheme(darkTheme = true) {
            SplashScreen(onSplashFinished = { showSplashScreen = false })
        }
        return
    }

    var isDarkMode by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(ReverieTab.HOME) }
    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    val currentTrack by playerManager.currentTrack.collectAsState()
    val isPlaying by playerManager.isPlaying.collectAsState()
    val isBuffering by playerManager.isBuffering.collectAsState()
    val progressMs by playerManager.playbackPositionMs.collectAsState()
    val durationMs by playerManager.durationMs.collectAsState()
    val shuffleMode by playerManager.shuffleMode.collectAsState()
    val repeatMode by playerManager.repeatMode.collectAsState()
    val likedTracks by playerManager.likedTracks.collectAsState()
    val history by playerManager.history.collectAsState()
    val queue by playerManager.queue.collectAsState()
    val customPlaylists by playerManager.customPlaylists.collectAsState()
    val userProfile by playerManager.userProfile.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    ReverieTheme(darkTheme = isDarkMode) {
        // Tampilkan layar Login/Daftar terlebih dahulu jika pengguna belum masuk
        if (!userProfile.isLoggedIn) {
            Scaffold { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    ProfileScreen(
                        userProfile = userProfile,
                        playlistsCount = customPlaylists.size,
                        likedCount = likedTracks.size,
                        onUpdateProfile = { u, e -> playerManager.updateUserProfile(u, e) },
                        onLogin = { u, e -> playerManager.login(u, e) },
                        onRegister = { u, e, _ -> playerManager.login(u, e) },
                        onLogout = { playerManager.logout() },
                        onToggleTheme = { isDarkMode = !isDarkMode },
                        isDarkMode = isDarkMode,
                        onToggleLanguage = { LanguageManager.toggleLanguage() }
                    )
                }
            }
            return@ReverieTheme
        }

        BackHandler(enabled = isNowPlayingExpanded) {
            isNowPlayingExpanded = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    ReverieNavRail(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        MainContentScreen(
                            currentTab = currentTab,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            likedTracks = likedTracks,
                            history = history,
                            customPlaylists = customPlaylists,
                            userProfile = userProfile,
                            isDarkMode = isDarkMode,
                            onTrackSelect = { track, newQueue ->
                                playerManager.playTrack(track, newQueue)
                            },
                            onLikeToggle = { playerManager.toggleLike(it) },
                            onCreatePlaylist = { name, desc ->
                                playerManager.createPlaylist(name, desc)
                            },
                            onDeletePlaylist = { playlistId ->
                                playerManager.deletePlaylist(playlistId)
                            },
                            onAddTrackToPlaylist = { playlistId, track ->
                                playerManager.addTrackToPlaylist(playlistId, track)
                            },
                            onAddCustomTrack = { t, a, c, s ->
                                playerManager.addCustomTrack(t, a, c, s)
                            },
                            onAddLocalTrack = { t, a, c, u ->
                                playerManager.addLocalAudioTrack(t, a, c, u)
                            },
                            onClearHistory = {
                                playerManager.clearHistory()
                            },
                            onUpdateProfile = { u, e ->
                                playerManager.updateUserProfile(u, e)
                            },
                            onLogin = { u, e -> playerManager.login(u, e) },
                            onRegister = { u, e, _ -> playerManager.login(u, e) },
                            onLogout = { playerManager.logout() },
                            onToggleTheme = { isDarkMode = !isDarkMode },
                            onToggleLanguage = { LanguageManager.toggleLanguage() }
                        )

                        if (currentTrack != null && !isNowPlayingExpanded) {
                            MiniPlayer(
                                track = currentTrack,
                                isPlaying = isPlaying,
                                progressMs = progressMs,
                                durationMs = durationMs,
                                isBuffering = isBuffering,
                                onPlayPauseClick = { playerManager.togglePlayPause() },
                                onNextClick = { playerManager.nextTrack() },
                                onExpandClick = { isNowPlayingExpanded = true },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            } else {
                Scaffold(
                    bottomBar = {
                        Column {
                            if (currentTrack != null && !isNowPlayingExpanded) {
                                MiniPlayer(
                                    track = currentTrack,
                                    isPlaying = isPlaying,
                                    progressMs = progressMs,
                                    durationMs = durationMs,
                                    isBuffering = isBuffering,
                                    onPlayPauseClick = { playerManager.togglePlayPause() },
                                    onNextClick = { playerManager.nextTrack() },
                                    onExpandClick = { isNowPlayingExpanded = true }
                                )
                            }
                            ReverieBottomBar(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it }
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        MainContentScreen(
                            currentTab = currentTab,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            likedTracks = likedTracks,
                            history = history,
                            customPlaylists = customPlaylists,
                            userProfile = userProfile,
                            isDarkMode = isDarkMode,
                            onTrackSelect = { track, newQueue ->
                                playerManager.playTrack(track, newQueue)
                            },
                            onLikeToggle = { playerManager.toggleLike(it) },
                            onCreatePlaylist = { name, desc ->
                                playerManager.createPlaylist(name, desc)
                            },
                            onDeletePlaylist = { playlistId ->
                                playerManager.deletePlaylist(playlistId)
                            },
                            onAddTrackToPlaylist = { playlistId, track ->
                                playerManager.addTrackToPlaylist(playlistId, track)
                            },
                            onAddCustomTrack = { t, a, c, s ->
                                playerManager.addCustomTrack(t, a, c, s)
                            },
                            onAddLocalTrack = { t, a, c, u ->
                                playerManager.addLocalAudioTrack(t, a, c, u)
                            },
                            onClearHistory = {
                                playerManager.clearHistory()
                            },
                            onUpdateProfile = { u, e ->
                                playerManager.updateUserProfile(u, e)
                            },
                            onLogin = { u, e -> playerManager.login(u, e) },
                            onRegister = { u, e, _ -> playerManager.login(u, e) },
                            onLogout = { playerManager.logout() },
                            onToggleTheme = { isDarkMode = !isDarkMode },
                            onToggleLanguage = { LanguageManager.toggleLanguage() }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isNowPlayingExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NowPlayingScreen(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    progressMs = progressMs,
                    durationMs = durationMs,
                    shuffleMode = shuffleMode,
                    repeatMode = repeatMode,
                    isLiked = currentTrack?.let { ct -> likedTracks.any { it.id == ct.id } } ?: false,
                    queue = queue,
                    isBuffering = isBuffering,
                    onPlayPauseClick = { playerManager.togglePlayPause() },
                    onNextClick = { playerManager.nextTrack() },
                    onPreviousClick = { playerManager.previousTrack() },
                    onSeekTo = { playerManager.seekTo(it) },
                    onToggleShuffle = { playerManager.toggleShuffle() },
                    onToggleRepeat = { playerManager.toggleRepeat() },
                    onToggleLike = { currentTrack?.let { playerManager.toggleLike(it) } },
                    onTrackSelect = { playerManager.playTrack(it, queue) },
                    onCollapseClick = { isNowPlayingExpanded = false },
                    onEnterPiP = onEnterPiP,
                    isLandscape = isLandscape
                )
            }
        }
    }
}

@Composable
private fun MainContentScreen(
    currentTab: ReverieTab,
    currentTrack: Track?,
    isPlaying: Boolean,
    likedTracks: List<Track>,
    history: List<Track>,
    customPlaylists: List<Playlist>,
    userProfile: UserProfile,
    isDarkMode: Boolean,
    onTrackSelect: (Track, List<Track>) -> Unit,
    onLikeToggle: (Track) -> Unit,
    onCreatePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onAddTrackToPlaylist: (playlistId: String, track: Track) -> Unit,
    onAddCustomTrack: (title: String, artist: String, coverUrl: String, songUrl: String) -> Unit,
    onAddLocalTrack: (title: String, artist: String, coverUrl: String, fileUri: String) -> Unit,
    onClearHistory: () -> Unit,
    onUpdateProfile: (username: String, email: String) -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    when (currentTab) {
        ReverieTab.HOME -> {
            HomeScreen(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                likedTracks = likedTracks,
                customPlaylists = customPlaylists,
                onTrackSelect = onTrackSelect,
                onLikeToggle = onLikeToggle,
                onAddTrackToPlaylist = onAddTrackToPlaylist,
                onAddCustomTrack = onAddCustomTrack,
                onAddLocalTrack = onAddLocalTrack,
                onToggleTheme = onToggleTheme,
                onToggleLanguage = onToggleLanguage,
                isDarkMode = isDarkMode
            )
        }

        ReverieTab.SEARCH -> {
            MusicSearchScreen(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                likedTracks = likedTracks,
                customPlaylists = customPlaylists,
                onTrackSelect = onTrackSelect,
                onLikeToggle = onLikeToggle,
                onAddTrackToPlaylist = onAddTrackToPlaylist
            )
        }

        ReverieTab.LIBRARY -> {
            LibraryScreen(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                likedTracks = likedTracks,
                history = history,
                customPlaylists = customPlaylists,
                onCreatePlaylist = onCreatePlaylist,
                onDeletePlaylist = onDeletePlaylist,
                onAddTrackToPlaylist = onAddTrackToPlaylist,
                onAddCustomTrack = onAddCustomTrack,
                onAddLocalTrack = onAddLocalTrack,
                onClearHistory = onClearHistory,
                onTrackSelect = onTrackSelect,
                onLikeToggle = onLikeToggle
            )
        }

        ReverieTab.SETTINGS -> {
            ProfileScreen(
                userProfile = userProfile,
                playlistsCount = customPlaylists.size,
                likedCount = likedTracks.size,
                onUpdateProfile = onUpdateProfile,
                onLogin = onLogin,
                onRegister = onRegister,
                onLogout = onLogout,
                onToggleTheme = onToggleTheme,
                isDarkMode = isDarkMode,
                onToggleLanguage = onToggleLanguage
            )
        }
    }
}
