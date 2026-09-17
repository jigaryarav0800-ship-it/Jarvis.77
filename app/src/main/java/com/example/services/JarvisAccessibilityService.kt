package com.example.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JarvisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isAccessibilityEnabled.value = true
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val root = rootInActiveWindow ?: return
        val sb = StringBuilder()
        traverseNode(root, sb, 0)
        val text = sb.toString().trim()
        if (text.isNotBlank()) {
            _currentScreenText.value = text.take(2000)
        }
    }

    private fun traverseNode(node: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (node == null || depth > 8) return
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrBlank()) {
            sb.append(text).append("\n")
        } else if (!desc.isNullOrBlank()) {
            sb.append(desc).append("\n")
        }

        for (i in 0 until node.childCount) {
            traverseNode(node.getChild(i), sb, depth + 1)
        }
    }

    override fun onInterrupt() {
        _isAccessibilityEnabled.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        _isAccessibilityEnabled.value = false
        if (instance == this) instance = null
    }

    companion object {
        private var instance: JarvisAccessibilityService? = null

        private val _isAccessibilityEnabled = MutableStateFlow(false)
        val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

        private val _currentScreenText = MutableStateFlow("")
        val currentScreenText: StateFlow<String> = _currentScreenText.asStateFlow()

        fun performHome(): Boolean = instance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        fun performBack(): Boolean = instance?.performGlobalAction(GLOBAL_ACTION_BACK) ?: false
        fun performNotifications(): Boolean = instance?.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) ?: false
        fun performRecents(): Boolean = instance?.performGlobalAction(GLOBAL_ACTION_RECENTS) ?: false

        fun isServiceRunning(): Boolean = instance != null

        fun openSettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
