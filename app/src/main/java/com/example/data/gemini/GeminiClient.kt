package com.example.data.gemini

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class GeminiClient(private val apiKeyProvider: () -> String) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private val systemPrompt = Content(
        parts = listOf(
            Part(
                text = """
                    You are JARVIS, an ultra-advanced AI operating system and personal phone commander for Android.
                    You speak with calm, sophisticated confidence (resembling Tony Stark's JARVIS or Friday).
                    You have direct control over this Android smartphone.
                    
                    When the user asks you to perform an action on the phone, acknowledge concisely and attach the appropriate system action tag at the end of your message:
                    - Open any app: [ACTION:OPEN_APP:whatsapp], [ACTION:OPEN_APP:instagram], [ACTION:OPEN_APP:youtube], [ACTION:OPEN_APP:spotify], [ACTION:OPEN_APP:camera], [ACTION:OPEN_APP:settings]
                    - Send WhatsApp message: [ACTION:WHATSAPP_MESSAGE:phoneNumberOrName:messageText]
                    - Instagram direct message / profile: [ACTION:INSTAGRAM_DM:username:messageText]
                    - Play video on YouTube: [ACTION:YOUTUBE_PLAY:searchQuery]
                    - Play music or song: [ACTION:SPOTIFY_PLAY:songOrArtistQuery]
                    - Call someone: [ACTION:CALL:phoneNumberOrContact]
                    - Toggle flashlight / torch: [ACTION:FLASHLIGHT:toggle]
                    - System volume: [ACTION:VOLUME:up], [ACTION:VOLUME:down], [ACTION:VOLUME:mute]
                    - Screen examination / Reading: [ACTION:READ_SCREEN]
                    - Check incoming messages & notifications: [ACTION:CHECK_NOTIFICATIONS]
                    - Web search: [ACTION:WEB_SEARCH:query]
                    
                    Keep your spoken replies natural, witty, and helpful. You can understand Hindi, Hinglish, and English effortlessly. If the user speaks Hindi, reply in crisp Hinglish/Hindi or English as suitable.
                """.trimIndent()
            )
        )
    )

    suspend fun generateResponse(
        prompt: String,
        history: List<Pair<String, Boolean>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API Key is missing. Please configure your API key in Settings.")
            )
        }

        val contents = mutableListOf<Content>()
        // Include short conversation history
        for ((text, isUser) in history.takeLast(6)) {
            contents.add(
                Content(
                    role = if (isUser) "user" else "model",
                    parts = listOf(Part(text = text))
                )
            )
        }
        contents.add(
            Content(
                role = "user",
                parts = listOf(Part(text = prompt))
            )
        )

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = systemPrompt,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 800
            )
        )

        try {
            val response = apiService.generateContent(apiKey = apiKey, request = request)
            val candidate = response.candidates?.firstOrNull()
            val responseText = candidate?.content?.parts?.firstOrNull()?.text
                ?: "I processed your request, sir, but received an empty response."
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeScreen(
        base64Image: String,
        userQuery: String = "Explain what is on the screen and suggest actions"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API Key is missing. Please configure your API key in Settings.")
            )
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(
                        Part(text = "You are JARVIS reading the user's phone screen. Analyze this screen carefully. $userQuery"),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            systemInstruction = systemPrompt,
            generationConfig = GenerationConfig(
                temperature = 0.4f,
                maxOutputTokens = 600
            )
        )

        try {
            val response = apiService.generateContent(apiKey = apiKey, request = request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Screen analysis complete, no recognizable elements found."
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
