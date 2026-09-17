package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.gemini.GeminiClient
import com.example.services.InterceptedNotification
import com.example.services.JarvisAccessibilityService
import com.example.storage.JarvisPreferences
import com.example.ui.screens.AgentVoiceScreen
import com.example.ui.screens.ChatMessage
import com.example.ui.screens.NotificationInterceptorScreen
import com.example.ui.screens.PhoneControlHubScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepNavy
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.voice.AgentStatus
import com.example.voice.JarvisVoiceEngine
import kotlinx.coroutines.launch

enum class MainTab {
    VOICE_AGENT,
    PHONE_HUB,
    MESSAGES_RADAR,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: JarvisPreferences
    private lateinit var geminiClient: GeminiClient
    private lateinit var phoneController: PhoneController
    private lateinit var voiceEngine: JarvisVoiceEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = JarvisPreferences(applicationContext)
        geminiClient = GeminiClient { preferences.getEffectiveApiKey() }
        phoneController = PhoneController(this)

        setContent {
            MyApplicationTheme {
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent() {
        val coroutineScope = rememberCoroutineScope()
        var currentTab by remember { mutableStateOf(MainTab.VOICE_AGENT) }
        val messages = remember { mutableStateListOf<ChatMessage>() }

        // Initialize Voice Engine
        val engine = remember {
            JarvisVoiceEngine(
                context = this@MainActivity,
                onVoiceInputRecognized = { transcript ->
                    handleUserPrompt(transcript, messages, coroutineScope)
                }
            ).also { voiceEngine = it }
        }

        DisposableEffect(Unit) {
            onDispose {
                engine.destroy()
            }
        }

        // Request initial permissions
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            if (audioGranted) {
                // Audio ready
            }
        }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE
                )
            )

