package com.citra.reverie.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.ui.graphics.Color
import com.citra.reverie.model.Artist
import com.citra.reverie.model.Category
import com.citra.reverie.model.Playlist
import com.citra.reverie.model.Track

object DummyMusicRepository {

    val popularTracks = emptyList<Track>()

    val sampleTracks = emptyList<Track>()

    val samplePlaylists = emptyList<Playlist>()

    val sampleCategories = listOf(
        Category("cat_1", "Pop", Color(0xFF6C5CE7), Icons.Filled.MusicNote),
        Category("cat_2", "Indie", Color(0xFF00CEC9), Icons.Filled.Headphones),
        Category("cat_3", "Rock", Color(0xFFFF7675), Icons.Filled.GraphicEq),
        Category("cat_4", "Electronic", Color(0xFF0984E3), Icons.Filled.Album),
        Category("cat_5", "Jazz", Color(0xFFFDCB6E), Icons.Filled.Piano),
        Category("cat_6", "Acoustic", Color(0xFFE84393), Icons.Filled.Mic)
    )

    val popularArtists = listOf(
        Artist("art_1", "Bernadya", "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/d8/bb/8c/d8bb8c81-ddb9-e427-a18b-c385afae12c2/23UMGIM65225.rgb.jpg/600x600bb.jpg", "Pop"),
        Artist("art_2", "Tulus", "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/6f/18/10/6f1810e0-2d6a-a456-1362-de607c70408a/cover.jpg/600x600bb.jpg", "Pop"),
        Artist("art_3", "Nadin Amizah", "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/c1/b0/f7/c1b0f7fa-f141-7c53-9531-0b248b06e96c/cover.jpg/600x600bb.jpg", "Indie"),
        Artist("art_4", "Sal Priadi", "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/fa/a8/4c/faa84c4f-0f09-3730-6460-8c53e9dc159d/cover.jpg/600x600bb.jpg", "Indie"),
        Artist("art_5", "Lady Gaga", "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/11/ae/f2/11aef294-f57c-bab9-c9fc-529162984e62/24UMGIM85348.rgb.jpg/600x600bb.jpg", "Pop")
    )
}
