package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.RecitationSessionRecord
import com.example.domain.model.RecitationReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

interface SessionRepository {
    val recentSessions: StateFlow<List<RecitationSessionRecord>>
    fun saveSession(report: RecitationReport)
    fun getRecentSessions(limit: Int = 10): List<RecitationSessionRecord>
    fun clearHistory()
}

class SessionRepositoryImpl(
    context: Context
) : SessionRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tilawa_recitation_sessions", Context.MODE_PRIVATE)

    private val _recentSessions = MutableStateFlow<List<RecitationSessionRecord>>(emptyList())
    override val recentSessions: StateFlow<List<RecitationSessionRecord>> = _recentSessions.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        val jsonString = prefs.getString("sessions_json", null)
        if (jsonString.isNullOrBlank()) {
            _recentSessions.value = emptyList()
            return
        }

        try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<RecitationSessionRecord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    RecitationSessionRecord(
                        sessionId = obj.optString("sessionId", ""),
                        surahNumber = obj.optInt("surahNumber", 1),
                        surahNameArabic = obj.optString("surahNameArabic", ""),
                        surahNameEnglish = obj.optString("surahNameEnglish", ""),
                        startAyah = obj.optInt("startAyah", 1),
                        endAyah = obj.optInt("endAyah", 1),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        durationSeconds = obj.optLong("durationSeconds", 0L),
                        accuracyPercentage = obj.optInt("accuracyPercentage", 0),
                        totalWords = obj.optInt("totalWords", 0),
                        correctWords = obj.optInt("correctWords", 0),
                        mistakesCount = obj.optInt("mistakesCount", 0),
                        skippedWordsCount = obj.optInt("skippedWordsCount", 0),
                        ayahsCompleted = obj.optInt("ayahsCompleted", 0)
                    )
                )
            }
            _recentSessions.value = list
        } catch (e: Exception) {
            _recentSessions.value = emptyList()
        }
    }

    override fun saveSession(report: RecitationReport) {
        val record = RecitationSessionRecord(
            sessionId = report.sessionId,
            surahNumber = report.surahNumber,
            surahNameArabic = report.surahNameArabic,
            surahNameEnglish = report.surahNameEnglish,
            startAyah = report.startAyah,
            endAyah = report.endAyah,
            timestamp = System.currentTimeMillis(),
            durationSeconds = report.durationSeconds,
            accuracyPercentage = report.overallAccuracy,
            totalWords = report.totalWordsCount,
            correctWords = report.correctWordsCount,
            mistakesCount = report.mistakesCount,
            skippedWordsCount = report.skippedWordsCount,
            ayahsCompleted = report.ayahsCompleted
        )

        val updated = mutableListOf(record).apply {
            addAll(_recentSessions.value.filter { it.sessionId != record.sessionId })
        }.take(30)

        _recentSessions.value = updated

        // Persist to SharedPreferences
        try {
            val jsonArray = JSONArray()
            for (item in updated) {
                val obj = JSONObject().apply {
                    put("sessionId", item.sessionId)
                    put("surahNumber", item.surahNumber)
                    put("surahNameArabic", item.surahNameArabic)
                    put("surahNameEnglish", item.surahNameEnglish)
                    put("startAyah", item.startAyah)
                    put("endAyah", item.endAyah)
                    put("timestamp", item.timestamp)
                    put("durationSeconds", item.durationSeconds)
                    put("accuracyPercentage", item.accuracyPercentage)
                    put("totalWords", item.totalWords)
                    put("correctWords", item.correctWords)
                    put("mistakesCount", item.mistakesCount)
                    put("skippedWordsCount", item.skippedWordsCount)
                    put("ayahsCompleted", item.ayahsCompleted)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("sessions_json", jsonArray.toString()).apply()
        } catch (e: Exception) {
            // Graceful fallback
        }
    }

    override fun getRecentSessions(limit: Int): List<RecitationSessionRecord> {
        return _recentSessions.value.take(limit)
    }

    override fun clearHistory() {
        prefs.edit().remove("sessions_json").apply()
        _recentSessions.value = emptyList()
    }
}
