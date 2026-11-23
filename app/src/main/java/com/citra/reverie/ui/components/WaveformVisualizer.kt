package com.citra.reverie.ui.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    maxHeight: Dp = 18.dp,
    barWidth: Dp = 3.dp,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val heights = List(barCount) { index ->
        val duration = 400 + (index * 150)
        val target = if (isPlaying) 0.3f + (index * 0.18f).coerceAtMost(0.7f) else 0.2f
        val animatedValue by infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = if (isPlaying) target + 0.3f else 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
        animatedValue
    }

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { heightFactor ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(heightFactor.coerceIn(0.15f, 1.0f))
                    .background(barColor, shape = RoundedCornerShape(2.dp))
            )
        }
    }
}
