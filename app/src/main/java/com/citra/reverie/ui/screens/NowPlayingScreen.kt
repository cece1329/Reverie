package com.citra.reverie.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.citra.reverie.data.AppStrings
import com.citra.reverie.model.Track
import com.citra.reverie.player.RepeatMode
import com.citra.reverie.ui.components.TrackItem
import com.citra.reverie.ui.theme.ReverieTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    track: Track?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    shuffleMode: Boolean,
    repeatMode: RepeatMode,
    isLiked: Boolean,
    queue: List<Track>,
    isBuffering: Boolean = false,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: (Track) -> Unit,
    onTrackSelect: (Track) -> Unit,
    onCollapseClick: () -> Unit,
    onEnterPiP: () -> Unit = {},
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    var showQueueSheet by rememberSaveable { mutableStateOf(false) }
    var showLyricsView by rememberSaveable { mutableStateOf(false) }

    // Swipe gesture untuk next/previous
    var swipeDelta by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeDelta < -80f) onNextClick()
                        else if (swipeDelta > 80f) onPreviousClick()
                        swipeDelta = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        swipeDelta += dragAmount
                    }
                )
            }
    ) {
        // Blurred background artwork
        AsyncImage(
            model = track.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(100.dp)
        )

        // Deep gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.5f),
                        0.4f to Color.Black.copy(alpha = 0.75f),
                        1f to Color.Black.copy(alpha = 0.97f)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapseClick) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Tutup",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                AnimatedContent(
                    targetState = showLyricsView to showQueueSheet,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { (lyrics, queue) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when {
                                lyrics -> AppStrings.get("lyrics")
                                queue -> AppStrings.get("queue")
                                else -> AppStrings.get("now_playing")
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = track.album,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tombol Mode Melayang (PiP)
                    IconButton(onClick = onEnterPiP) {
                        Icon(
                            Icons.Filled.PictureInPictureAlt,
                            contentDescription = AppStrings.get("pip_mode"),
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Tombol Lirik
                    IconButton(onClick = {
                        showLyricsView = !showLyricsView
                        if (showLyricsView) showQueueSheet = false
                    }) {
                        Icon(
                            Icons.Filled.Lyrics,
                            contentDescription = AppStrings.get("lyrics"),
                            tint = if (showLyricsView) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Tombol Antrean
                    IconButton(onClick = {
                        showQueueSheet = !showQueueSheet
                        if (showQueueSheet) showLyricsView = false
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = AppStrings.get("queue"),
                            tint = if (showQueueSheet) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Content area
            when {
                showQueueSheet -> {
                    QueueSheet(
                        queue = queue,
                        currentTrack = track,
                        isPlaying = isPlaying,
                        onTrackSelect = onTrackSelect,
                        onToggleLike = onToggleLike,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    )
                }
                showLyricsView -> {
                    LyricsSheet(
                        track = track,
                        currentPositionMs = progressMs,
                        onSeekTo = onSeekTo,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    )
                }
                else -> {
                    // Main player view
                    MainPlayerContent(
                        track = track,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        progressMs = progressMs,
                        durationMs = durationMs,
                        isLiked = isLiked,
                        shuffleMode = shuffleMode,
                        repeatMode = repeatMode,
                        onPlayPauseClick = onPlayPauseClick,
                        onNextClick = onNextClick,
                        onPreviousClick = onPreviousClick,
                        onSeekTo = onSeekTo,
                        onToggleShuffle = onToggleShuffle,
                        onToggleRepeat = onToggleRepeat,
                        onToggleLike = { onToggleLike(track) },
                        isLandscape = isLandscape,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MainPlayerContent(
    track: Track,
    isPlaying: Boolean,
    isBuffering: Boolean,
    progressMs: Long,
    durationMs: Long,
    isLiked: Boolean,
    shuffleMode: Boolean,
    repeatMode: RepeatMode,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: () -> Unit,
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            val artworkScale by animateFloatAsState(
                targetValue = if (isPlaying) 1f else 0.9f,
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                label = "artwork_scale_ls"
            )

            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(artworkScale)
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isBuffering && !isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onToggleLike) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SeekBarSection(
                    progressMs = progressMs,
                    durationMs = durationMs,
                    onSeekTo = onSeekTo
                )

                Spacer(modifier = Modifier.height(8.dp))

                ControlsSection(
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    shuffleMode = shuffleMode,
                    repeatMode = repeatMode,
                    onPlayPauseClick = onPlayPauseClick,
                    onNextClick = onNextClick,
                    onPreviousClick = onPreviousClick,
                    onToggleShuffle = onToggleShuffle,
                    onToggleRepeat = onToggleRepeat
                )
            }
        }
    } else {
        Column(
            modifier = modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

        // Artwork yang besar dengan animasi scale saat playing
        val artworkScale by animateFloatAsState(
            targetValue = if (isPlaying) 1f else 0.88f,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "artwork_scale"
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(artworkScale)
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Buffering overlay
            if (isBuffering && !isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Track info + like button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = track.title,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                    }
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AnimatedContent(
                    targetState = track.artist,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                    }
                ) { artist ->
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Like button dengan animasi
            val likeScale by animateFloatAsState(
                targetValue = if (isLiked) 1.15f else 1f,
                animationSpec = tween(200),
                label = "like_scale"
            )
            IconButton(onClick = onToggleLike, modifier = Modifier.scale(likeScale)) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorit",
                    tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Seek bar
        SeekBarSection(
            progressMs = progressMs,
            durationMs = durationMs,
            onSeekTo = onSeekTo
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        ControlsSection(
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            shuffleMode = shuffleMode,
            repeatMode = repeatMode,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onToggleShuffle = onToggleShuffle,
            onToggleRepeat = onToggleRepeat
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
}

@Composable
private fun SeekBarSection(
    progressMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }

    val displayValue = if (isDragging) dragPosition else progressMs.toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = displayValue,
            onValueChange = { newValue ->
                if (!isDragging) isDragging = true
                dragPosition = newValue
            },
            onValueChangeFinished = {
                onSeekTo(dragPosition.toLong())
                isDragging = false
            },
            valueRange = 0f..(durationMs.toFloat().coerceAtLeast(1f)),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatMs(if (isDragging) dragPosition.toLong() else progressMs),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = formatMs(durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ControlsSection(
    isPlaying: Boolean,
    isBuffering: Boolean,
    shuffleMode: Boolean,
    repeatMode: RepeatMode,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        IconButton(onClick = onToggleShuffle) {
            Icon(
                Icons.Filled.Shuffle,
                contentDescription = AppStrings.get("shuffle"),
                tint = if (shuffleMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }

        // Previous
        IconButton(
            onClick = onPreviousClick,
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                Icons.Filled.SkipPrevious,
                contentDescription = AppStrings.get("previous"),
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        // Play/Pause — tombol utama
        Surface(
            onClick = onPlayPauseClick,
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(68.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (isBuffering && !isPlaying) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) AppStrings.get("pause") else AppStrings.get("play"),
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Next
        IconButton(
            onClick = onNextClick,
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                Icons.Filled.SkipNext,
                contentDescription = AppStrings.get("next"),
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        // Repeat
        IconButton(onClick = onToggleRepeat) {
            Icon(
                imageVector = if (repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                contentDescription = AppStrings.get("repeat"),
                tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun QueueSheet(
    queue: List<Track>,
    currentTrack: Track,
    isPlaying: Boolean,
    onTrackSelect: (Track) -> Unit,
    onToggleLike: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "${AppStrings.get("queue")} • ${queue.size} ${AppStrings.get("songs_count")}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(queue, key = { _, it -> it.id }) { index, item ->
                TrackItem(
                    track = item,
                    isCurrentTrack = item.id == currentTrack.id,
                    isPlaying = isPlaying && item.id == currentTrack.id,
                    isLiked = false,
                    onClick = { onTrackSelect(item) },
                    onLikeToggle = { onToggleLike(item) }
                )
            }
        }
    }
}

@Composable
private fun LyricsSheet(
    track: Track,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val syncedLyrics = remember(track) { track.getEffectiveSyncedLyrics() }
    val listState = rememberLazyListState()

    val activeIndex = remember(syncedLyrics, currentPositionMs) {
        val idx = syncedLyrics.indexOfLast { currentPositionMs >= it.timeMs }
        if (idx >= 0) idx else 0
    }

    LaunchedEffect(activeIndex) {
        if (syncedLyrics.isNotEmpty() && activeIndex in syncedLyrics.indices) {
            listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Live Sync",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Text(
            text = AppStrings.get("lyrics_synced"),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (syncedLyrics.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AppStrings.get("no_lyrics"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(syncedLyrics) { index, line ->
                    val isActive = index == activeIndex
                    val isPast = index < activeIndex

                    val textColor = when {
                        isActive -> MaterialTheme.colorScheme.primary
                        isPast -> Color.White.copy(alpha = 0.75f)
                        else -> Color.White.copy(alpha = 0.35f)
                    }

                    val fontSize = if (isActive) 22.sp else 18.sp
                    val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold

                    Text(
                        text = line.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = fontWeight,
                            fontSize = fontSize,
                            lineHeight = 30.sp
                        ),
                        color = textColor,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onSeekTo(line.timeMs)
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSecs = (ms / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format(Locale.getDefault(), "%d:%02d", mins, secs)
}

@Preview(showBackground = true, name = "Now Playing Screen Preview")
@Composable
fun NowPlayingScreenPreview() {
    val sampleTrack = Track(
        id = "prev_1",
        title = "Reverie Song",
        artist = "Artist Name",
        album = "Album",
        coverUrl = "",
        audioUrl = "",
        durationMs = 210000L
    )
    ReverieTheme(darkTheme = true) {
        Surface {
            NowPlayingScreen(
                track = sampleTrack,
                isPlaying = true,
                progressMs = 45000L,
                durationMs = 210000L,
                shuffleMode = false,
                repeatMode = RepeatMode.OFF,
                isLiked = true,
                queue = listOf(sampleTrack),
                isBuffering = false,
                onPlayPauseClick = {},
                onNextClick = {},
                onPreviousClick = {},
                onSeekTo = {},
                onToggleShuffle = {},
                onToggleRepeat = {},
                onToggleLike = {},
                onTrackSelect = {},
                onCollapseClick = {}
            )
        }
    }
}
