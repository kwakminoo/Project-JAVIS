package com.example.iris.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun WaveformVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 30
    val transition = rememberInfiniteTransition()

    val animatedValue by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing)
        )
    )

    val barHeights = remember(animatedValue, isActive) {
        if (!isActive) List(barCount) { 5f }
        else List(barCount) { i ->
            val phaseShift = (i.toFloat() / barCount) * PI.toFloat() * 2f
            val beat = sin(animatedValue + phaseShift)
            val height = (abs(beat).pow(2.5f)) * 80f + 20f
            height
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 8.dp)
    ) {
        val barWidth = size.width / barHeights.size
        barHeights.forEachIndexed { index, height ->
            drawRect(
                color = Color.Cyan,
                topLeft = androidx.compose.ui.geometry.Offset(index * barWidth, size.height - height),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.5f, height)
            )
        }
    }
}
