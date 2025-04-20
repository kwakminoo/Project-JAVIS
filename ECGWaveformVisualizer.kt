package com.example.iris.ui.visualizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ECGWaveformVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 48, // 좌우 대칭으로 보이기 위한 총 막대 개수
    maxHeight: Float = 100f
) {
    var amplitudes by remember { mutableStateOf(List(barCount) { 0f }) }

    LaunchedEffect(isActive) {
        while (isActive) {
            amplitudes = List(barCount) { Random.nextFloat() * maxHeight }
            delay(60)
        }

        // 멈추면 다시 일직선으로 초기화
        amplitudes = List(barCount) { 4f }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val centerY = size.height / 2
        val barWidth = size.width / (amplitudes.size * 1.5f)

        amplitudes.forEachIndexed { i, height ->
            val x = (i * barWidth * 1.5f) + (barWidth / 2)
            drawLine(
                color = Color.Cyan,
                start = androidx.compose.ui.geometry.Offset(x, centerY - height / 2),
                end = androidx.compose.ui.geometry.Offset(x, centerY + height / 2),
                strokeWidth = barWidth
            )
        }
    }
}
