package com.citra.reverie.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Category(
    val id: String,
    val name: String,
    val color: Color,
    val icon: ImageVector
)
