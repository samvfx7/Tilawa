package com.example.recognition.speech

import kotlinx.coroutines.flow.StateFlow

sealed interface SpeechError {
    data object MicrophoneUnavailable : SpeechError
    data object PermissionDenied : SpeechError
    data object SpeechServiceUnavailable : SpeechError
    data object NetworkError : SpeechError
    data object AudioTimeout : SpeechError
    data object NoSpeechDetected : SpeechError
    data class Other(val message: String, val errorCode: Int = 0) : SpeechError
}

interface TilawaSpeechRecognizer {
    val isListening: StateFlow<Boolean>
    val audioRms: StateFlow<Float>

    fun startListening(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (SpeechError) -> Unit,
        onRmsChanged: ((Float) -> Unit)? = null
    )

    fun stopListening()
    fun cancel()
    fun destroy()
    fun isAvailable(): Boolean
}
