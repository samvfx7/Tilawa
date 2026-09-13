package com.example.ui.recitation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.QuranAyah
import com.example.data.model.QuranSurah
import com.example.domain.model.MistakeType
import com.example.domain.model.RecitationReport
import com.example.domain.model.WordAlignmentState
import com.example.domain.model.WordRecitationStatus
import com.example.recognition.session.SessionStatus
import com.example.ui.components.QuranWordView
import com.example.ui.components.RecitationWaveform
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MistakeRed
import com.example.ui.theme.QuranHeaderTypography
import com.example.ui.theme.QuranTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecitationScreen(
    surah: QuranSurah,
    startAyah: Int,
    endAyah: Int,
    isSimulation: Boolean,
    viewModel: RecitationViewModel,
    onFinishRecitation: (RecitationReport) -> Unit,
    onCancelRecitation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedWordDetail by viewModel.selectedWordDetail.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.startRecitation(surah, startAyah, endAyah, isSimulation)
        } else {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasMicPermission && !isSimulation) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.startRecitation(surah, startAyah, endAyah, isSimulation)
        }
    }

    // Auto-navigate to report when session completes
    LaunchedEffect(uiState.status, uiState.finalReport) {
        if (uiState.status == SessionStatus.COMPLETED && uiState.finalReport != null) {
            onFinishRecitation(uiState.finalReport!!)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp) // Leave space for bottom control bar
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.cancel()
                        onCancelRecitation()
                    },
                    modifier = Modifier.testTag("exit_recitation_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Recitation",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${surah.number}. ${surah.nameEnglish} (${surah.nameArabic})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Ayah $startAyah - $endAyah • ${formatTime(uiState.elapsedSeconds)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Status indicator pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (uiState.status) {
                                SessionStatus.LISTENING -> EmeraldAccent.copy(alpha = 0.2f)
                                SessionStatus.PAUSED -> GoldPrimary.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (uiState.status) {
                            SessionStatus.LISTENING -> "Listening"
                            SessionStatus.PAUSED -> "Paused"
                            SessionStatus.COMPLETED -> "Finished"
                            else -> "Ready"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (uiState.status) {
                                SessionStatus.LISTENING -> EmeraldAccent
                                SessionStatus.PAUSED -> GoldPrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }
            }

            // Main Quran Canvas
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 18.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Bismillah Banner (for all except Surah At-Tawbah #9)
                if (surah.number != 9) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = QuranHeaderTypography.copy(
                                    color = GoldPrimary,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }

                // Ayahs with Word Highlights
                items(uiState.selectedAyahs) { ayah ->
                    AyahRecitationView(
                        ayah = ayah,
                        allWordStates = uiState.wordStates,
                        currentGlobalIndex = uiState.currentWordIndex,
                        onWordClick = { wordState -> viewModel.selectWordDetail(wordState) }
                    )
                }

                // Interactive Simulation/Test Chips (if in simulation or for testing)
                item {
                    val nextExpected = uiState.wordStates.getOrNull(uiState.currentWordIndex)?.word?.textUthmani
                    if (nextExpected != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Recitation Assistant / Test Inputs",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.injectTestWord(nextExpected) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Recite Correct: $nextExpected", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.injectTestWord("خطأ") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MistakeRed.copy(alpha = 0.8f))
                                    ) {
                                        Text("Simulate Mistake", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sticky Audio Visualizer & Controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Detected speech text preview
                if (uiState.lastHeardText.isNotBlank()) {
                    Text(
                        text = "Heard: \"${uiState.lastHeardText}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Audio Waveform
                RecitationWaveform(
                    audioRms = uiState.audioRms,
                    isListening = uiState.status == SessionStatus.LISTENING,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pause / Resume Button
                    IconButton(
                        onClick = {
                            if (uiState.status == SessionStatus.LISTENING) viewModel.pause()
                            else viewModel.resume()
                        },
                        modifier = Modifier
                            .testTag("pause_resume_button")
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (uiState.status == SessionStatus.LISTENING) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.status == SessionStatus.LISTENING) "Pause" else "Resume",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Stop & View Report Button
                    Button(
                        onClick = {
                            val report = viewModel.stopAndFinish()
                            if (report != null) {
                                onFinishRecitation(report)
                            }
                        },
                        modifier = Modifier
                            .testTag("finish_recitation_button")
                            .height(52.dp)
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Finish & Report",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Word Detail Popup / Sheet
        if (selectedWordDetail != null) {
            val state = selectedWordDetail!!
            AlertDialog(
                onDismissRequest = { viewModel.selectWordDetail(null) },
                confirmButton = {
                    TextButton(onClick = { viewModel.selectWordDetail(null) }) {
                        Text("Close")
                    }
                },
                title = {
                    Text(
                        text = state.word.textUthmani,
                        style = QuranTypography.copy(fontSize = 24.sp, color = GoldPrimary)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Status: ${state.status.name}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (state.status) {
                                    WordRecitationStatus.CORRECT -> CorrectGreen
                                    WordRecitationStatus.MISTAKE -> MistakeRed
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        )
                        if (state.spokenText != null) {
                            Text(text = "Heard: ${state.spokenText}")
                        }
                        if (state.mistakeType != MistakeType.NONE) {
                            Text(
                                text = "Issue: ${state.mistakeType.name.replace('_', ' ')}",
                                color = MistakeRed
                            )
                        }
                        Text(
                            text = "Surah ${state.word.surahNumber}, Ayah ${state.word.ayahNumber}, Word #${state.word.positionInAyah}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            )
        }

        // Permission Rationale Dialog
        if (showPermissionRationale) {
            AlertDialog(
                onDismissRequest = { showPermissionRationale = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = GoldPrimary) },
                title = { Text("Microphone Permission") },
                text = {
                    Text("Tilawa needs microphone access to listen to your Quran recitation on-device and correct errors. No audio is ever uploaded.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionRationale = false
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    ) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPermissionRationale = false
                            // Switch to simulation mode
                            viewModel.startRecitation(surah, startAyah, endAyah, isSimulationMode = true)
                        }
                    ) {
                        Text("Use Practice Mode")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AyahRecitationView(
    ayah: QuranAyah,
    allWordStates: List<WordAlignmentState>,
    currentGlobalIndex: Int,
    onWordClick: (WordAlignmentState) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Ayah Badge Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Ayah ${ayah.ayahNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EmeraldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Flow of Words in RTL Layout
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ayah.words.forEach { word ->
                    val wordState = allWordStates.find { it.word.id == word.id }
                        ?: WordAlignmentState(word = word)
                    val isCurrent = allWordStates.getOrNull(currentGlobalIndex)?.word?.id == word.id

                    QuranWordView(
                        state = wordState,
                        isCurrent = isCurrent,
                        onClick = { onWordClick(wordState) }
                    )
                }

                // Ayah end marker (۝)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = " ۝${ayah.ayahNumber} ",
                        style = QuranTypography.copy(
                            fontSize = 18.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
