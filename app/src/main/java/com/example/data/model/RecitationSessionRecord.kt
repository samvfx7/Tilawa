package com.example.data.model

data class RecitationSessionRecord(
    val sessionId: String,
    val surahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val startAyah: Int,
    val endAyah: Int,
    val timestamp: Long,
    val durationSeconds: Long,
    val accuracyPercentage: Int,
    val totalWords: Int,
    val correctWords: Int,
    val mistakesCount: Int,
    val skippedWordsCount: Int,
    val ayahsCompleted: Int
)
