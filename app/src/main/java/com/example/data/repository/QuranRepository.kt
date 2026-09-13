package com.example.data.repository

import com.example.data.model.QuranSurah
import com.example.data.source.QuranDataSource

interface QuranRepository {
    fun getAllSurahs(): List<QuranSurah>
    fun getSurah(number: Int): QuranSurah?
    fun getFeaturedSurahs(): List<QuranSurah>
    fun searchSurahs(query: String): List<QuranSurah>
}

class QuranRepositoryImpl : QuranRepository {

    override fun getAllSurahs(): List<QuranSurah> {
        return QuranDataSource.ALL_SURAHS_INDEX
    }

    override fun getSurah(number: Int): QuranSurah? {
        // Return full Surah with ayahs and words if present in dataset
        val fullSurah = QuranDataSource.SURAHS_WITH_TEXT[number]
        if (fullSurah != null) return fullSurah

        // Fallback to metadata if full text not loaded
        return QuranDataSource.ALL_SURAHS_INDEX.find { it.number == number }
    }

    override fun getFeaturedSurahs(): List<QuranSurah> {
        val featuredNumbers = listOf(1, 112, 113, 114, 108, 110, 109, 107, 106, 105, 97, 94, 93, 67, 36, 2)
        return featuredNumbers.mapNotNull { getSurah(it) }
    }

    override fun searchSurahs(query: String): List<QuranSurah> {
        if (query.isBlank()) return getAllSurahs()
        val q = query.trim().lowercase()

        return QuranDataSource.ALL_SURAHS_INDEX.filter {
            it.nameEnglish.lowercase().contains(q) ||
            it.nameArabic.contains(q) ||
            it.englishTranslation.lowercase().contains(q) ||
            it.number.toString() == q
        }
    }
}
