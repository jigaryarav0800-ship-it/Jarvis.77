package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.voice.AgentStatus
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    status: AgentStatus,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primaryColor = when (status) {
        AgentStatus.IDLE -> Color(0xFF00E5FF).copy(alpha = 0.6f)
        AgentStatus.CONNECTING -> JarvisAmber
        AgentStatus.CONNECTED -> JarvisCyan
        AgentStatus.LISTENING -> JarvisGreen
        AgentStatus.THINKING -> JarvisAmber
        AgentStatus.SPEAKING -> JarvisCyan
        AgentStatus.EXECUTING -> JarvisAmber
    }

    val glowColor = primaryColor.copy(alpha = 0.35f)

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2.6f)
            val reactiveScale = 1.0f + (amplitude * 0.45f)

            // Outer subtle aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.5f * reactiveScale
                ),
                center = center,
                radius = baseRadius * 1.5f * reactiveScale
            )

            // Outer segmented ring
            val segments = 8
            for (i in 0 until segments) {
                val startAngle = rotation + (i * (360f / segments))
                drawArc(
                    color = primaryColor.copy(alpha = 0.7f),
                    startAngle = startAngle,
                    sweepAngle = 28f,
                    useCenter = false,
                    style = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round),
                    size = androidx.compose.ui.geometry.Size(
                        baseRadius * 2 * reactiveScale,
                        baseRadius * 2 * reactiveScale
                    ),
                    topLeft = Offset(
                        center.x - baseRadius * reactiveScale,
                        center.y - baseRadius * reactiveScale
                    )
                )
            }

            // Middle counter-rotating dashed ring
            val midRadius = baseRadius * 0.75f
            val midSegments = 12
            for (i in 0 until midSegments) {
                val startAngle = counterRotation + (i * (360f / midSegments))
                drawArc(
                    color = primaryColor.copy(alpha = 0.85f),
                    startAngle = startAngle,
                    sweepAngle = 18f,
                    useCenter = false,
                    style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round),
                    size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                    topLeft = Offset(center.x - midRadius, center.y - midRadius)
                )
            }

            // Radial energy nodes
            val nodeCount = 6
            for (i in 0 until nodeCount) {
                val angle = Math.toRadians((rotation * 1.5f + (i * 60)).toDouble())
                val distance = baseRadius * 0.52f
                val nodeX = center.x + (distance * cos(angle)).toFloat()
                val nodeY = center.y + (distance * sin(angle)).toFloat()
                drawCircle(
                    color = primaryColor,
                    radius = 3.5f.dp.toPx() * (1f + amplitude * 0.5f),
                    center = Offset(nodeX, nodeY)
                )
            }

            // Inner glowing Core
            val coreRadius = baseRadius * 0.38f * pulse * (1f + amplitude * 0.3f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, primaryColor, Color.Transparent),
                    center = center,
                    radius = coreRadius
                ),
                center = center,
                radius = coreRadius
            )

            // Center crystal
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx() * (1f + amplitude * 0.5f),
                center = center
            )
        }
    }
}
