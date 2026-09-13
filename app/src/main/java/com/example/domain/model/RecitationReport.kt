package com.example.domain.model

data class RecitationReport(
    val sessionId: String,
    val surahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val startAyah: Int,
    val endAyah: Int,
    val durationSeconds: Long,
    val overallAccuracy: Int,
    val totalWordsCount: Int,
    val correctWordsCount: Int,
    val mistakesCount: Int,
    val skippedWordsCount: Int,
    val repeatedWordsCount: Int,
    val ayahsCompleted: Int,
    val totalAyahs: Int,
    val detailedWordStates: List<WordAlignmentState>,
    val practiceAreas: List<PracticeArea>
)

data class PracticeArea(
    val ayahNumber: Int,
    val ayahTextUthmani: String,
    val mistakeCount: Int,
    val recommendation: String,
    val problematicWords: List<WordAlignmentState>
)
