package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.voice.AgentStatus

@Composable
fun AgentStatusIndicator(
    status: AgentStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_alpha"
    )

    val (color, text) = when (status) {
        AgentStatus.IDLE -> Color(0xFF64748B) to "JARVIS STANDBY"
        AgentStatus.CONNECTING -> JarvisAmber to "CONNECTING TO GEMINI..."
        AgentStatus.CONNECTED -> JarvisCyan to "GEMINI LIVE ACTIVE"
        AgentStatus.LISTENING -> JarvisGreen to "LISTENING FOR COMMANDS..."
        AgentStatus.THINKING -> JarvisAmber to "PROCESSING WITH GEMINI 3.5..."
        AgentStatus.SPEAKING -> JarvisCyan to "JARVIS SPEAKING..."
        AgentStatus.EXECUTING -> JarvisGreen to "EXECUTING PHONE ACTION..."
    }

    val animatedColor by animateColorAsState(targetValue = color, label = "color")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(JarvisSurfaceVariant.copy(alpha = 0.75f))
            .border(1.dp, animatedColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(animatedColor.copy(alpha = if (status != AgentStatus.IDLE) alpha else 0.7f))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = animatedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}
