package com.example.recognition.aligner

import com.example.data.model.QuranWord
import com.example.domain.model.AlignmentResult
import com.example.domain.model.SpokenToken
import com.example.domain.model.WordAlignmentState

interface QuranAligner {
    /**
     * Initializes the aligner with the target list of Quran words for the session.
     */
    fun initializeSession(targetWords: List<QuranWord>): List<WordAlignmentState>

    /**
     * Processes new incoming spoken tokens against the expected Quran words.
     * Updates states and advances the current word pointer.
     */
    fun processSpokenText(
        spokenText: String,
        currentStates: List<WordAlignmentState>,
        currentIndex: Int
    ): AlignmentResult

    /**
     * Finalizes the session by marking all unreached words as pending or unrecited.
     */
    fun finalizeSession(
        currentStates: List<WordAlignmentState>,
        currentIndex: Int
    ): List<WordAlignmentState>
}
