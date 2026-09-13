package com.example.recognition.speech

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class SimulationSpeechRecognizer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : TilawaSpeechRecognizer {

    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    override val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private var simulationJob: Job? = null
    private var wordsToSimulate: List<String> = emptyList()
    private var currentIndex = 0

    override fun isAvailable(): Boolean = true

    fun setWordsSequence(words: List<String>) {
        this.wordsToSimulate = words
        this.currentIndex = 0
    }

    override fun startListening(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (SpeechError) -> Unit,
        onRmsChanged: ((Float) -> Unit)?
    ) {
        _isListening.value = true
        simulationJob?.cancel()
        simulationJob = scope.launch {
            val accumulatedWords = mutableListOf<String>()
            while (isActive && _isListening.value) {
                // Animate audio waveform
                _audioRms.value = (0.2f + Random.nextFloat() * 0.7f)
                onRmsChanged?.invoke(_audioRms.value)
                delay(600)

                if (wordsToSimulate.isNotEmpty() && currentIndex < wordsToSimulate.size) {
                    val nextWord = wordsToSimulate[currentIndex]
                    accumulatedWords.add(nextWord)
                    currentIndex++

                    val currentPhrase = accumulatedWords.joinToString(" ")
                    onPartialResult(currentPhrase)

                    if (accumulatedWords.size % 4 == 0 || currentIndex == wordsToSimulate.size) {
                        onFinalResult(currentPhrase)
                        accumulatedWords.clear()
                    }
                }
                delay(700)
            }
        }
    }

    /**
     * Manually inject a recognized token for interactive testing.
     */
    fun injectSpokenText(text: String, onFinalResult: (String) -> Unit) {
        _audioRms.value = 0.8f
        onFinalResult(text)
    }

    override fun stopListening() {
        _isListening.value = false
        _audioRms.value = 0f
        simulationJob?.cancel()
    }

    override fun cancel() {
        stopListening()
    }

    override fun destroy() {
        stopListening()
    }
}
