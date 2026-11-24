package com.citra.reverie.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.ui.graphics.Color
import com.citra.reverie.model.Category

object DummyMusicRepository {

    val sampleCategories = listOf(
        Category("cat_1", "Pop", Color(0xFF6C5CE7), Icons.Filled.MusicNote),
        Category("cat_2", "Indie", Color(0xFF00CEC9), Icons.Filled.Headphones),
        Category("cat_3", "Rock", Color(0xFFFF7675), Icons.Filled.GraphicEq),
        Category("cat_4", "Electronic", Color(0xFF0984E3), Icons.Filled.Album),
        Category("cat_5", "Jazz", Color(0xFFFDCB6E), Icons.Filled.Piano),
        Category("cat_6", "Acoustic", Color(0xFFE84393), Icons.Filled.Mic)
    )
}
