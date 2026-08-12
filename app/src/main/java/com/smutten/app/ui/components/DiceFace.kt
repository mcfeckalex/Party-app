package com.smutten.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun DiceFace(value: Int, modifier: Modifier = Modifier) {
    val pipColor = MaterialTheme.colorScheme.onPrimary
    val dieColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .background(dieColor, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        val radius = size.minDimension / 9f
        pipOffsets(value).forEach { (fx, fy) ->
            drawCircle(
                color = pipColor,
                radius = radius,
                center = Offset(size.width * fx, size.height * fy)
            )
        }
    }
}

private fun pipOffsets(value: Int): List<Pair<Float, Float>> {
    val center = 0.5f to 0.5f
    val topLeft = 0.22f to 0.22f
    val topRight = 0.78f to 0.22f
    val bottomLeft = 0.22f to 0.78f
    val bottomRight = 0.78f to 0.78f
    val midLeft = 0.22f to 0.5f
    val midRight = 0.78f to 0.5f

    return when (value) {
        1 -> listOf(center)
        2 -> listOf(topLeft, bottomRight)
        3 -> listOf(topLeft, center, bottomRight)
        4 -> listOf(topLeft, topRight, bottomLeft, bottomRight)
        5 -> listOf(topLeft, topRight, center, bottomLeft, bottomRight)
        6 -> listOf(topLeft, topRight, midLeft, midRight, bottomLeft, bottomRight)
        else -> emptyList()
    }
}
