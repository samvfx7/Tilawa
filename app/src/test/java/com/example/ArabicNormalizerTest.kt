package com.example

import com.example.recognition.normalizer.ArabicNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicNormalizerTest {

    @Test
    fun testDiacriticStripping() {
        val input = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
        val expected = "بسم الله الرحمن الرحيم"
        val normalized = ArabicNormalizer.normalizeForRecognition(input)
        assertEquals(expected, normalized)
    }

    @Test
    fun testAlifAndHamzaNormalization() {
        val input = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"
        val normalized = ArabicNormalizer.normalizeForRecognition(input)
        assertEquals("اياك نعبد واياك نستعين", normalized)
    }

    @Test
    fun testPhoneticSimilarityExact() {
        val sim = ArabicNormalizer.calculateWordSimilarity("الرحمن", "الرحمن")
        assertTrue(sim >= 0.99f)
    }

    @Test
    fun testPhoneticSimilarityVariants() {
        // Alif with hamza vs plain Alif
        val sim = ArabicNormalizer.calculateWordSimilarity("إياك", "اياك")
        assertTrue(sim >= 0.85f)
    }

    @Test
    fun testPhoneticSimilarityDifferentWords() {
        val sim = ArabicNormalizer.calculateWordSimilarity("الحمد", "نستعين")
        assertTrue(sim < 0.35f)
    }
}
