package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldAccent
import kotlin.random.Random

@Composable
fun RecitationWaveform(
    audioRms: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 18
) {
    Row(
        modifier = modifier.height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val baseRms = if (isListening) audioRms.coerceIn(0.05f, 1.0f) else 0.05f

        for (i in 0 until barCount) {
            // Give varied heights around the center for visual waveform realism
            val centerDist = kotlin.math.abs(i - barCount / 2f) / (barCount / 2f)
            val factor = (1f - centerDist * 0.45f) * (0.6f + (Random(i * 13).nextFloat() * 0.8f))
            val targetHeight = if (isListening) {
                (baseRms * factor).coerceIn(0.12f, 1.0f)
            } else {
                0.12f
            }

            val animatedHeight by animateFloatAsState(
                targetValue = targetHeight,
                animationSpec = tween(durationMillis = 180),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .fillMaxHeight(animatedHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isListening) EmeraldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
