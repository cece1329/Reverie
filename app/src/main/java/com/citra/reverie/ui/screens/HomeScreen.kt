package com.citra.reverie.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.citra.reverie.data.AppStrings
import com.citra.reverie.data.AudiusApiService
import com.citra.reverie.data.LanguageManager
import com.citra.reverie.model.Artist
import com.citra.reverie.model.Playlist
import com.citra.reverie.model.Track
import com.citra.reverie.ui.components.AddCustomTrackDialog
import com.citra.reverie.ui.components.AestheticMusicHeader
import com.citra.reverie.ui.components.SelectPlaylistDialog
import com.citra.reverie.ui.components.TrackItem
import java.util.Calendar

@Composable
fun HomeScreen(
    currentTrack: Track?,
    isPlaying: Boolean,
    likedTracks: List<Track>,
    customPlaylists: List<Playlist> = emptyList(),
    onTrackSelect: (Track, List<Track>) -> Unit,
    onLikeToggle: (Track) -> Unit,
    onAddTrackToPlaylist: (playlistId: String, track: Track) -> Unit = { _, _ -> },
    onAddCustomTrack: (title: String, artist: String, coverUrl: String, songUrl: String) -> Unit,
    onAddLocalTrack: (title: String, artist: String, coverUrl: String, fileUri: String) -> Unit = { _, _, _, _ -> },
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit = {},
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedFilterKey by remember { mutableStateOf("genre_all") }
    val filterKeys = listOf("genre_all", "genre_rock", "genre_pop", "genre_jazz", "genre_indie", "genre_electronic", "genre_rnb")

    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showAddTrackDialog by remember { mutableStateOf(false) }
    var selectedTrackForPlaylist by remember { mutableStateOf<Track?>(null) }

    if (selectedPlaylist != null) {
        PlaylistDetailScreen(
            playlist = selectedPlaylist!!,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            likedTracks = likedTracks,
            onTrackSelect = onTrackSelect,
            onLikeToggle = onLikeToggle,
            onBack = { selectedPlaylist = null },
            modifier = modifier
        )
        return
    }

    val audiusService = remember { AudiusApiService() }
    var allTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFilterKey) {
        isLoading = true
        try {
            val genreName = AppStrings.get(selectedFilterKey)
            val tracks = audiusService.fetchTracksByGenre(genreName)
            if (tracks.isNotEmpty()) {
                allTracks = tracks
            }
        } catch (_: Exception) {
            // Keep current tracks
        } finally {
            isLoading = false
        }
    }

    val greeting = remember(LanguageManager.currentLanguage) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> AppStrings.get("greeting_morning")
            in 12..16 -> AppStrings.get("greeting_afternoon")
            in 17..19 -> AppStrings.get("greeting_evening")
            else -> AppStrings.get("greeting_night")
        }
    }

    val likedTrackIds = remember(likedTracks) { likedTracks.map { it.id }.toSet() }

    val featuredPlaylists = remember(allTracks) {
        if (allTracks.isNotEmpty()) {
            listOf(
                Playlist(
                    id = "pl_featured_1",
                    name = "Hits Pop Reverie",
                    description = "Lagu-lagu terpopuler dan terhangat dari musisi pilihan",
                    coverUrl = allTracks.firstOrNull()?.coverUrl ?: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
                    trackCount = allTracks.take(8).size,
                    tracks = allTracks.take(8)
                ),
                Playlist(
                    id = "pl_featured_2",
                    name = "Indie & Chill",
                    description = "Iringan melodi santai untuk menemani harimu",
                    coverUrl = allTracks.getOrNull(2)?.coverUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
                    trackCount = allTracks.drop(3).take(6).size,
                    tracks = allTracks.drop(3).take(6)
                ),
                Playlist(
                    id = "pl_featured_3",
                    name = "Top Viral Global",
                    description = "Lagu-lagu yang sedang trending di seluruh dunia",
                    coverUrl = allTracks.getOrNull(4)?.coverUrl ?: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
                    trackCount = allTracks.drop(6).take(6).size,
                    tracks = allTracks.drop(6).take(6)
                )
            ).filter { it.tracks.isNotEmpty() }
        } else emptyList()
    }

    val popularArtists = remember(allTracks) {
        allTracks
            .distinctBy { it.artist }
            .take(6)
            .mapIndexed { idx, track ->
                Artist(
                    id = "art_$idx",
                    name = track.artist,
                    imageUrl = track.coverUrl,
                    genre = track.genre.ifBlank { "Music" }
                )
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ESTETIK MUSIC HEADER
        AestheticMusicHeader(
            greeting = greeting,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            isLoading = isLoading,
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            onToggleLanguage = onToggleLanguage,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // FILTER CHIPS + TOMBOL TAMBAH LAGU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterKeys.forEach { filterKey ->
                        val isSelected = selectedFilterKey == filterKey
                        AssistChip(
                            onClick = { selectedFilterKey = filterKey },
                            label = {
                                Text(
                                    text = AppStrings.get(filterKey),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = null,
                            shape = RoundedCornerShape(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Tombol + Tambah Lagu Sendiri
                Button(
                    onClick = { showAddTrackDialog = true },
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.get("btn_add_music"), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // FEATURED PLAYLIST
            if (featuredPlaylists.isNotEmpty()) {
                Column {
                    SectionHeader(title = AppStrings.get("section_playlists"))
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(end = 4.dp)
                    ) {
                        items(featuredPlaylists, key = { it.id }) { playlist ->
                            PlaylistCard(playlist = playlist, onClick = {
                                selectedPlaylist = playlist
                            })
                        }
                    }
                }
            }

            // ARTIS POPULER
            if (popularArtists.isNotEmpty()) {
                Column {
                    SectionHeader(title = AppStrings.get("section_popular_artists"))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        popularArtists.forEach { artist ->
                            ArtistItem(artist = artist, onClick = {
                                val artistTracks = allTracks.filter {
                                    it.artist.contains(artist.name, ignoreCase = true)
                                }
                                if (artistTracks.isNotEmpty()) {
                                    onTrackSelect(artistTracks.first(), artistTracks)
                                }
                            })
                        }
                    }
                }
            }

            // DAFTAR LAGU
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = AppStrings.get("section_top_songs"))
                    Text(
                        text = "${allTracks.size} ${AppStrings.get("songs_count")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppStrings.get("loading_api"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (allTracks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppStrings.get("no_songs_genre"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allTracks.forEach { track ->
                            TrackItem(
                                track = track,
                                isCurrentTrack = currentTrack?.id == track.id,
                                isPlaying = isPlaying && currentTrack?.id == track.id,
                                isLiked = likedTrackIds.contains(track.id),
                                onClick = { onTrackSelect(track, allTracks) },
                                onLikeToggle = { onLikeToggle(track) },
                                onAddToPlaylist = { selectedTrackForPlaylist = it }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Dialog Tambah Lagu
    if (showAddTrackDialog) {
        AddCustomTrackDialog(
            onDismiss = { showAddTrackDialog = false },
            onAddTrack = { title, artist, coverUrl, songUrl ->
                onAddCustomTrack(title, artist, coverUrl, songUrl)
                showAddTrackDialog = false
            },
            onAddLocalTrack = { title, artist, coverUrl, fileUri ->
                onAddLocalTrack(title, artist, coverUrl, fileUri)
                showAddTrackDialog = false
            }
        )
    }

    // Dialog Tambah ke Playlist
    if (selectedTrackForPlaylist != null) {
        SelectPlaylistDialog(
            track = selectedTrackForPlaylist!!,
            playlists = customPlaylists,
            onDismiss = { selectedTrackForPlaylist = null },
            onSelectPlaylist = { playlistId ->
                onAddTrackToPlaylist(playlistId, selectedTrackForPlaylist!!)
                selectedTrackForPlaylist = null
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "playlist_scale"
    )

    Surface(
        modifier = Modifier
            .width(165.dp)
            .height(205.dp)
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .clickable {
                pressed = true
                onClick()
                pressed = false
            },
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        tonalElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = playlist.coverUrl,
                contentDescription = playlist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(10.dp)
                    .size(34.dp)
                    .align(Alignment.TopEnd)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = AppStrings.get("play"),
                        tint = Color.Black,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.trackCount} ${AppStrings.get("songs_count")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ArtistItem(artist: Artist, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(84.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = artist.genre,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
