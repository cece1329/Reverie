package com.citra.reverie.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.saveable.rememberSaveable
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

    var isDarkMode by rememberSaveable { mutableStateOf(true) }
    var currentTab by rememberSaveable { mutableStateOf(ReverieTab.HOME) }
    var isNowPlayingExpanded by rememberSaveable { mutableStateOf(false) }

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
    val customTracks by playerManager.customTracks.collectAsState()
    val userProfile by playerManager.userProfile.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    ReverieTheme(darkTheme = isDarkMode) {
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
                        onLogin = { u, e, p ->
                            playerManager.login(u, e, p)
                            currentTab = ReverieTab.HOME
                        },
                        onRegister = { u, e, p ->
                            playerManager.register(u, e, p)
                            currentTab = ReverieTab.HOME
                        },
                        onLogout = { playerManager.logout() },
                        onToggleTheme = { isDarkMode = !isDarkMode },
                        isDarkMode = isDarkMode,
                        onToggleLanguage = { LanguageManager.toggleLanguage() }
                    )
                }
            }
        } else {
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
                                customTracks = customTracks,
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
                                onDeleteCustomTrack = { trackId ->
                                    playerManager.deleteCustomTrack(trackId)
                                },
                                onClearHistory = {
                                    playerManager.clearHistory()
                                },
                                onUpdateProfile = { u, e ->
                                    playerManager.updateUserProfile(u, e)
                                },
                                onLogin = { u, e, p ->
                                    playerManager.login(u, e, p)
                                    currentTab = ReverieTab.HOME
                                },
                                onRegister = { u, e, p ->
                                    playerManager.register(u, e, p)
                                    currentTab = ReverieTab.HOME
                                },
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
                                customTracks = customTracks,
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
                                onDeleteCustomTrack = { trackId ->
                                    playerManager.deleteCustomTrack(trackId)
                                },
                                onClearHistory = {
                                    playerManager.clearHistory()
                                },
                                onUpdateProfile = { u, e ->
                                    playerManager.updateUserProfile(u, e)
                                },
                                onLogin = { u, e, p ->
                                    playerManager.login(u, e, p)
                                    currentTab = ReverieTab.HOME
                                },
                                onRegister = { u, e, p ->
                                    playerManager.register(u, e, p)
                                    currentTab = ReverieTab.HOME
                                },
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
}

@Composable
private fun MainContentScreen(
    currentTab: ReverieTab,
    currentTrack: Track?,
    isPlaying: Boolean,
    likedTracks: List<Track>,
    customTracks: List<Track> = emptyList(),
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
    onDeleteCustomTrack: ((String) -> Unit)? = null,
    onClearHistory: () -> Unit,
    onUpdateProfile: (username: String, email: String) -> Unit,
    onLogin: (String, String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    AnimatedContent(
        targetState = currentTab,
        transitionSpec = {
            val isForward = targetState.ordinal > initialState.ordinal
            val enterAnim = if (isForward) {
                slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> width / 4 } + fadeIn(animationSpec = tween(300))
            } else {
                slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> -width / 4 } + fadeIn(animationSpec = tween(300))
            }
            val exitAnim = if (isForward) {
                slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> -width / 4 } + fadeOut(animationSpec = tween(200))
            } else {
                slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> width / 4 } + fadeOut(animationSpec = tween(200))
            }
            enterAnim togetherWith exitAnim
        },
        label = "NavbarTabAnimation"
    ) { targetTab ->
        when (targetTab) {
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
                    customTracks = customTracks,
                    history = history,
                    customPlaylists = customPlaylists,
                    onCreatePlaylist = onCreatePlaylist,
                    onDeletePlaylist = onDeletePlaylist,
                    onAddTrackToPlaylist = onAddTrackToPlaylist,
                    onAddCustomTrack = onAddCustomTrack,
                    onAddLocalTrack = onAddLocalTrack,
                    onDeleteCustomTrack = onDeleteCustomTrack,
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
}
