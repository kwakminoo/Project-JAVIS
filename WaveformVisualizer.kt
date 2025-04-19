package com.example.iris.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedAmplitude by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = if (isActive) 60f else 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "amplitude"
    )

    Canvas(modifier = modifier) {
        val centerY = size.height / 2
        val lineWidth = 20f
        val space = 30f

        for (i in 0..4) {
            val x = size.width / 2 + (i - 2) * (lineWidth + space)
            drawLine(
                color = Color.Cyan,
                start = Offset(x, centerY - animatedAmplitude),
                end = Offset(x, centerY + animatedAmplitude),
                strokeWidth = lineWidth
            )
        }
    }
}
