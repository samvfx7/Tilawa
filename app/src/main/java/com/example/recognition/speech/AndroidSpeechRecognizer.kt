package com.example.recognition.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AndroidSpeechRecognizer(
    private val context: Context
) : TilawaSpeechRecognizer {

    private val tag = "AndroidSpeechRecognizer"

    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    override val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var isCurrentlyActive = false

    private var currentOnPartial: ((String) -> Unit)? = null
    private var currentOnFinal: ((String) -> Unit)? = null
    private var currentOnError: ((SpeechError) -> Unit)? = null
    private var currentOnRms: ((Float) -> Unit)? = null

    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    override fun startListening(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (SpeechError) -> Unit,
        onRmsChanged: ((Float) -> Unit)?
    ) {
        currentOnPartial = onPartialResult
        currentOnFinal = onFinalResult
        currentOnError = onError
        currentOnRms = onRmsChanged

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            }

            speechRecognizer?.setRecognitionListener(createListener())

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                // Arabic (Saudi Arabia / Standard)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-SA")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar", "ar-EG", "ar-XA"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                // Prefer on-device offline recognition when supported
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.startListening(intent)
            isCurrentlyActive = true
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start speech recognition", e)
            _isListening.value = false
            isCurrentlyActive = false
            onError(SpeechError.Other(e.message ?: "Failed to start listening"))
        }
    }

    override fun stopListening() {
        isCurrentlyActive = false
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping speech recognizer", e)
        }
    }

    override fun cancel() {
        isCurrentlyActive = false
        _isListening.value = false
        _audioRms.value = 0f
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e(tag, "Error canceling speech recognizer", e)
        }
    }

    override fun destroy() {
        cancel()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(tag, "Error destroying speech recognizer", e)
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize dB to 0.0 - 1.0 range (typical rms is -2 to 10)
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                _audioRms.value = normalized
                currentOnRms?.invoke(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _audioRms.value = 0f
            }

            override fun onError(errorCode: Int) {
                _audioRms.value = 0f
                val error = when (errorCode) {
                    SpeechRecognizer.ERROR_AUDIO -> SpeechError.MicrophoneUnavailable
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechError.PermissionDenied
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> SpeechError.NetworkError
                    SpeechRecognizer.ERROR_NO_MATCH -> SpeechError.NoSpeechDetected
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechError.AudioTimeout
                    SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechError.Other("Speech recognizer busy", errorCode)
                    else -> SpeechError.Other("Recognition error $errorCode", errorCode)
                }

                // If continuous listening is desired and it's a minor timeout or no match, gracefully restart
                if (isCurrentlyActive && (errorCode == SpeechRecognizer.ERROR_NO_MATCH || errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                    restartListeningSilently()
                } else {
                    currentOnError?.invoke(error)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    currentOnFinal?.invoke(text)
                }
                // Continue continuous listening if active
                if (isCurrentlyActive) {
                    restartListeningSilently()
                } else {
                    _isListening.value = false
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    currentOnPartial?.invoke(text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun restartListeningSilently() {
        if (!isCurrentlyActive) return
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(tag, "Failed to restart speech recognizer", e)
        }
    }
}
