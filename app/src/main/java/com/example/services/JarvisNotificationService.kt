package com.example.services

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class InterceptedNotification(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val appName: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val hasReplyAction: Boolean = false
)

class JarvisNotificationService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isServiceRunning.value = true
        fetchActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isServiceRunning.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        processNotification(sbn)
    }

    private fun processNotification(sbn: StatusBarNotification) {
        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val pkg = sbn.packageName ?: ""

        if (title.isBlank() && text.isBlank()) return

        // Resolve clean app label
        val appName = try {
            val pm = applicationContext.packageManager
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            when {
                pkg.contains("whatsapp") -> "WhatsApp"
                pkg.contains("instagram") -> "Instagram"
                pkg.contains("youtube") -> "YouTube"
                pkg.contains("spotify") -> "Spotify"
                pkg.contains("messaging") || pkg.contains("mms") -> "Messages"
                pkg.contains("telecom") || pkg.contains("dialer") -> "Phone Call"
                else -> pkg.substringAfterLast('.')
            }
        }

        val hasReply = hasQuickReply(sbn.notification)

        val item = InterceptedNotification(
            packageName = pkg,
            appName = appName,
            title = title,
            message = text,
            timestamp = sbn.postTime,
            hasReplyAction = hasReply
        )

        val currentList = _notifications.value.toMutableList()
        // Deduplicate recent same title+text
        currentList.removeAll { it.packageName == pkg && it.title == title && it.message == text }
        currentList.add(0, item)
        // Keep max 50 recent notifications
        _notifications.value = currentList.take(50)
    }

    private fun hasQuickReply(notification: Notification?): Boolean {
        notification ?: return false
        val actions = NotificationCompat.getActionCount(notification)
        for (i in 0 until actions) {
            val action = NotificationCompat.getAction(notification, i)
            if (action?.remoteInputs?.isNotEmpty() == true) {
                return true
            }
        }
        return false
    }

    private fun fetchActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            for (sbn in active) {
                processNotification(sbn)
            }
        } catch (_: Exception) {}
    }

    companion object {
        private val _notifications = MutableStateFlow<List<InterceptedNotification>>(emptyList())
        val notifications: StateFlow<List<InterceptedNotification>> = _notifications.asStateFlow()

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun isPermissionGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, JarvisNotificationService::class.java).flattenToString()
            return enabledListeners.contains(myComponent)
        }

        fun openSettings(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        fun clearAll() {
            _notifications.value = emptyList()
        }
    }
}
