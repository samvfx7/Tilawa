package com.example.domain.model

enum class WordRecitationStatus {
    PENDING,
    CURRENT,
    CORRECT,
    MISTAKE,
    SKIPPED,
    REPEATED
}

enum class MistakeType {
    NONE,
    INCORRECT_PRONUNCIATION,
    SUBSTITUTED_WORD,
    SKIPPED_WORD,
    EXTRA_WORD,
    REPEATED_WORD,
    WRONG_ORDER
}
