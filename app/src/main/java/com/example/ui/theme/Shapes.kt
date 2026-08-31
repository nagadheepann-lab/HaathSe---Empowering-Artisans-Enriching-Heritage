package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// =======================================================================
// HAATHSE SHAPES SYSTEM
// Indian craft-inspired soft contours, elevated cards, and tactile buttons
// =======================================================================

val HaathSeShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Reusable custom shape tokens
val ButtonShape = RoundedCornerShape(14.dp)
val CardShape = RoundedCornerShape(16.dp)
val DialogShape = RoundedCornerShape(24.dp)
val PillShape = RoundedCornerShape(50)
val BadgeShape = RoundedCornerShape(8.dp)
val ImageCardShape = RoundedCornerShape(14.dp)
