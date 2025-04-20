// 위치: com/example/iris/ui/visualizer/CircularWaveform.kt
package com.example.iris.ui.visualizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CircularWaveform(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val bars = 60
    val angles = List(bars) { it * (360f / bars) }
    var values by remember { mutableStateOf(List(bars) { 0f }) }

    LaunchedEffect(isActive) {
        while (isActive) {
            values = List(bars) { (10..100).random().toFloat() }
            delay(50)
        }
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f
        val barWidth = 6.dp.toPx()
        val maxBarLength = size.minDimension / 4f

        angles.forEachIndexed { index, angle ->
            val length = values.getOrNull(index) ?: 0f
            val barLength = (length / 100f) * maxBarLength

            rotate(degrees = angle, pivot = center) {
                drawRoundRect(
                    color = Color.Cyan,
                    topLeft = Offset(center.x - barWidth / 2, center.y - radius - barLength),
                    size = Size(barWidth, barLength),
                    cornerRadius = CornerRadius.Zero
                )
            }
        }
    }
}
