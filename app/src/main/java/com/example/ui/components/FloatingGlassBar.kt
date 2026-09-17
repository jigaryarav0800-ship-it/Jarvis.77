package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurfaceVariant

@Composable
fun FloatingGlassBar(
    isLiveCallActive: Boolean,
    isMuted: Boolean,
    onToggleLiveCall: () -> Unit,
    onToggleMute: () -> Unit,
    onVisionTrigger: () -> Unit,
    onHubToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xE60A1326))
                .border(1.2.dp, JarvisCyan.copy(alpha = 0.35f), RoundedCornerShape(32.dp))
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = JarvisCyan)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Unmute Button
            GlassIconButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isMuted) "Unmute Jarvis" else "Mute Jarvis",
                isActive = !isMuted,
                activeColor = JarvisCyan,
                inactiveColor = Color(0xFF64748B),
                onClick = onToggleMute,
                testTag = "btn_toggle_mic"
            )

            // Primary Live Voice Call Toggle Button (Larger Center Button)
            val callBg by animateColorAsState(
                targetValue = if (isLiveCallActive) JarvisRed else JarvisGreen,
                label = "call_btn_bg"
            )

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(callBg)
                    .clickable { onToggleLiveCall() }
                    .testTag("btn_live_voice_call"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLiveCallActive) Icons.Default.CallEnd else Icons.Default.Call,
                    contentDescription = if (isLiveCallActive) "End Live Voice Call" else "Start Live Voice Call",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Screen Vision / Camera Reader Button
            GlassIconButton(
                icon = Icons.Default.CameraAlt,
                contentDescription = "Read Screen / Vision",
                isActive = false,
                activeColor = JarvisCyan,
                inactiveColor = Color(0xFF94A3B8),
                onClick = onVisionTrigger,
                testTag = "btn_vision_reader"
            )

            // Phone Controls Hub Button
            GlassIconButton(
                icon = Icons.Default.Dashboard,
                contentDescription = "Phone Control Hub",
                isActive = false,
                activeColor = JarvisCyan,
                inactiveColor = Color(0xFF94A3B8),
                onClick = onHubToggle,
                testTag = "btn_phone_hub"
            )
        }
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor.copy(alpha = 0.18f) else JarvisSurfaceVariant.copy(alpha = 0.6f))
            .border(
                1.dp,
                if (isActive) activeColor.copy(alpha = 0.5f) else JarvisBorder.copy(alpha = 0.4f),
                CircleShape
            )
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
