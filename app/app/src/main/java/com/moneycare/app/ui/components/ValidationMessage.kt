package com.moneycare.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.theme.MoneyCareSpacing
import com.moneycare.app.ui.theme.SoftGreenSuccess
import com.moneycare.app.ui.theme.SoftRed

/**
 * Friendly, animated inline message shown below a field. Slides down and
 * fades in so the surrounding layout doesn't jump. Used for both error and
 * "looks good" success states to keep tone consistent and non-scary.
 */
@Composable
fun ValidationMessage(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message.isNotBlank(),
        enter = fadeIn() + expandVertically() + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut() + shrinkVertically() + slideOutVertically(targetOffsetY = { -it / 2 })
    ) {
        Row(
            modifier = modifier.padding(top = MoneyCareSpacing.XXS, start = MoneyCareSpacing.XS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Filled.ErrorOutline else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = if (isError) SoftRed else SoftGreenSuccess,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) SoftRed else SoftGreenSuccess
            )
        }
    }
}
