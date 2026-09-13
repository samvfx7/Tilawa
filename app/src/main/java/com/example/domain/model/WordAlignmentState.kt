package com.example.domain.model

import com.example.data.model.QuranWord

data class WordAlignmentState(
    val word: QuranWord,
    val status: WordRecitationStatus = WordRecitationStatus.PENDING,
    val spokenText: String? = null,
    val normalizedSpokenText: String? = null,
    val mistakeType: MistakeType = MistakeType.NONE,
    val similarityScore: Float = 0.0f,
    val timestampMs: Long = 0L
)
