package com.example.data.model

data class QuranWord(
    val id: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val positionInAyah: Int,
    val globalPosition: Int,
    val textUthmani: String,
    val textNormalized: String
)
