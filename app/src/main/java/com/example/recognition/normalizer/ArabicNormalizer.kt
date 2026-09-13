package com.example.recognition.normalizer

import kotlin.math.max
import kotlin.math.min

object ArabicNormalizer {

    // Arabic Diacritics (Harakat / Tashkeel) range and special Quranic marks
    private val HARAKAT_REGEX = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06DC\\u06DF-\\u06E8\\u06EA-\\u06ED]")
    
    // Quranic Waqf (stopping marks), verse separators, decorative symbols
    private val WAQF_AND_PUNCTUATION_REGEX = Regex("[\\u06D6-\\u06ED\\u06DD\\u06DE۝۩۞\\(\\)\\[\\]\\{\\}،؛؟\\.\\-\\—\\_\\\"\\'\uFD3E\uFD3F]")
    
    // Non-Arabic letters and extraneous symbols except space
    private val EXTRA_SYMBOLS_REGEX = Regex("[^\\u0600-\\u06FF\\s]")
    
    // Tatweel (Kashida)
    private const val TATWEEL = '\u0640'

    /**
     * Strips all Tashkeel / Harakat and Quranic marks from the Arabic string.
     */
    fun stripTashkeel(text: String): String {
        return text
            .replace(HARAKAT_REGEX, "")
            .replace(WAQF_AND_PUNCTUATION_REGEX, "")
            .replace(TATWEEL.toString(), "")
            .trim()
    }

    /**
     * Normalizes Arabic text for accurate speech recognition alignment.
     * Normalizes Alif variants, Taa Marbuta / Haa, Alif Maqsura / Yaa, Hamza variants.
     */
    fun normalizeForRecognition(text: String): String {
        if (text.isBlank()) return ""

        var normalized = stripTashkeel(text)
        normalized = normalized.replace(EXTRA_SYMBOLS_REGEX, "")

        val sb = StringBuilder(normalized.length)
        for (ch in normalized) {
            when (ch) {
                // Alif variants -> Standard Alif
                'أ', 'إ', 'آ', 'ٱ', 'ا', 'ٲ', 'ٳ', 'ٵ' -> sb.append('ا')
                
                // Taa Marbuta -> Haa (reciters often pause with haa or STT recognizes as haa/taa)
                'ة' -> sb.append('ه')
                
                // Alif Maqsura / Yaa variants -> Standard Yaa
                'ى', 'ي', 'ئ', 'ی', 'ۍ', 'ێ' -> sb.append('ي')
                
                // Waw with Hamza -> Waw
                'ؤ' -> sb.append('و')
                
                // Isolated Hamza -> ignored or normalized
                'ء' -> sb.append('ء')
                
                // Arabic numbers or digits
                in '٠'..'٩' -> { /* skip verse numbers in words */ }
                
                // Whitespace normalization
                ' ', '\t', '\n', '\r' -> {
                    if (sb.isNotEmpty() && sb.last() != ' ') {
                        sb.append(' ')
                    }
                }
                
                else -> sb.append(ch)
            }
        }

        return sb.toString().trim()
    }

    /**
     * Tokenizes an Arabic string into individual normalized words.
     */
    fun tokenize(text: String): List<String> {
        val normalized = normalizeForRecognition(text)
        if (normalized.isBlank()) return emptyList()
        return normalized.split("\\s+".toRegex()).filter { it.isNotBlank() }
    }

    /**
     * Computes similarity between an expected Quran word and a spoken word (0.0 to 1.0).
     * Takes into account phonetic closeness in Arabic Tajweed (e.g. Alif-Lam assimilation).
     */
    fun calculateWordSimilarity(expected: String, spoken: String): Float {
        val normExpected = normalizeForRecognition(expected)
        val normSpoken = normalizeForRecognition(spoken)

        if (normExpected.isEmpty() || normSpoken.isEmpty()) return 0.0f
        if (normExpected == normSpoken) return 1.0f

        // Handle common Quranic prefixes like "ال" (Al-) or "و" (Wa-) or "ف" (Fa-) or "ب" (Bi-)
        val expWithoutAl = if (normExpected.startsWith("ال") && normExpected.length > 3) normExpected.substring(2) else normExpected
        val spkWithoutAl = if (normSpoken.startsWith("ال") && normSpoken.length > 3) normSpoken.substring(2) else normSpoken
        if (expWithoutAl == spkWithoutAl) return 0.95f

        // Handle "و" prefix (e.g. والضحى vs الضحى or و vs word)
        val expWithoutWa = if (normExpected.startsWith("و") && normExpected.length > 2) normExpected.substring(1) else normExpected
        val spkWithoutWa = if (normSpoken.startsWith("و") && normSpoken.length > 2) normSpoken.substring(1) else normSpoken
        if (expWithoutWa == spkWithoutWa) return 0.90f

        // Levenshtein distance calculation
        val distance = levenshteinDistance(normExpected, normSpoken)
        val maxLength = max(normExpected.length, normSpoken.length)
        if (maxLength == 0) return 1.0f

        val rawSimilarity = (maxLength - distance).toFloat() / maxLength.toFloat()
        return max(0.0f, min(1.0f, rawSimilarity))
    }

    /**
     * Levenshtein Distance between two strings with Arabic phonetic sensitivity.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) {
                    0
                } else if (isPhoneticallyClose(s1[i - 1], s2[j - 1])) {
                    0 // Count phonetically equivalent Arabic letters as match
                } else {
                    1
                }
                dp[i][j] = min(
                    dp[i - 1][j] + 1, // deletion
                    min(
                        dp[i][j - 1] + 1, // insertion
                        dp[i - 1][j - 1] + cost // substitution
                    )
                )
            }
        }
        return dp[m][n]
    }

    private fun isPhoneticallyClose(c1: Char, c2: Char): Boolean {
        if (c1 == c2) return true
        // Thaa (ث), Seen (س), Saad (ص)
        if ((c1 == 'ث' || c1 == 'س' || c1 == 'ص') && (c2 == 'ث' || c2 == 'س' || c2 == 'ص')) return true
        // Dhal (ذ), Zay (ز), Dhaa (ظ)
        if ((c1 == 'ذ' || c1 == 'ز' || c1 == 'ظ') && (c2 == 'ذ' || c2 == 'ز' || c2 == 'ظ')) return true
        // Qaf (ق), Kaf (ك)
        if ((c1 == 'ق' || c1 == 'ك') && (c2 == 'ق' || c2 == 'ك')) return true
        // Taa (ت), Taa-marbuta (ه/ة), Taa (ط)
        if ((c1 == 'ت' || c1 == 'ط' || c1 == 'ه') && (c2 == 'ت' || c2 == 'ط' || c2 == 'ه')) return true
        // Dhad (ض), Daal (د)
        if ((c1 == 'ض' || c1 == 'د') && (c2 == 'ض' || c2 == 'د')) return true
        // Haa (ح), Haa (ه)
        if ((c1 == 'ح' || c1 == 'ه') && (c2 == 'ح' || c2 == 'ه')) return true
        // Ayn (ع), Hamza (ء/ا)
        if ((c1 == 'ع' || c1 == 'ء' || c1 == 'ا') && (c2 == 'ع' || c2 == 'ء' || c2 == 'ا')) return true
        return false
    }
}
