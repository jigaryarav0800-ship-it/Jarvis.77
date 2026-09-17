package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class AgentStatus {
    IDLE,
    CONNECTING,
    CONNECTED,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING
}

class JarvisVoiceEngine(
    private val context: Context,
    private val onVoiceInputRecognized: (String) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false

    private val _agentStatus = MutableStateFlow(AgentStatus.IDLE)
    val agentStatus: StateFlow<AgentStatus> = _agentStatus.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _isLiveCallActive = MutableStateFlow(false)
    val isLiveCallActive: StateFlow<Boolean> = _isLiveCallActive.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var amplitudeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                tts?.language = Locale.ENGLISH
                tts?.setPitch(0.95f)
                tts?.setSpeechRate(1.05f)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _agentStatus.value = AgentStatus.SPEAKING
                        startAudioSimulation(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        startAudioSimulation(false)
                        if (_isLiveCallActive.value && !_isMuted.value) {
                            scope.launch {
                                delay(300)
                                startListening()
                            }
                        } else {
                            _agentStatus.value = AgentStatus.IDLE
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        startAudioSimulation(false)
                        _agentStatus.value = AgentStatus.IDLE
                    }
                })
            }
        }
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun speak(text: String, autoCleanTags: Boolean = true) {
        if (!isTtsInitialized) return
        stopListening()
        val speechText = if (autoCleanTags) {
            text.replace(Regex("\\[ACTION:[^\\]]+\\]"), "").trim()
        } else {
            text
        }

        if (speechText.isBlank()) return

        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        startAudioSimulation(false)
        if (_isLiveCallActive.value) {
            _agentStatus.value = AgentStatus.CONNECTED
        } else {
            _agentStatus.value = AgentStatus.IDLE
        }
    }

    fun startListening() {
        if (_isMuted.value) return
        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _agentStatus.value = AgentStatus.LISTENING
                startAudioSimulation(true)
            }

            override fun onBeginningOfSpeech() {
                _agentStatus.value = AgentStatus.LISTENING
            }

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2) / 12f).coerceIn(0.1f, 1.0f)
                _audioAmplitude.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                startAudioSimulation(false)
                _agentStatus.value = AgentStatus.THINKING
            }

            override fun onError(error: Int) {
                startAudioSimulation(false)
                if (_isLiveCallActive.value && !_isMuted.value) {
                    // In live mode, retry listening after brief pause
                    scope.launch {
                        delay(600)
                        if (_isLiveCallActive.value) startListening()
                    }
                } else {
                    _agentStatus.value = AgentStatus.IDLE
                }
            }

            override fun onResults(results: Bundle?) {
                startAudioSimulation(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull() ?: ""
                if (recognizedText.isNotBlank()) {
                    _agentStatus.value = AgentStatus.THINKING
                    onVoiceInputRecognized(recognizedText)
                } else {
                    _agentStatus.value = if (_isLiveCallActive.value) AgentStatus.CONNECTED else AgentStatus.IDLE
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            _agentStatus.value = AgentStatus.IDLE
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        startAudioSimulation(false)
        if (_agentStatus.value == AgentStatus.LISTENING) {
            _agentStatus.value = if (_isLiveCallActive.value) AgentStatus.CONNECTED else AgentStatus.IDLE
        }
    }

    fun startLiveCall() {
        _isLiveCallActive.value = true
        _agentStatus.value = AgentStatus.CONNECTING
        scope.launch {
            delay(500)
            _agentStatus.value = AgentStatus.CONNECTED
            speak("Jarvis online and connected, sir. How can I assist you with your phone?")
        }
    }

    fun endLiveCall() {
        _isLiveCallActive.value = false
        stopSpeaking()
        stopListening()
        _agentStatus.value = AgentStatus.IDLE
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            stopListening()
        } else if (_isLiveCallActive.value && _agentStatus.value != AgentStatus.SPEAKING) {
            startListening()
        }
    }

    fun setStatus(status: AgentStatus) {
        _agentStatus.value = status
    }

    private fun startAudioSimulation(active: Boolean) {
        amplitudeJob?.cancel()
        if (active) {
            amplitudeJob = scope.launch {
                while (isActive) {
                    val base = 0.25f + (Math.random().toFloat() * 0.7f)
                    _audioAmplitude.value = base
                    delay(80)
                }
            }
        } else {
            _audioAmplitude.value = 0f
        }
    }

    fun destroy() {
        stopSpeaking()
        stopListening()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
