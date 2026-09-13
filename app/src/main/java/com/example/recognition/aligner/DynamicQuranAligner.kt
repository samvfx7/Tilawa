package com.example.recognition.aligner

import com.example.data.model.QuranWord
import com.example.domain.model.AlignmentResult
import com.example.domain.model.MistakeType
import com.example.domain.model.SpokenToken
import com.example.domain.model.WordAlignmentState
import com.example.domain.model.WordRecitationStatus
import com.example.recognition.normalizer.ArabicNormalizer
import kotlin.math.max
import kotlin.math.min

class DynamicQuranAligner : QuranAligner {

    companion object {
        private const val EXACT_MATCH_THRESHOLD = 0.85f
        private const val PHONETIC_MATCH_THRESHOLD = 0.65f
        private const val LOOKAHEAD_WINDOW = 4
        private const val REPEAT_LOOKBACK_WINDOW = 3
    }

    override fun initializeSession(targetWords: List<QuranWord>): List<WordAlignmentState> {
        return targetWords.mapIndexed { index, word ->
            WordAlignmentState(
                word = word,
                status = if (index == 0) WordRecitationStatus.CURRENT else WordRecitationStatus.PENDING
            )
        }
    }

    override fun processSpokenText(
        spokenText: String,
        currentStates: List<WordAlignmentState>,
        currentIndex: Int
    ): AlignmentResult {
        if (spokenText.isBlank() || currentStates.isEmpty()) {
            return AlignmentResult(
                wordStates = currentStates,
                currentWordIndex = currentIndex
            )
        }

        val spokenWords = ArabicNormalizer.tokenize(spokenText)
        if (spokenWords.isEmpty()) {
            return AlignmentResult(
                wordStates = currentStates,
                currentWordIndex = currentIndex
            )
        }

        var updatedStates = currentStates.toMutableList()
        var pointer = currentIndex.coerceIn(0, currentStates.size - 1)
        val newlyIdentifiedMistakes = mutableListOf<WordAlignmentState>()
        var lastSpokenToken: SpokenToken? = null

        // Iterate through each spoken word from the recognizer
        for (spoken in spokenWords) {
            val normalizedSpoken = ArabicNormalizer.normalizeForRecognition(spoken)
            lastSpokenToken = SpokenToken(
                rawText = spoken,
                normalizedText = normalizedSpoken
            )

            if (pointer >= currentStates.size) {
                // User recited beyond the target range
                break
            }

            val expectedWord = updatedStates[pointer].word
            val directSimilarity = ArabicNormalizer.calculateWordSimilarity(
                expectedWord.textUthmani,
                spoken
            )

            if (directSimilarity >= EXACT_MATCH_THRESHOLD) {
                // 1. Direct Match -> CORRECT
                val state = updatedStates[pointer].copy(
                    status = WordRecitationStatus.CORRECT,
                    spokenText = spoken,
                    normalizedSpokenText = normalizedSpoken,
                    mistakeType = MistakeType.NONE,
                    similarityScore = directSimilarity,
                    timestampMs = System.currentTimeMillis()
                )
                updatedStates[pointer] = state
                pointer++
            } else if (directSimilarity >= PHONETIC_MATCH_THRESHOLD) {
                // 1b. Acceptable phonetic match (minor tajweed pronunciation difference)
                val state = updatedStates[pointer].copy(
                    status = WordRecitationStatus.CORRECT,
                    spokenText = spoken,
                    normalizedSpokenText = normalizedSpoken,
                    mistakeType = MistakeType.NONE,
                    similarityScore = directSimilarity,
                    timestampMs = System.currentTimeMillis()
                )
                updatedStates[pointer] = state
                pointer++
            } else {
                // Check for Repetition: Did the user repeat a recently spoken word?
                val repetitionIndex = findRepetition(spoken, updatedStates, pointer)
                if (repetitionIndex != null) {
                    val repeatedState = updatedStates[repetitionIndex].copy(
                        status = WordRecitationStatus.REPEATED,
                        spokenText = spoken,
                        mistakeType = MistakeType.REPEATED_WORD,
                        timestampMs = System.currentTimeMillis()
                    )
                    updatedStates[repetitionIndex] = repeatedState
                    // Keep pointer at current position
                    continue
                }

                // Check for Skip: Did the user skip ahead to a future word in the window?
                val lookaheadIndex = findLookaheadMatch(spoken, updatedStates, pointer)
                if (lookaheadIndex != null && lookaheadIndex > pointer) {
                    // Mark intermediate words as SKIPPED
                    for (skipIdx in pointer until lookaheadIndex) {
                        val skippedState = updatedStates[skipIdx].copy(
                            status = WordRecitationStatus.SKIPPED,
                            mistakeType = MistakeType.SKIPPED_WORD,
                            timestampMs = System.currentTimeMillis()
                        )
                        updatedStates[skipIdx] = skippedState
                        newlyIdentifiedMistakes.add(skippedState)
                    }

                    // Mark the matched word as CORRECT
                    val matchedState = updatedStates[lookaheadIndex].copy(
                        status = WordRecitationStatus.CORRECT,
                        spokenText = spoken,
                        normalizedSpokenText = normalizedSpoken,
                        mistakeType = MistakeType.NONE,
                        similarityScore = 1.0f,
                        timestampMs = System.currentTimeMillis()
                    )
                    updatedStates[lookaheadIndex] = matchedState
                    pointer = lookaheadIndex + 1
                } else {
                    // 3. Substitution / Mistake at current position
                    val mistakeState = updatedStates[pointer].copy(
                        status = WordRecitationStatus.MISTAKE,
                        spokenText = spoken,
                        normalizedSpokenText = normalizedSpoken,
                        mistakeType = MistakeType.SUBSTITUTED_WORD,
                        similarityScore = directSimilarity,
                        timestampMs = System.currentTimeMillis()
                    )
                    updatedStates[pointer] = mistakeState
                    newlyIdentifiedMistakes.add(mistakeState)
                    pointer++
                }
            }
        }

        // Set CURRENT pointer indicator
        if (pointer < updatedStates.size) {
            val currentWordState = updatedStates[pointer]
            if (currentWordState.status == WordRecitationStatus.PENDING) {
                updatedStates[pointer] = currentWordState.copy(status = WordRecitationStatus.CURRENT)
            }
        }

        val isSessionCompleted = pointer >= updatedStates.size ||
                updatedStates.all { it.status != WordRecitationStatus.PENDING && it.status != WordRecitationStatus.CURRENT }

        return AlignmentResult(
            wordStates = updatedStates,
            currentWordIndex = pointer.coerceAtMost(updatedStates.size - 1),
            newlyIdentifiedMistakes = newlyIdentifiedMistakes,
            isSessionCompleted = isSessionCompleted,
            latestSpokenToken = lastSpokenToken,
            confidence = 1.0f
        )
    }

