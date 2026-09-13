package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.WordAlignmentState
import com.example.domain.model.WordRecitationStatus
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenBg
import com.example.ui.theme.CorrectGreenBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MistakeAmber
import com.example.ui.theme.MistakeBg
import com.example.ui.theme.MistakeBorder
import com.example.ui.theme.MistakeRed
import com.example.ui.theme.QuranWordTypography
import com.example.ui.theme.RepeatedBg
import com.example.ui.theme.RepeatedBlue
import com.example.ui.theme.SkippedBg
import com.example.ui.theme.SkippedGray

@Composable
fun QuranWordView(
    state: WordAlignmentState,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val status = state.status

    // Pulsing scale animation for active word
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isCurrent) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            status == WordRecitationStatus.CORRECT -> CorrectGreenBg.copy(alpha = 0.5f)
            status == WordRecitationStatus.MISTAKE -> MistakeBg.copy(alpha = 0.7f)
            status == WordRecitationStatus.SKIPPED -> SkippedBg.copy(alpha = 0.4f)
            status == WordRecitationStatus.REPEATED -> RepeatedBg.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = tween(250),
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isCurrent -> GoldPrimary
            status == WordRecitationStatus.CORRECT -> CorrectGreenBorder.copy(alpha = 0.7f)
            status == WordRecitationStatus.MISTAKE -> MistakeBorder
            status == WordRecitationStatus.SKIPPED -> SkippedGray.copy(alpha = 0.3f)
            status == WordRecitationStatus.REPEATED -> RepeatedBlue.copy(alpha = 0.6f)
            else -> Color.Transparent
        },
        animationSpec = tween(250),
        label = "borderColor"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isCurrent -> MaterialTheme.colorScheme.onBackground
            status == WordRecitationStatus.CORRECT -> CorrectGreen
            status == WordRecitationStatus.MISTAKE -> MistakeAmber
            status == WordRecitationStatus.SKIPPED -> SkippedGray
            status == WordRecitationStatus.REPEATED -> RepeatedBlue
            else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
        },
        animationSpec = tween(250),
        label = "textColor"
    )

    val textDecoration = if (status == WordRecitationStatus.SKIPPED) {
        TextDecoration.LineThrough
    } else {
        TextDecoration.None
    }

    Box(
        modifier = modifier
            .testTag("word_${state.word.id}")
            .scale(if (isCurrent) pulseScale else 1.0f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isCurrent) 1.5.dp else if (status != WordRecitationStatus.PENDING) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = state.word.textUthmani,
            style = QuranWordTypography.copy(
                fontSize = 22.sp,
                color = textColor,
                textDecoration = textDecoration
            )
        )
    }
}
