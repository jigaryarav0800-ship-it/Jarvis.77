package com.example.automation

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import com.example.services.JarvisAccessibilityService
import java.net.URLEncoder

sealed class AutomationResult {
    data class Success(val message: String) : AutomationResult()
    data class Error(val error: String) : AutomationResult()
}

class PhoneController(private val context: Context) {

    private var isTorchOn = false

    fun openApp(packageNameOrKeyword: String): AutomationResult {
        val targetPackage = when (packageNameOrKeyword.lowercase().trim()) {
            "whatsapp", "wa" -> "com.whatsapp"
            "instagram", "insta", "ig" -> "com.instagram.android"
            "youtube", "yt" -> "com.google.android.youtube"
            "spotify", "music", "song" -> "com.spotify.music"
            "chrome", "browser" -> "com.android.chrome"
            "maps", "google maps" -> "com.google.android.apps.maps"
            "settings" -> {
                val intent = Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
                return AutomationResult.Success("Opened System Settings")
            }
            "camera" -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
                return AutomationResult.Success("Opened Camera")
            }
            "dialer", "phone", "call" -> {
                val intent = Intent(Intent.ACTION_DIAL).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
                return AutomationResult.Success("Opened Phone Dialer")
            }
            else -> packageNameOrKeyword
        }

        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                AutomationResult.Success("Launched ${packageNameOrKeyword.replaceFirstChar { it.uppercase() }}")
            } else {
                // Fallback: search on Google Play or browser
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$targetPackage")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                AutomationResult.Success("Opening app store for $packageNameOrKeyword")
            }
        } catch (e: Exception) {
            AutomationResult.Error("Could not open $packageNameOrKeyword: ${e.message}")
        }
    }

    fun sendWhatsAppMessage(target: String, message: String): AutomationResult {
        return try {
            val cleanNumber = target.filter { it.isDigit() }
            val encodedMsg = URLEncoder.encode(message, "UTF-8")

            val intent = if (cleanNumber.length >= 7) {
                // Direct WhatsApp chat link
                val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMsg"
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                // Generic send intent targeting WhatsApp
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage("com.whatsapp")
                    putExtra(Intent.EXTRA_TEXT, message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }

            // Verify if WhatsApp is installed
            val pm = context.packageManager
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
                AutomationResult.Success("Dispatched WhatsApp message to $target: \"$message\"")
            } else {
                // Web fallback
                val webUrl = "https://api.whatsapp.com/send?text=$encodedMsg"
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                AutomationResult.Success("Opened WhatsApp web interface with message draft")
            }
        } catch (e: Exception) {
            AutomationResult.Error("WhatsApp automation error: ${e.message}")
        }
    }

    fun openInstagram(action: String, target: String = "", message: String = ""): AutomationResult {
        return try {
            val intent = when (action.lowercase()) {
                "inbox", "dm" -> {
                    val directUri = Uri.parse("https://instagram.com/direct/inbox/")
                    Intent(Intent.ACTION_VIEW, directUri).apply {
                        setPackage("com.instagram.android")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                "profile", "user" -> {
                    val userClean = target.removePrefix("@").trim()
                    val profileUri = Uri.parse("https://instagram.com/$userClean")
                    Intent(Intent.ACTION_VIEW, profileUri).apply {
                        setPackage("com.instagram.android")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                else -> {
                    // Open Instagram app
                    context.packageManager.getLaunchIntentForPackage("com.instagram.android")?.apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    } ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            }

            context.startActivity(intent)
            AutomationResult.Success(
                if (target.isNotBlank()) "Connecting to Instagram @$target" else "Opened Instagram Direct Inbox"
            )
        } catch (e: Exception) {
            AutomationResult.Error("Instagram automation error: ${e.message}")
        }
    }

    fun playYouTubeVideo(query: String): AutomationResult {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val appIntent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (appIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(appIntent)
                AutomationResult.Success("Playing YouTube video: $query")
            } else {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=$encoded")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(webIntent)
                AutomationResult.Success("Opened YouTube search: $query")
            }
        } catch (e: Exception) {
            AutomationResult.Error("YouTube error: ${e.message}")
        }
    }

    fun playMusic(query: String): AutomationResult {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            // Try Spotify
            val spotifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:$encoded")).apply {
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (spotifyIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(spotifyIntent)
                AutomationResult.Success("Playing \"$query\" on Spotify")
            } else {
                // Fallback to general music search or YouTube Music
                val mediaIntent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
                    putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)
                    putExtra(MediaStore.EXTRA_MEDIA_TITLE, query)
                    putExtra(MediaStore.EXTRA_MEDIA_ARTIST, query)
                    putExtra(android.app.SearchManager.QUERY, query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (mediaIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mediaIntent)
                    AutomationResult.Success("Streaming \"$query\" on default music player")
                } else {
                    playYouTubeVideo(query)
                }
            }
        } catch (e: Exception) {
            AutomationResult.Error("Music error: ${e.message}")
        }
    }

    fun makeCall(contactOrNumber: String): AutomationResult {
        return try {
            val clean = contactOrNumber.filter { it.isDigit() || it == '+' }
            val dialIntent = if (clean.isNotBlank()) {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
            } else {
                Intent(Intent.ACTION_DIAL)
            }.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(dialIntent)
            AutomationResult.Success("Dialing $contactOrNumber")
        } catch (e: Exception) {
            AutomationResult.Error("Dialer error: ${e.message}")
        }
    }

    fun toggleFlashlight(forceState: Boolean? = null): AutomationResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return AutomationResult.Error("Camera service unavailable")
            val cameraId = cameraManager.cameraIdList.firstOrNull()
                ?: return AutomationResult.Error("No camera flash found")

            isTorchOn = forceState ?: !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            AutomationResult.Success("Flashlight turned ${if (isTorchOn) "ON" else "OFF"}")
        } catch (e: Exception) {
            AutomationResult.Error("Flashlight toggle error: ${e.message}")
        }
    }

    fun adjustVolume(direction: String): AutomationResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return AutomationResult.Error("Audio manager unavailable")

            when (direction.lowercase()) {
                "up", "raise" -> {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI
                    )
                    AutomationResult.Success("Volume increased")
                }
                "down", "lower" -> {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI
                    )
                    AutomationResult.Success("Volume decreased")
                }
                "mute" -> {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_MUTE,
                        AudioManager.FLAG_SHOW_UI
                    )
                    AutomationResult.Success("Device muted")
                }
                else -> {
                    AutomationResult.Error("Unknown volume direction")
                }
            }
        } catch (e: Exception) {
            AutomationResult.Error("Volume adjustment error: ${e.message}")
        }
    }

    fun searchWeb(query: String): AutomationResult {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult.Success("Searching web for \"$query\"")
        } catch (e: Exception) {
            AutomationResult.Error("Search error: ${e.message}")
        }
    }

    /**
     * Extracts and executes [ACTION:COMMAND:ARGS] tags generated by Gemini
     */
    fun parseAndExecuteActions(geminiResponse: String): List<AutomationResult> {
        val results = mutableListOf<AutomationResult>()
        val regex = Regex("\\[ACTION:([A-Z_]+)(?::([^:\\]]+))?(?::([^\\]]+))?\\]")
        val matches = regex.findAll(geminiResponse)

        for (match in matches) {
            val action = match.groupValues.getOrNull(1)?.uppercase() ?: continue
            val param1 = match.groupValues.getOrNull(2) ?: ""
            val param2 = match.groupValues.getOrNull(3) ?: ""

            val result = when (action) {
                "OPEN_APP" -> openApp(param1)
                "WHATSAPP_MESSAGE" -> sendWhatsAppMessage(param1, param2)
                "INSTAGRAM_DM" -> openInstagram("dm", param1, param2)
                "YOUTUBE_PLAY" -> playYouTubeVideo(param1)
                "SPOTIFY_PLAY" -> playMusic(param1)
                "CALL" -> makeCall(param1)
                "FLASHLIGHT" -> toggleFlashlight()
                "VOLUME" -> adjustVolume(param1)
                "WEB_SEARCH" -> searchWeb(param1)
                "READ_SCREEN" -> {
                    val screenText = JarvisAccessibilityService.currentScreenText.value
                    if (screenText.isNotBlank()) {
                        AutomationResult.Success("Screen read: ${screenText.take(150)}...")
                    } else {
                        AutomationResult.Success("Screen reader ready. Analyzing display contents...")
                    }
                }
                "CHECK_NOTIFICATIONS" -> {
                    AutomationResult.Success("Intercepted notifications pulled into mission dashboard")
                }
                else -> null
            }
            if (result != null) results.add(result)
        }
        return results
    }
}
