package com.example.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class JarvisPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        private const val KEY_VOICE_RATE = "voice_rate"
        private const val KEY_AUTO_SPEAK = "auto_speak"
        private const val KEY_SCREEN_ANALYSIS_PROMPT = "screen_prompt"
    }

    var customApiKey: String
        get() = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_API_KEY, value.trim()).apply()

    fun getEffectiveApiKey(): String {
        val custom = customApiKey
        if (custom.isNotBlank()) return custom
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
    }

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Sir") ?: "Sir"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var voicePitch: Float
        get() = prefs.getFloat(KEY_VOICE_PITCH, 0.95f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_PITCH, value).apply()

    var voiceRate: Float
        get() = prefs.getFloat(KEY_VOICE_RATE, 1.05f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_RATE, value).apply()

    var autoSpeak: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SPEAK, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SPEAK, value).apply()
}
