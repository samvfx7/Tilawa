package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.QuranSurah
import com.example.data.repository.QuranRepositoryImpl
import com.example.data.repository.SessionRepositoryImpl
import com.example.domain.model.RecitationReport
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.recitation.RecitationScreen
import com.example.ui.recitation.RecitationViewModel
import com.example.ui.report.ReportScreen
import com.example.ui.theme.MyApplicationTheme

sealed interface Screen {
    data object Home : Screen
    data class Recitation(
        val surah: QuranSurah,
        val startAyah: Int,
        val endAyah: Int,
        val isSimulation: Boolean
    ) : Screen
    data class Report(val report: RecitationReport) : Screen
}

@Composable
fun TilawaApp() {
    val context = LocalContext.current
    val quranRepository = remember { QuranRepositoryImpl() }
    val sessionRepository = remember { SessionRepositoryImpl(context) }

    val homeViewModel = remember { HomeViewModel(quranRepository, sessionRepository) }
    val homeState by homeViewModel.uiState.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    MyApplicationTheme(
        darkTheme = true,
        amoledTheme = homeState.isAmoledMode
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition",
                modifier = Modifier.padding(innerPadding)
            ) { screen ->
                when (screen) {
                    is Screen.Home -> {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onStartRecitation = { surah, startAyah, endAyah, isSimulation ->
                                currentScreen = Screen.Recitation(surah, startAyah, endAyah, isSimulation)
                            }
                        )
                    }

                    is Screen.Recitation -> {
                        val recitationViewModel = remember(screen.surah.number, screen.startAyah, screen.endAyah) {
                            RecitationViewModel(context, sessionRepository)
                        }

                        RecitationScreen(
                            surah = screen.surah,
                            startAyah = screen.startAyah,
                            endAyah = screen.endAyah,
                            isSimulation = screen.isSimulation,
                            viewModel = recitationViewModel,
                            onFinishRecitation = { report ->
                                currentScreen = Screen.Report(report)
                            },
                            onCancelRecitation = {
                                currentScreen = Screen.Home
                            }
                        )
                    }

                    is Screen.Report -> {
                        ReportScreen(
                            report = screen.report,
                            onPracticeAgain = {
                                val surah = quranRepository.getSurah(screen.report.surahNumber)
                                    ?: quranRepository.getAllSurahs().first()
                                currentScreen = Screen.Recitation(
                                    surah = surah,
                                    startAyah = screen.report.startAyah,
                                    endAyah = screen.report.endAyah,
                                    isSimulation = false
                                )
                            },
                            onBackHome = {
                                currentScreen = Screen.Home
                            }
                        )
                    }
                }
            }
        }
    }
}
