package com.example.recognition.session

import com.example.data.model.QuranAyah
import com.example.data.model.QuranSurah
import com.example.data.model.QuranWord
import com.example.domain.model.AlignmentResult
import com.example.domain.model.RecitationReport
import com.example.domain.model.WordAlignmentState
import com.example.recognition.aligner.DynamicQuranAligner
import com.example.recognition.aligner.QuranAligner
import com.example.recognition.analyzer.RecitationAnalyzer
import com.example.recognition.analyzer.RecitationAnalyzerImpl
import com.example.recognition.speech.SpeechError
import com.example.recognition.speech.TilawaSpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class SessionStatus {
    IDLE,
    LISTENING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class RecitationSessionUiState(
    val status: SessionStatus = SessionStatus.IDLE,
    val surah: QuranSurah? = null,
    val selectedAyahs: List<QuranAyah> = emptyList(),
    val wordStates: List<WordAlignmentState> = emptyList(),
    val currentWordIndex: Int = 0,
    val audioRms: Float = 0f,
    val elapsedSeconds: Long = 0L,
    val lastHeardText: String = "",
    val error: SpeechError? = null,
    val finalReport: RecitationReport? = null,
    val isSimulationMode: Boolean = false
)

interface RecitationSession {
    val uiState: StateFlow<RecitationSessionUiState>

    fun startSession(
        surah: QuranSurah,
        startAyah: Int = 1,
        endAyah: Int = surah.totalAyahs,
        isSimulationMode: Boolean = false
    )

    fun pauseSession()
    fun resumeSession()
    fun stopAndGenerateReport(): RecitationReport?
    fun cancelSession()
    fun injectSpokenTextForTest(text: String)
}

class RecitationSessionManager(
    private val speechRecognizer: TilawaSpeechRecognizer,
    private val aligner: QuranAligner = DynamicQuranAligner(),
    private val analyzer: RecitationAnalyzer = RecitationAnalyzerImpl(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : RecitationSession {

    private val _uiState = MutableStateFlow(RecitationSessionUiState())
    override val uiState: StateFlow<RecitationSessionUiState> = _uiState.asStateFlow()

    private var sessionId: String = UUID.randomUUID().toString()
    private var timerJob: Job? = null
    private var targetWords: List<QuranWord> = emptyList()

    override fun startSession(
        surah: QuranSurah,
        startAyah: Int,
        endAyah: Int,
        isSimulationMode: Boolean
    ) {
        cancelSession()
        sessionId = UUID.randomUUID().toString()

        val filteredAyahs = surah.ayahs.filter { it.ayahNumber in startAyah..endAyah }
        val allWords = filteredAyahs.flatMap { it.words }
        targetWords = allWords

        val initialStates = aligner.initializeSession(allWords)

        _uiState.value = RecitationSessionUiState(
            status = SessionStatus.LISTENING,
            surah = surah,
            selectedAyahs = filteredAyahs,
            wordStates = initialStates,
            currentWordIndex = 0,
            audioRms = 0f,
            elapsedSeconds = 0L,
            lastHeardText = "",
            error = null,
            finalReport = null,
            isSimulationMode = isSimulationMode
        )

        startTimer()

        // Start listening
        speechRecognizer.startListening(
            onPartialResult = { partial ->
                handleIncomingSpeech(partial, isFinal = false)
            },
            onFinalResult = { finalResult ->
                handleIncomingSpeech(finalResult, isFinal = true)
            },
            onError = { error ->
                handleSpeechError(error)
            },
            onRmsChanged = { rms ->
                _uiState.value = _uiState.value.copy(audioRms = rms)
            }
        )
    }

    private fun handleIncomingSpeech(speech: String, isFinal: Boolean) {
        if (speech.isBlank() || _uiState.value.status != SessionStatus.LISTENING) return

        val currentState = _uiState.value
        val result: AlignmentResult = aligner.processSpokenText(
            spokenText = speech,
            currentStates = currentState.wordStates,
            currentIndex = currentState.currentWordIndex
        )

        _uiState.value = currentState.copy(
            wordStates = result.wordStates,
            currentWordIndex = result.currentWordIndex,
            lastHeardText = speech
        )

        if (result.isSessionCompleted) {
            stopAndGenerateReport()
        }
    }

    private fun handleSpeechError(error: SpeechError) {
        // If it's a transient speech timeout or minor no-speech, don't break the session immediately
        when (error) {
            is SpeechError.AudioTimeout, is SpeechError.NoSpeechDetected -> {
                // Keep listening
            }
            else -> {
                _uiState.value = _uiState.value.copy(
                    error = error
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                if (_uiState.value.status == SessionStatus.LISTENING) {
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = _uiState.value.elapsedSeconds + 1
                    )
                }
            }
        }
    }

    override fun pauseSession() {
        if (_uiState.value.status == SessionStatus.LISTENING) {
            speechRecognizer.stopListening()
            _uiState.value = _uiState.value.copy(
                status = SessionStatus.PAUSED,
                audioRms = 0f
            )
        }
    }

    override fun resumeSession() {
        if (_uiState.value.status == SessionStatus.PAUSED) {
            _uiState.value = _uiState.value.copy(
                status = SessionStatus.LISTENING,
                error = null
            )
            speechRecognizer.startListening(
                onPartialResult = { partial -> handleIncomingSpeech(partial, false) },
                onFinalResult = { finalResult -> handleIncomingSpeech(finalResult, true) },
                onError = { error -> handleSpeechError(error) },
                onRmsChanged = { rms -> _uiState.value = _uiState.value.copy(audioRms = rms) }
            )
        }
    }

    override fun stopAndGenerateReport(): RecitationReport? {
        speechRecognizer.stopListening()
        timerJob?.cancel()

        val currentState = _uiState.value
        val surah = currentState.surah ?: return null

        val finalizedStates = aligner.finalizeSession(
            currentStates = currentState.wordStates,
            currentIndex = currentState.currentWordIndex
        )

        val report = analyzer.generateReport(
            sessionId = sessionId,
            surahNumber = surah.number,
            surahNameArabic = surah.nameArabic,
            surahNameEnglish = surah.nameEnglish,
            startAyah = currentState.selectedAyahs.firstOrNull()?.ayahNumber ?: 1,
            endAyah = currentState.selectedAyahs.lastOrNull()?.ayahNumber ?: surah.totalAyahs,
            durationSeconds = currentState.elapsedSeconds,
            ayahs = currentState.selectedAyahs,
            wordStates = finalizedStates
        )

        _uiState.value = currentState.copy(
            status = SessionStatus.COMPLETED,
            wordStates = finalizedStates,
            finalReport = report,
            audioRms = 0f
        )

        return report
    }

    override fun cancelSession() {
        speechRecognizer.cancel()
        timerJob?.cancel()
        _uiState.value = RecitationSessionUiState(status = SessionStatus.IDLE)
    }

    override fun injectSpokenTextForTest(text: String) {
        handleIncomingSpeech(text, isFinal = true)
    }
}