            // Initial welcome from Jarvis
            val welcomeText = "Jarvis system online. Connected to Gemini 3.5. Ready for voice calls, WhatsApp, Instagram, and full device control, ${preferences.userName}."
            messages.add(ChatMessage(text = welcomeText, isUser = false))
            if (preferences.autoSpeak) {
                engine.speak(welcomeText)
            }
        }

        val agentStatus by engine.agentStatus.collectAsState()
        val amplitude by engine.audioAmplitude.collectAsState()
        val isLiveCallActive by engine.isLiveCallActive.collectAsState()
        val isMuted by engine.isMuted.collectAsState()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                JarvisTopBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    isLiveCallActive = isLiveCallActive
                )
            },
            containerColor = JarvisDeepNavy
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    MainTab.VOICE_AGENT -> {
                        AgentVoiceScreen(
                            status = agentStatus,
                            amplitude = amplitude,
                            isLiveCallActive = isLiveCallActive,
                            isMuted = isMuted,
                            messages = messages,
                            onToggleLiveCall = {
                                if (isLiveCallActive) {
                                    engine.endLiveCall()
                                } else {
                                    engine.startLiveCall()
                                }
                            },
                            onToggleMute = { engine.toggleMute() },
                            onSendMessage = { prompt ->
                                handleUserPrompt(prompt, messages, coroutineScope)
                            },
                            onMicClick = {
                                if (agentStatus == AgentStatus.LISTENING) {
                                    engine.stopListening()
                                } else {
                                    engine.startListening()
                                }
                            },
                            onVisionTrigger = {
                                handleScreenVisionAnalysis(messages, coroutineScope)
                            },
                            onHubToggle = {
                                currentTab = MainTab.PHONE_HUB
                            },
                            onSpeakMessage = { text ->
                                engine.speak(text)
                            }
                        )
                    }

                    MainTab.PHONE_HUB -> {
                        PhoneControlHubScreen(
                            phoneController = phoneController,
                            onAnalyzeScreenText = { screenContent ->
                                currentTab = MainTab.VOICE_AGENT
                                handleUserPrompt("Analyze this screen text: $screenContent", messages, coroutineScope)
                            }
                        )
                    }

                    MainTab.MESSAGES_RADAR -> {
                        NotificationInterceptorScreen(
                            onSmartReplyRequest = { notification ->
                                handleSmartReply(notification, messages, coroutineScope) {
                                    currentTab = MainTab.VOICE_AGENT
                                }
                            }
                        )
                    }

                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            preferences = preferences,
                            voiceEngine = engine,
                            onTestApiKey = { key ->
                                coroutineScope.launch {
                                    Toast.makeText(this@MainActivity, "Testing Gemini API connection...", Toast.LENGTH_SHORT).show()
                                    val result = geminiClient.generateResponse("Ping check: Are you online?")
                                    result.onSuccess {
                                        Toast.makeText(this@MainActivity, "Gemini Connected: $it", Toast.LENGTH_LONG).show()
                                    }.onFailure {
                                        Toast.makeText(this@MainActivity, "Connection Error: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun handleUserPrompt(
        prompt: String,
        messages: MutableList<ChatMessage>,
        scope: kotlinx.coroutines.CoroutineScope
    ) {
        if (prompt.isBlank()) return

        messages.add(ChatMessage(text = prompt, isUser = true))
        voiceEngine.setStatus(AgentStatus.THINKING)

        scope.launch {
            val history = messages.map { it.text to it.isUser }
            val result = geminiClient.generateResponse(prompt, history)

            result.onSuccess { responseText ->
                voiceEngine.setStatus(AgentStatus.EXECUTING)
                // Execute actions parsed from Gemini response
                val actionResults = phoneController.parseAndExecuteActions(responseText)

                messages.add(
                    ChatMessage(
                        text = responseText,
                        isUser = false,
                        actionResults = actionResults
                    )
                )

                if (preferences.autoSpeak) {
                    voiceEngine.speak(responseText)
                } else {
                    voiceEngine.setStatus(if (voiceEngine.isLiveCallActive.value) AgentStatus.CONNECTED else AgentStatus.IDLE)
                }
            }.onFailure { error ->
                val errorMsg = "Apologies, sir. Encountered an issue: ${error.message}"
                messages.add(ChatMessage(text = errorMsg, isUser = false))
                voiceEngine.speak(errorMsg)
                voiceEngine.setStatus(AgentStatus.IDLE)
            }
        }
    }

    private fun handleScreenVisionAnalysis(
        messages: MutableList<ChatMessage>,
        scope: kotlinx.coroutines.CoroutineScope
    ) {
        val currentScreen = JarvisAccessibilityService.currentScreenText.value
        val prompt = if (currentScreen.isNotBlank()) {
            "Jarvis, analyze my current phone screen: \"$currentScreen\". Explain what is happening and what action I should take."
        } else {
            "Jarvis, perform screen audit. What is currently active on my phone?"
        }
        handleUserPrompt(prompt, messages, scope)
    }

    private fun handleSmartReply(
        notification: InterceptedNotification,
        messages: MutableList<ChatMessage>,
        scope: kotlinx.coroutines.CoroutineScope,
        onNavigateToVoice: () -> Unit
    ) {
        onNavigateToVoice()
        val prompt = "Incoming ${notification.appName} message from ${notification.title}: \"${notification.message}\". Draft a quick smart reply and send it."
        handleUserPrompt(prompt, messages, scope)
    }
}

@Composable
fun JarvisTopBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    isLiveCallActive: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(JarvisSurface)
            .border(0.8.dp, JarvisBorder)
            .padding(top = 10.dp, bottom = 6.dp)
    ) {
        // App Title & Call Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(JarvisCyan.copy(alpha = 0.2f))
                        .border(1.dp, JarvisCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "JARVIS AI",
                        color = JarvisCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "PHONE CONTROLLER • GEMINI 3.5",
                        color = JarvisTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (isLiveCallActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "● LIVE CALL",
                        color = Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Tabs (Inspired by LiveKit starter screens: Agent, Hub, Radar, Settings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavTabItem(
                title = "VOICE",
                icon = Icons.Default.Mic,
                isSelected = currentTab == MainTab.VOICE_AGENT,
                onClick = { onTabSelected(MainTab.VOICE_AGENT) },
                testTag = "tab_voice"
            )
            NavTabItem(
                title = "HUB",
                icon = Icons.Default.Dashboard,
                isSelected = currentTab == MainTab.PHONE_HUB,
                onClick = { onTabSelected(MainTab.PHONE_HUB) },
                testTag = "tab_hub"
            )
            NavTabItem(
                title = "RADAR",
                icon = Icons.Default.ChatBubble,
                isSelected = currentTab == MainTab.MESSAGES_RADAR,
                onClick = { onTabSelected(MainTab.MESSAGES_RADAR) },
                testTag = "tab_radar"
            )
            NavTabItem(
                title = "CONFIG",
                icon = Icons.Default.Settings,
                isSelected = currentTab == MainTab.SETTINGS,
                onClick = { onTabSelected(MainTab.SETTINGS) },
                testTag = "tab_settings"
            )
        }
    }
}

@Composable
fun NavTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = JarvisCyan
    val inactiveColor = JarvisTextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) activeColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) activeColor else inactiveColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
