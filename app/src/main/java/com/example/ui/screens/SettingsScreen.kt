package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.JarvisAccessibilityService
import com.example.services.JarvisNotificationService
import com.example.storage.JarvisPreferences
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepNavy
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.voice.JarvisVoiceEngine

@Composable
fun SettingsScreen(
    preferences: JarvisPreferences,
    voiceEngine: JarvisVoiceEngine,
    onTestApiKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var apiKeyInput by remember { mutableStateOf(preferences.customApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var userNameInput by remember { mutableStateOf(preferences.userName) }
    var pitchVal by remember { mutableFloatStateOf(preferences.voicePitch) }
    var rateVal by remember { mutableFloatStateOf(preferences.voiceRate) }
    var autoSpeakVal by remember { mutableStateOf(preferences.autoSpeak) }
    var saveStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDeepNavy)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "JARVIS CONFIGURATION & API ENGINE",
            color = JarvisCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        saveStatus?.let { status ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisGreen.copy(alpha = 0.15f))
                    .border(1.dp, JarvisGreen, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(text = status, color = JarvisGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Gemini API Key Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GEMINI API KEY (LIVE & INTELLIGENCE)",
                        color = JarvisCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Provide your personal Google Gemini API Key or leave empty to use the system default. Used for voice reasoning, phone actions & screen analysis.",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Hide key" else "Show key",
                                tint = JarvisTextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        cursorColor = JarvisCyan,
                        focusedLabelColor = JarvisCyan,
                        unfocusedLabelColor = JarvisTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_api_key")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            preferences.customApiKey = apiKeyInput
                            saveStatus = "Gemini API Key updated successfully!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        modifier = Modifier.weight(1f).testTag("btn_save_api_key")
                    ) {
                        Text("Save Key", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onTestApiKey(preferences.getEffectiveApiKey())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                        modifier = Modifier.testTag("btn_test_api_key")
                    ) {
                        Text("Test Key", color = JarvisCyan)
                    }
                }
            }
        }

        // Voice & Personality Preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = JarvisGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JARVIS SPEECH & VOICE SYNTHESIS",
                        color = JarvisGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User name / honorific
                OutlinedTextField(
                    value = userNameInput,
                    onValueChange = {
                        userNameInput = it
                        preferences.userName = it
                    },
                    label = { Text("What should Jarvis call you? (e.g. Sir, Boss)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_user_name")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Pitch Slider
                Text(
                    text = "Speech Pitch: ${(pitchVal * 100).toInt()}%",
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = pitchVal,
                    onValueChange = {
                        pitchVal = it
                        preferences.voicePitch = it
                        voiceEngine.setPitch(it)
                    },
                    valueRange = 0.6f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisSurfaceVariant
                    ),
                    modifier = Modifier.testTag("slider_pitch")
                )

                // Voice Rate Slider
                Text(
                    text = "Speech Rate / Speed: ${(rateVal * 100).toInt()}%",
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = rateVal,
                    onValueChange = {
                        rateVal = it
                        preferences.voiceRate = it
                        voiceEngine.setSpeechRate(it)
                    },
                    valueRange = 0.6f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisSurfaceVariant
                    ),
                    modifier = Modifier.testTag("slider_rate")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Auto-Vocalize Replies", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Speak responses aloud automatically", color = JarvisTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoSpeakVal,
                        onCheckedChange = {
                            autoSpeakVal = it
                            preferences.autoSpeak = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = JarvisCyan
                        ),
                        modifier = Modifier.testTag("switch_auto_speak")
                    )
                }
            }
        }

        // System Permissions Checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = JarvisAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PHONE CONTROL PERMISSIONS STATUS",
                        color = JarvisAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val notifGranted = JarvisNotificationService.isPermissionGranted(context)
                val accessGranted = JarvisAccessibilityService.isServiceRunning()

                PermissionStatusItem(
                    title = "Notification Interceptor",
                    description = "Intercept WhatsApp, Instagram, SMS messages for smart reply",
                    isGranted = notifGranted,
                    onGrant = { JarvisNotificationService.openSettings(context) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionStatusItem(
                    title = "Accessibility Automation",
                    description = "Direct screen reading & automated device navigation",
                    isGranted = accessGranted,
                    onGrant = { JarvisAccessibilityService.openSettings(context) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionStatusItem(
                    title = "System App Settings",
                    description = "Manage microphone, camera, and phone permissions",
                    isGranted = true,
                    onGrant = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PermissionStatusItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisSurfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) JarvisGreen else JarvisAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, color = JarvisTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, color = JarvisTextSecondary, fontSize = 10.sp)
        }

        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) JarvisSurfaceVariant else JarvisAmber
            ),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = if (isGranted) "Check" else "Grant",
                color = if (isGranted) JarvisCyan else Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
