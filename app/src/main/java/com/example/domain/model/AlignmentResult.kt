package com.example.domain.model

data class AlignmentResult(
    val wordStates: List<WordAlignmentState>,
    val currentWordIndex: Int,
    val newlyIdentifiedMistakes: List<WordAlignmentState> = emptyList(),
    val isAyahCompleted: Boolean = false,
    val isSessionCompleted: Boolean = false,
    val latestSpokenToken: SpokenToken? = null,
    val confidence: Float = 1.0f
)
