package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.QuranSurah
import com.example.data.model.RecitationSessionRecord
import com.example.data.repository.QuranRepository
import com.example.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val searchQuery: String = "",
    val selectedTab: Int = 0, // 0: Featured / Juz Amma, 1: All Surahs
    val allSurahs: List<QuranSurah> = emptyList(),
    val displayedSurahs: List<QuranSurah> = emptyList(),
    val recentSessions: List<RecitationSessionRecord> = emptyList(),
    val isAmoledMode: Boolean = false,
    val isSimulationMode: Boolean = false,
    val selectedSurahForRange: QuranSurah? = null,
    val startAyah: Int = 1,
    val endAyah: Int = 7
)

class HomeViewModel(
    private val quranRepository: QuranRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeSessions()
    }

    private fun loadData() {
        val all = quranRepository.getAllSurahs()
        val featured = quranRepository.getFeaturedSurahs()

        _uiState.update {
            it.copy(
                allSurahs = all,
                displayedSurahs = featured
            )
        }
    }

    private fun observeSessions() {
        viewModelScope.launch {
            sessionRepository.recentSessions.collect { sessions ->
                _uiState.update { it.copy(recentSessions = sessions) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = if (query.isBlank()) {
                if (current.selectedTab == 0) quranRepository.getFeaturedSurahs() else current.allSurahs
            } else {
                quranRepository.searchSurahs(query)
            }
            current.copy(searchQuery = query, displayedSurahs = filtered)
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { current ->
            val surahs = when (tabIndex) {
                0 -> quranRepository.getFeaturedSurahs()
                else -> current.allSurahs
            }
            val filtered = if (current.searchQuery.isNotBlank()) {
                quranRepository.searchSurahs(current.searchQuery)
            } else {
                surahs
            }
            current.copy(selectedTab = tabIndex, displayedSurahs = filtered)
        }
    }

    fun toggleAmoledMode() {
        _uiState.update { it.copy(isAmoledMode = !it.isAmoledMode) }
    }

    fun toggleSimulationMode() {
        _uiState.update { it.copy(isSimulationMode = !it.isSimulationMode) }
    }

    fun openRangeSelector(surah: QuranSurah) {
        val fullSurah = quranRepository.getSurah(surah.number) ?: surah
        _uiState.update {
            it.copy(
                selectedSurahForRange = fullSurah,
                startAyah = 1,
                endAyah = fullSurah.totalAyahs.coerceAtMost(if (fullSurah.number == 2) 1 else fullSurah.totalAyahs)
            )
        }
    }

    fun closeRangeSelector() {
        _uiState.update { it.copy(selectedSurahForRange = null) }
    }

    fun updateRange(start: Int, end: Int) {
        _uiState.update { it.copy(startAyah = start, endAyah = end) }
    }

    fun clearRecentSessions() {
        sessionRepository.clearHistory()
    }
}
