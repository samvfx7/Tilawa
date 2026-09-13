package com.example.data.model

data class QuranSurah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishTranslation: String,
    val totalAyahs: Int,
    val revelationType: RevelationType,
    val juzNumber: Int,
    val ayahs: List<QuranAyah> = emptyList()
)

enum class RevelationType {
    MECCAN,
    MEDINAN
}
