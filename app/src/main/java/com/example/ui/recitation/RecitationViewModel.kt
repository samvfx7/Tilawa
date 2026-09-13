package com.example.ui.recitation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.QuranSurah
import com.example.data.repository.SessionRepository
import com.example.domain.model.RecitationReport
import com.example.domain.model.WordAlignmentState
import com.example.recognition.aligner.DynamicQuranAligner
import com.example.recognition.analyzer.RecitationAnalyzerImpl
import com.example.recognition.session.RecitationSession
import com.example.recognition.session.RecitationSessionManager
import com.example.recognition.session.RecitationSessionUiState
import com.example.recognition.session.SessionStatus
import com.example.recognition.speech.AndroidSpeechRecognizer
import com.example.recognition.speech.SimulationSpeechRecognizer
import com.example.recognition.speech.TilawaSpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecitationViewModel(
    private val context: Context,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private var activeSession: RecitationSession? = null
    private var speechRecognizer: TilawaSpeechRecognizer? = null

    private val _uiState = MutableStateFlow(RecitationSessionUiState())
    val uiState: StateFlow<RecitationSessionUiState> = _uiState.asStateFlow()

    private val _selectedWordDetail = MutableStateFlow<WordAlignmentState?>(null)
    val selectedWordDetail: StateFlow<WordAlignmentState?> = _selectedWordDetail.asStateFlow()

    fun startRecitation(
        surah: QuranSurah,
        startAyah: Int,
        endAyah: Int,
        isSimulationMode: Boolean
    ) {
        // Destroy any prior recognizer
        speechRecognizer?.destroy()

        // Choose appropriate recognizer
        val recognizer: TilawaSpeechRecognizer = if (isSimulationMode) {
            SimulationSpeechRecognizer().apply {
                val words = surah.ayahs
                    .filter { it.ayahNumber in startAyah..endAyah }
                    .flatMap { it.words }
                    .map { it.textUthmani }
                setWordsSequence(words)
            }
        } else {
            val androidRecognizer = AndroidSpeechRecognizer(context)
            if (androidRecognizer.isAvailable()) {
                androidRecognizer
            } else {
                // Graceful fallback to simulation recognizer if Android speech service is missing
                SimulationSpeechRecognizer().apply {
                    val words = surah.ayahs
                        .filter { it.ayahNumber in startAyah..endAyah }
                        .flatMap { it.words }
                        .map { it.textUthmani }
                    setWordsSequence(words)
                }
            }
        }

        speechRecognizer = recognizer

        val session = RecitationSessionManager(
            speechRecognizer = recognizer,
            aligner = DynamicQuranAligner(),
            analyzer = RecitationAnalyzerImpl(),
            scope = viewModelScope
        )

        activeSession = session

        viewModelScope.launch {
            session.uiState.collect { sessionState ->
                _uiState.value = sessionState
                if (sessionState.status == SessionStatus.COMPLETED && sessionState.finalReport != null) {
                    sessionRepository.saveSession(sessionState.finalReport)
                }
            }
        }

        session.startSession(
            surah = surah,
            startAyah = startAyah,
            endAyah = endAyah,
            isSimulationMode = isSimulationMode
        )
    }

    fun pause() {
        activeSession?.pauseSession()
    }

    fun resume() {
        activeSession?.resumeSession()
    }

    fun stopAndFinish(): RecitationReport? {
        val report = activeSession?.stopAndGenerateReport()
        if (report != null) {
            sessionRepository.saveSession(report)
        }
        return report
    }

    fun cancel() {
        activeSession?.cancelSession()
    }

    fun injectTestWord(text: String) {
        activeSession?.injectSpokenTextForTest(text)
    }

    fun selectWordDetail(wordState: WordAlignmentState?) {
        _selectedWordDetail.value = wordState
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
