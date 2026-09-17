package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.InterceptedNotification
import com.example.services.JarvisNotificationService
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepNavy
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationInterceptorScreen(
    onSmartReplyRequest: (InterceptedNotification) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notifications by JarvisNotificationService.notifications.collectAsState()
    val isGranted = JarvisNotificationService.isPermissionGranted(context)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDeepNavy)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MESSAGE & NOTIFICATION RADAR",
                    color = JarvisCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${notifications.size} messages intercepted",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp
                )
            }

            if (notifications.isNotEmpty()) {
                Button(
                    onClick = { JarvisNotificationService.clearAll() },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                    modifier = Modifier.testTag("btn_clear_notifications")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", fontSize = 11.sp, color = JarvisTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Permission card if not granted
        if (!isGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisAmber.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = JarvisAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notification Access Required",
                            color = JarvisAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Allow Jarvis to read incoming WhatsApp, Instagram & SMS messages for auto-reply.",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { JarvisNotificationService.openSettings(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisAmber),
                        modifier = Modifier.testTag("btn_grant_notif")
                    ) {
                        Text("Enable", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = JarvisCyan.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "RADAR LISTENING FOR INCOMING MESSAGES",
                        color = JarvisCyan.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Incoming WhatsApp chats, Instagram DMs, calls, and SMS will be intercepted here with Gemini smart auto-reply.",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationCard(
                        notification = notif,
                        onSmartReply = { onSmartReplyRequest(notif) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: InterceptedNotification,
    onSmartReply: () -> Unit
) {
    val isWhatsApp = notification.packageName.contains("whatsapp")
    val isInstagram = notification.packageName.contains("instagram")
    val isCall = notification.packageName.contains("telecom") || notification.packageName.contains("dialer")

    val badgeColor = when {
        isWhatsApp -> JarvisGreen
        isInstagram -> Color(0xFFE1306C)
        isCall -> JarvisAmber
        else -> JarvisCyan
    }

    val timeFormatted = remember(notification.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(notification.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_${notification.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCall) Icons.Default.Phone else Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notification.appName.uppercase(),
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = timeFormatted,
                    color = JarvisTextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.title.ifBlank { "Incoming Notification" },
                color = JarvisTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            if (notification.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    color = JarvisTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gemini Smart Reply Action
            Button(
                onClick = onSmartReply,
                colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_smart_reply_${notification.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Gemini Smart Auto-Reply",
                    color = JarvisCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
