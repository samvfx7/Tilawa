package com.example.recognition.analyzer

import com.example.data.model.QuranAyah
import com.example.domain.model.PracticeArea
import com.example.domain.model.RecitationReport
import com.example.domain.model.WordAlignmentState
import com.example.domain.model.WordRecitationStatus
import java.util.UUID
import kotlin.math.roundToInt

interface RecitationAnalyzer {
    fun generateReport(
        sessionId: String = UUID.randomUUID().toString(),
        surahNumber: Int,
        surahNameArabic: String,
        surahNameEnglish: String,
        startAyah: Int,
        endAyah: Int,
        durationSeconds: Long,
        ayahs: List<QuranAyah>,
        wordStates: List<WordAlignmentState>
    ): RecitationReport
}

class RecitationAnalyzerImpl : RecitationAnalyzer {

    override fun generateReport(
        sessionId: String,
        surahNumber: Int,
        surahNameArabic: String,
        surahNameEnglish: String,
        startAyah: Int,
        endAyah: Int,
        durationSeconds: Long,
        ayahs: List<QuranAyah>,
        wordStates: List<WordAlignmentState>
    ): RecitationReport {
        val totalWords = wordStates.size
        val correctWords = wordStates.count { it.status == WordRecitationStatus.CORRECT }
        val mistakesCount = wordStates.count { it.status == WordRecitationStatus.MISTAKE }
        val skippedCount = wordStates.count { it.status == WordRecitationStatus.SKIPPED }
        val repeatedCount = wordStates.count { it.status == WordRecitationStatus.REPEATED }

        // Accuracy is calculated based on correct words over total attempted/expected words
        val accuracy = if (totalWords > 0) {
            ((correctWords.toFloat() / totalWords.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
        } else {
            0
        }

        // Calculate ayahs completed (ayahs where all words were recited with >= 80% accuracy)
        val ayahsCompleted = ayahs.count { ayah ->
            val ayahWordStates = wordStates.filter {
                it.word.surahNumber == ayah.surahNumber && it.word.ayahNumber == ayah.ayahNumber
            }
            if (ayahWordStates.isEmpty()) false
            else {
                val correctInAyah = ayahWordStates.count { it.status == WordRecitationStatus.CORRECT }
                (correctInAyah.toFloat() / ayahWordStates.size.toFloat()) >= 0.75f
            }
        }

        // Generate practice areas for ayahs with mistakes or skips
        val practiceAreas = ayahs.mapNotNull { ayah ->
            val ayahWordStates = wordStates.filter {
                it.word.surahNumber == ayah.surahNumber && it.word.ayahNumber == ayah.ayahNumber
            }
            val problematicWords = ayahWordStates.filter {
                it.status == WordRecitationStatus.MISTAKE || it.status == WordRecitationStatus.SKIPPED
            }

            if (problematicWords.isNotEmpty()) {
                val recommendation = when {
                    problematicWords.any { it.status == WordRecitationStatus.SKIPPED } ->
                        "Pay attention to continuity; one or more words were skipped in Ayah ${ayah.ayahNumber}."
                    problematicWords.size >= 2 ->
                        "Review pronunciation and articulation (Makharij) for Ayah ${ayah.ayahNumber}."
                    else ->
                        "Check the marked word in Ayah ${ayah.ayahNumber} for accurate recitation."
                }

                PracticeArea(
                    ayahNumber = ayah.ayahNumber,
                    ayahTextUthmani = ayah.textUthmani,
                    mistakeCount = problematicWords.size,
                    recommendation = recommendation,
                    problematicWords = problematicWords
                )
            } else {
                null
            }
        }

        return RecitationReport(
            sessionId = sessionId,
            surahNumber = surahNumber,
            surahNameArabic = surahNameArabic,
            surahNameEnglish = surahNameEnglish,
            startAyah = startAyah,
            endAyah = endAyah,
            durationSeconds = durationSeconds,
            overallAccuracy = accuracy,
            totalWordsCount = totalWords,
            correctWordsCount = correctWords,
            mistakesCount = mistakesCount,
            skippedWordsCount = skippedCount,
            repeatedWordsCount = repeatedCount,
            ayahsCompleted = ayahsCompleted,
            totalAyahs = ayahs.size,
            detailedWordStates = wordStates,
            practiceAreas = practiceAreas
        )
    }
}
