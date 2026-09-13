package com.example.data.model

data class QuranAyah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textUthmani: String,
    val textNormalized: String,
    val words: List<QuranWord>
)
