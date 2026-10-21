package com.moneycare.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Design system corner radii
object MoneyCareRadius {
    val Small = 12.dp
    val Medium = 16.dp
    val Large = 24.dp
    val ExtraLarge = 32.dp
}

val MoneyCareShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(MoneyCareRadius.Small),
    medium = RoundedCornerShape(MoneyCareRadius.Medium),
    large = RoundedCornerShape(MoneyCareRadius.Large),
    extraLarge = RoundedCornerShape(MoneyCareRadius.ExtraLarge)
)
