package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.PhoneController
import com.example.services.JarvisAccessibilityService
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepNavy
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun PhoneControlHubScreen(
    phoneController: PhoneController,
    onAnalyzeScreenText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var waPhone by remember { mutableStateOf("") }
    var waMessage by remember { mutableStateOf("") }

    var instaUser by remember { mutableStateOf("") }
    var instaMessage by remember { mutableStateOf("") }

    var mediaQuery by remember { mutableStateOf("") }
    var dialNumber by remember { mutableStateOf("") }

    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDeepNavy)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status banner if any action triggered
        statusMessage?.let { status ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisCyan.copy(alpha = 0.15f))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚡ $status",
                    color = JarvisCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Quick System Control Grid
        Text(
            text = "HARDWARE & SYSTEM CONTROLS",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickToggleCard(
                title = "Flashlight",
                subtitle = "Toggle Torch",
                icon = Icons.Default.FlashlightOn,
                color = JarvisAmber,
                modifier = Modifier.weight(1f),
                onClick = {
                    val res = phoneController.toggleFlashlight()
                    statusMessage = "Flashlight command sent"
                },
                testTag = "card_flashlight"
            )

            QuickToggleCard(
                title = "Camera",
                subtitle = "Launch Lens",
                icon = Icons.Default.Camera,
                color = JarvisCyan,
                modifier = Modifier.weight(1f),
                onClick = {
                    phoneController.openApp("camera")
                    statusMessage = "Camera launched"
                },
                testTag = "card_camera"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickToggleCard(
                title = "Volume Up",
                subtitle = "Raise Audio",
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                color = JarvisGreen,
                modifier = Modifier.weight(1f),
                onClick = {
                    phoneController.adjustVolume("up")
                    statusMessage = "Volume increased"
                },
                testTag = "card_vol_up"
            )

            QuickToggleCard(
                title = "Volume Mute",
                subtitle = "Silence",
                icon = Icons.AutoMirrored.Filled.VolumeMute,
                color = Color(0xFFFF5252),
                modifier = Modifier.weight(1f),
                onClick = {
                    phoneController.adjustVolume("mute")
                    statusMessage = "Media silenced"
                },
                testTag = "card_vol_mute"
            )

            QuickToggleCard(
                title = "Settings",
                subtitle = "System",
                icon = Icons.Default.Settings,
                color = JarvisTextSecondary,
                modifier = Modifier.weight(1f),
                onClick = {
                    phoneController.openApp("settings")
                    statusMessage = "Opened settings"
                },
                testTag = "card_settings"
            )
        }

        // WhatsApp Automation Card
        HubCard(title = "WHATSAPP DEEP AUTOMATION", color = JarvisGreen) {
            Text(
                text = "Send direct messages to phone numbers or search chat without saving contact.",
                color = JarvisTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = waPhone,
                onValueChange = { waPhone = it },
                label = { Text("Phone Number (with country code e.g. 919876543210)") },
                colors = outlinedColors(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_wa_phone")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = waMessage,
                onValueChange = { waMessage = it },
                label = { Text("Message Text") },
                colors = outlinedColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_wa_message")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val result = phoneController.sendWhatsAppMessage(waPhone, waMessage)
                        statusMessage = "WhatsApp dispatched"
                    },
                    enabled = waMessage.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisGreen),
                    modifier = Modifier.weight(1f).testTag("btn_send_wa")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Message", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        phoneController.openApp("whatsapp")
                        statusMessage = "WhatsApp opened"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                    modifier = Modifier.testTag("btn_open_wa")
                ) {
                    Text("Open WA", color = JarvisGreen)
                }
            }
        }

        // Instagram Connector Card
        HubCard(title = "INSTAGRAM DIRECT & CONNECT", color = Color(0xFFE1306C)) {
            Text(
                text = "Connect directly to Instagram Direct inbox, search profiles, or message users.",
                color = JarvisTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = instaUser,
                onValueChange = { instaUser = it },
                label = { Text("Instagram Username (e.g. alex or @alex)") },
                colors = outlinedColors(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_insta_user")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        phoneController.openInstagram("dm", instaUser, instaMessage)
                        statusMessage = "Opening Instagram Direct Inbox"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                    modifier = Modifier.weight(1f).testTag("btn_insta_dm")
                ) {
                    Text("Direct Inbox", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        phoneController.openInstagram("profile", instaUser)
                        statusMessage = "Opening @$instaUser"
                    },
                    enabled = instaUser.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                    modifier = Modifier.weight(1f).testTag("btn_insta_profile")
                ) {
                    Text("Open Profile", color = Color(0xFFE1306C))
                }
            }
        }

        // Media Playback (YouTube & Spotify)
        HubCard(title = "YOUTUBE & SPOTIFY STREAMING", color = JarvisCyan) {
            OutlinedTextField(
                value = mediaQuery,
                onValueChange = { mediaQuery = it },
                label = { Text("Song, Artist, or Video title") },
                colors = outlinedColors(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_media_query")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        phoneController.playYouTubeVideo(mediaQuery)
                        statusMessage = "Streaming on YouTube: $mediaQuery"
                    },
                    enabled = mediaQuery.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                    modifier = Modifier.weight(1f).testTag("btn_play_yt")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("YouTube", color = Color.White)
                }

                Button(
                    onClick = {
                        phoneController.playMusic(mediaQuery)
                        statusMessage = "Playing on Spotify: $mediaQuery"
                    },
                    enabled = mediaQuery.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisGreen),
                    modifier = Modifier.weight(1f).testTag("btn_play_spotify")
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Spotify", color = Color.Black)
                }
            }
        }

        // Screen Reading & Vision Assistant
        val liveScreen = JarvisAccessibilityService.currentScreenText.value
        HubCard(title = "SCREEN READER & VISION ASSISTANT", color = JarvisAmber) {
            Text(
                text = if (liveScreen.isNotBlank()) "CURRENT DETECTED SCREEN CONTENT:" else "Screen accessibility active. Tap below to feed screen text to Gemini.",
                color = JarvisTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            if (liveScreen.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisDeepNavy)
                        .padding(10.dp)
                ) {
                    Text(
                        text = liveScreen.take(250) + "...",
                        color = JarvisTextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onAnalyzeScreenText(liveScreen.ifBlank { "Analyze active screen content and assist me" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisAmber),
                modifier = Modifier.fillMaxWidth().testTag("btn_analyze_screen")
            ) {
                Text("Analyze Screen with Gemini", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun QuickToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = JarvisTextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun HubCard(
    title: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = JarvisCyan,
    unfocusedBorderColor = JarvisBorder,
    focusedTextColor = JarvisTextPrimary,
    unfocusedTextColor = JarvisTextPrimary,
    cursorColor = JarvisCyan,
    focusedLabelColor = JarvisCyan,
    unfocusedLabelColor = JarvisTextSecondary
)
