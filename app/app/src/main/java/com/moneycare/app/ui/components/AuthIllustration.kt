package com.moneycare.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.theme.MintContainer
import com.moneycare.app.ui.theme.MintPrimary
import com.moneycare.app.ui.theme.PeachAccent

enum class AuthIllustrationType { LOGIN, REGISTER }

/**
 * Cute mascot placeholder shown on the auth screens. In production this
 * would be swapped for a Lottie animation (see [AuthIllustrationLottie]
 * below) or a hand-drawn SVG matching the reference art (piggy-bank /
 * plant / coin mascot). Here it's a lightweight vector composition with a
 * gentle floating animation, so the screen still feels alive without
 * bundling binary assets.
 *
 * - LOGIN: mascot appears to "wave hello" — welcoming the user back.
 * - REGISTER: mascot appears to hold a coin — starting a new journey.
 */
@Composable
fun AuthIllustration(
    type: AuthIllustrationType,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascotFloat")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotOffsetY"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { translationY = offsetY }
            .background(color = MintContainer, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.62f)
                .background(color = MintPrimary.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (type == AuthIllustrationType.LOGIN) Icons.Filled.Savings else Icons.Filled.Celebration,
                contentDescription = null,
                tint = MintPrimary,
                modifier = Modifier.size(size * 0.4f)
            )
        }
        // Small accent "coin" / "leaf" detail
        Box(
            modifier = Modifier
                .size(size * 0.18f)
                .graphicsLayer { translationX = size.value * 0.55f; translationY = -size.value * 0.35f }
                .background(color = PeachAccent, shape = CircleShape)
        )
    }
}

/**
 * Drop-in replacement for [AuthIllustration] once real Lottie assets are
 * added to the project (e.g. res/raw/mascot_hello.json,
 * res/raw/mascot_plant_money.json). Requires the `com.airbnb.android:lottie-compose`
 * dependency.
 *
 * Example usage once wired up:
 * ```
 * val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.mascot_hello))
 * val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
 * LottieAnimation(composition, progress, modifier = Modifier.size(160.dp))
 * ```
 */
@Composable
fun AuthIllustrationLottiePlaceholderNote() {
    // Intentionally left as documentation only — see kdoc above.
}
