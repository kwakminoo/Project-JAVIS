package com.example.iris.ui.visualizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularWaveform(isActive: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing)
        )
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 10f
        val angleStep = PI * 2 / 50

        for (i in 0 until 50) {
            val angle = angleStep * i
            val x = (center.x + cos(angle) * radius).toFloat()
            val y = (center.y + sin(angle) * radius).toFloat()
            val length = if (isActive) {
                (sin(angle * 10 + animationProgress) * 10 + 10).toFloat()
            } else {
                10f
            }

            drawCircle(
                color = Color.Cyan,
                radius = length,
                center = Offset(x, y)
            )
        }
    }
}

