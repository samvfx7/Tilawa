package com.example.domain.model

data class SpokenToken(
    val rawText: String,
    val normalizedText: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val confidence: Float = 1.0f
)
