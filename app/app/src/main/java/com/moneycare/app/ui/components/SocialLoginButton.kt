package com.moneycare.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.theme.OutlineSoft

enum class SocialProvider { GOOGLE, FACEBOOK, APPLE }

/**
 * Small circular outlined icon button used for social sign-in (Google,
 * Facebook, Apple). Provide the brand icon via [icon]; a real app would
 * likely swap this for brand-accurate vector assets.
 */
@Composable
fun SocialLoginButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "socialButtonScale"
    )

    OutlinedIconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .size(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = CircleShape,
        border = BorderStroke(1.dp, OutlineSoft),
        colors = IconButtonDefaults.outlinedIconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp))
    }
}

/** Row of the three standard social login options, spaced evenly. */
@Composable
fun SocialLoginRow(
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    onAppleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // NOTE: Google/Facebook/Apple brand marks should use real vector assets
    // (res/drawable/ic_google.xml etc.) for production. Generic Material
    // icons are used here as safe placeholders that compile out of the box.
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SocialLoginButton(
            icon = Icons.Filled.Public,
            contentDescription = "Đăng nhập với Google",
            onClick = onGoogleClick
        )
        SocialLoginButton(
            icon = Icons.Filled.Chat,
            contentDescription = "Đăng nhập với Facebook",
            onClick = onFacebookClick
        )
        SocialLoginButton(
            icon = Icons.Filled.PhoneIphone,
            contentDescription = "Đăng nhập với Apple",
            onClick = onAppleClick
        )
    }
}