    private fun findRepetition(
        spoken: String,
        states: List<WordAlignmentState>,
        currentPointer: Int
    ): Int? {
        val startLookback = max(0, currentPointer - REPEAT_LOOKBACK_WINDOW)
        for (i in (currentPointer - 1) downTo startLookback) {
            val word = states[i].word
            val sim = ArabicNormalizer.calculateWordSimilarity(word.textUthmani, spoken)
            if (sim >= EXACT_MATCH_THRESHOLD) {
                return i
            }
        }
        return null
    }

    private fun findLookaheadMatch(
        spoken: String,
        states: List<WordAlignmentState>,
        currentPointer: Int
    ): Int? {
        val endLookahead = min(states.size - 1, currentPointer + LOOKAHEAD_WINDOW)
        for (i in (currentPointer + 1)..endLookahead) {
            val word = states[i].word
            val sim = ArabicNormalizer.calculateWordSimilarity(word.textUthmani, spoken)
            if (sim >= EXACT_MATCH_THRESHOLD) {
                return i
            }
        }
        return null
    }

    override fun finalizeSession(
        currentStates: List<WordAlignmentState>,
        currentIndex: Int
    ): List<WordAlignmentState> {
        return currentStates.mapIndexed { index, state ->
            if (state.status == WordRecitationStatus.PENDING || state.status == WordRecitationStatus.CURRENT) {
                // If the user stopped before reaching these words, mark them as skipped or unrecited
                state.copy(
                    status = WordRecitationStatus.SKIPPED,
                    mistakeType = MistakeType.SKIPPED_WORD
                )
            } else {
                state
            }
        }
    }
}
