package com.moneycare.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.theme.CoralAccent
import com.moneycare.app.ui.theme.MintPrimary
import com.moneycare.app.ui.theme.MintPrimaryLight
import com.moneycare.app.ui.theme.SoftYellow

/**
 * Soft rolling "hills" silhouette with a couple of flower dots, used as a
 * lightweight footer decoration on the Welcome screen (mirrors the grass +
 * flowers detail in the reference art). Pure Canvas drawing — no image
 * assets required.
 */
@Composable
fun DecorativeGroundStrip(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val width = size.width
        val height = size.height

        // Left hill
        val leftHill = Path().apply {
            moveTo(0f, height)
            lineTo(0f, height * 0.55f)
            quadraticBezierTo(width * 0.18f, height * 0.05f, width * 0.38f, height * 0.5f)
            lineTo(width * 0.38f, height)
            close()
        }
        drawPath(leftHill, color = MintPrimaryLight.copy(alpha = 0.55f), style = Fill)

        // Right hill
        val rightHill = Path().apply {
            moveTo(width, height)
            lineTo(width, height * 0.5f)
            quadraticBezierTo(width * 0.82f, height * 0.05f, width * 0.62f, height * 0.55f)
            lineTo(width * 0.62f, height)
            close()
        }
        drawPath(rightHill, color = MintPrimary.copy(alpha = 0.4f), style = Fill)

        // Flower dots
        drawCircle(color = CoralAccent, radius = 5.dp.toPx(), center = Offset(width * 0.12f, height * 0.62f))
        drawCircle(color = SoftYellow, radius = 4.dp.toPx(), center = Offset(width * 0.88f, height * 0.6f))
        drawCircle(color = CoralAccent.copy(alpha = 0.7f), radius = 3.dp.toPx(), center = Offset(width * 0.9f, height * 0.72f))
    }
}
