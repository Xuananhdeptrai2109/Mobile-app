package com.moneycare.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.theme.CoralAccent
import com.moneycare.app.ui.theme.MintContainer
import com.moneycare.app.ui.theme.MintPrimary
import com.moneycare.app.ui.theme.PeachAccent
import com.moneycare.app.ui.theme.SkyBlue

/**
 * Hero illustration for the Welcome screen: the "dino hugging a heart"
 * mascot, floating clouds, a speech-bubble heart, and leaf accents —
 * built from simple vector primitives so it renders with zero binary
 * assets. Swap the center [Icon] composition for a real illustration or
 * Lottie file (`res/raw/welcome_mascot.json`) once art is ready; see the
 * kdoc on [AuthIllustration] for the Lottie wiring snippet.
 */
@Composable
fun WelcomeIllustration(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcomeMascotFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcomeMascotOffsetY"
    )
    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudDrift"
    )

    Box(modifier = modifier.size(240.dp), contentAlignment = Alignment.Center) {

        // Drifting clouds, top corners
        Icon(
            imageVector = Icons.Filled.Cloud,
            contentDescription = null,
            tint = SkyBlue.copy(alpha = 0.5f),
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.TopStart)
                .graphicsLayer { translationX = cloudDrift; translationY = -cloudDrift }
        )
        Icon(
            imageVector = Icons.Filled.Cloud,
            contentDescription = null,
            tint = SkyBlue.copy(alpha = 0.35f),
            modifier = Modifier
                .size(26.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { translationX = -cloudDrift; translationY = cloudDrift }
        )

        // Leaf accents, either side of the mascot
        Icon(
            imageVector = Icons.Filled.Park,
            contentDescription = null,
            tint = MintPrimary.copy(alpha = 0.55f),
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.CenterStart)
        )
        Icon(
            imageVector = Icons.Filled.Park,
            contentDescription = null,
            tint = MintPrimary.copy(alpha = 0.4f),
            modifier = Modifier
                .size(22.dp)
                .align(Alignment.BottomEnd)
        )

        // Main mascot body — floating circle with heart hug
        Box(
            modifier = Modifier
                .size(176.dp)
                .graphicsLayer { translationY = floatOffset }
                .background(color = MintContainer, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(color = MintPrimary.copy(alpha = 0.18f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = CoralAccent,
                    modifier = Modifier.size(56.dp)
                )
            }

            // Speech-bubble heart, top-right of the mascot
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .offset(x = 62.dp, y = (-62).dp)
                    .background(color = androidx.compose.ui.graphics.Color.White, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = CoralAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Small coin/cheek accent, bottom-left
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = (-58).dp, y = 58.dp)
                    .background(color = PeachAccent, shape = CircleShape)
            )
        }
    }
}
