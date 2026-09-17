package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.AutomationResult
import com.example.ui.components.AgentStatusIndicator
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.FloatingGlassBar
import com.example.ui.components.MessageBar
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepNavy
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.voice.AgentStatus

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val actionResults: List<AutomationResult> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AgentVoiceScreen(
    status: AgentStatus,
    amplitude: Float,
    isLiveCallActive: Boolean,
    isMuted: Boolean,
    messages: List<ChatMessage>,
    onToggleLiveCall: () -> Unit,
    onToggleMute: () -> Unit,
    onSendMessage: (String) -> Unit,
    onMicClick: () -> Unit,
    onVisionTrigger: () -> Unit,
    onHubToggle: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisDeepNavy,
                        Color(0xFF030712),
                        Color(0xFF050B18)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Status & Visualizer Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Agent Status Badge
                AgentStatusIndicator(status = status)

                Spacer(modifier = Modifier.height(10.dp))

                // Central Holographic Arc Reactor
                ArcReactorVisualizer(
                    status = status,
                    amplitude = amplitude,
                    modifier = Modifier.size(190.dp)
                )
            }

            // Conversation & Actions stream
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (messages.isEmpty()) {
                    // Empty state welcome placeholder
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = JarvisCyan.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "JARVIS PHONE CONTROLLER READY",
                            color = JarvisCyan.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start live voice call or say \"Open WhatsApp\", \"Play music on Spotify\", \"Read screen\", or \"Message on Instagram\".",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageItem(
                                message = msg,
                                onSpeak = { onSpeakMessage(msg.text) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            // Bottom interactive Message Bar
            MessageBar(
                onSendMessage = onSendMessage,
                onMicClick = onMicClick,
                isListening = status == AgentStatus.LISTENING
            )

            // LiveKit Floating Glass Bar
            FloatingGlassBar(
                isLiveCallActive = isLiveCallActive,
                isMuted = isMuted,
                onToggleLiveCall = onToggleLiveCall,
                onToggleMute = onToggleMute,
                onVisionTrigger = onVisionTrigger,
                onHubToggle = onHubToggle
            )
        }
    }
}

@Composable
fun MessageItem(
    message: ChatMessage,
    onSpeak: () -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.8f else 0.92f)
                .testTag(if (isUser) "msg_user_${message.id}" else "msg_jarvis_${message.id}"),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) JarvisSurfaceVariant else JarvisSurface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) JarvisCyan.copy(alpha = 0.3f) else JarvisBorder.copy(alpha = 0.6f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "YOU" else "JARVIS (GEMINI 3.5)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) JarvisCyan else JarvisAmber,
                        letterSpacing = 1.sp
                    )

                    if (!isUser) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Vocalize",
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Clean display text without action tags
                val displayText = message.text.replace(Regex("\\[ACTION:[^\\]]+\\]"), "").trim()
                Text(
                    text = displayText,
                    color = JarvisTextPrimary,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )

                // Render Action execution badges
                if (message.actionResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        message.actionResults.forEach { action ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisDeepNavy)
                                    .border(0.8.dp, JarvisGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = JarvisGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val text = when (action) {
                                    is AutomationResult.Success -> action.message
                                    is AutomationResult.Error -> "Error: ${action.error}"
                                }
                                Text(
                                    text = text,
                                    color = JarvisGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
