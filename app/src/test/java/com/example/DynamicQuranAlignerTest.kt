package com.example

import com.example.data.model.QuranWord
import com.example.domain.model.WordRecitationStatus
import com.example.recognition.aligner.DynamicQuranAligner
import com.example.recognition.normalizer.ArabicNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicQuranAlignerTest {

    private val aligner = DynamicQuranAligner()

    private fun createWords(vararg texts: String): List<QuranWord> {
        return texts.mapIndexed { index, text ->
            QuranWord(
                id = "1_1_${index + 1}",
                surahNumber = 1,
                ayahNumber = 1,
                positionInAyah = index + 1,
                globalPosition = index + 1,
                textUthmani = text,
                textNormalized = ArabicNormalizer.normalizeForRecognition(text)
            )
        }
    }

    @Test
    fun testExactSequenceRecitation() {
        val words = createWords("بسم", "الله", "الرحمن", "الرحيم")
        val initialStates = aligner.initializeSession(words)

        val step1 = aligner.processSpokenText("بسم", initialStates, 0)
        assertEquals(WordRecitationStatus.CORRECT, step1.wordStates[0].status)
        assertEquals(1, step1.currentWordIndex)

        val step2 = aligner.processSpokenText("الله الرحمن", step1.wordStates, step1.currentWordIndex)
        assertEquals(WordRecitationStatus.CORRECT, step2.wordStates[1].status)
        assertEquals(WordRecitationStatus.CORRECT, step2.wordStates[2].status)
        assertEquals(3, step2.currentWordIndex)

        val step3 = aligner.processSpokenText("الرحيم", step2.wordStates, step2.currentWordIndex)
        assertEquals(WordRecitationStatus.CORRECT, step3.wordStates[3].status)
        assertTrue(step3.isSessionCompleted)
    }

    @Test
    fun testSkippedWordDetection() {
        val words = createWords("الحمد", "لله", "رب", "العالمين")
        val initialStates = aligner.initializeSession(words)

        // User says "الحمد" then skips "لله" and says "رب العالمين"
        val step1 = aligner.processSpokenText("الحمد", initialStates, 0)
        val step2 = aligner.processSpokenText("رب العالمين", step1.wordStates, step1.currentWordIndex)

        // "لله" should be marked as SKIPPED
        assertEquals(WordRecitationStatus.SKIPPED, step2.wordStates[1].status)
        assertEquals(WordRecitationStatus.CORRECT, step2.wordStates[2].status)
        assertEquals(WordRecitationStatus.CORRECT, step2.wordStates[3].status)
    }

    @Test
    fun testRepetitionDetection() {
        val words = createWords("قل", "هو", "الله", "احد")
        val initialStates = aligner.initializeSession(words)

        val step1 = aligner.processSpokenText("قل هو الله", initialStates, 0)
        assertEquals(3, step1.currentWordIndex)

        // Reciter repeats "هو الله"
        val step2 = aligner.processSpokenText("هو الله", step1.wordStates, step1.currentWordIndex)
        assertEquals(WordRecitationStatus.REPEATED, step2.wordStates[1].status)
    }
}
