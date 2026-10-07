package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.CandyViewModel
import com.example.game.GameStatus
import com.example.model.GameMode
import com.example.model.LevelConfig
import com.example.ui.components.BoosterBarView
import com.example.ui.components.GameBoardView
import com.example.ui.components.GameHeaderView
import com.example.ui.components.ParticleSystemView
import com.example.ui.dialogs.GameOverDialog
import com.example.ui.dialogs.WinDialog

@Composable
fun GameScreen(
    viewModel: CandyViewModel,
    onBackToLevels: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackToLevels()
    }

    val state by viewModel.uiState.collectAsState()
    val boardVersion by viewModel.boardVersion.collectAsState()
    val selectedTile by viewModel.selectedTile.collectAsState()
    val hintMove by viewModel.hintMove.collectAsState()
    val particles by viewModel.particles.collectAsState()

    val levelConfig = LevelConfig.getLevel(state.currentLevel)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF26053D), Color(0xFF3F0B5E), Color(0xFF1B032D))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation & Settings Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToLevels,
                    modifier = Modifier.testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.gameMode == GameMode.CAMPAIGN) "LEVEL ${state.currentLevel}"
                               else if (state.gameMode == GameMode.ARCADE) "ARCADE FRENZY"
                               else "ZEN MODE",
                        color = Color(0xFFFFD54F),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (state.gameMode == GameMode.CAMPAIGN) {
                        Text(
                            text = levelConfig.title,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.testTag("game_sound_button")
                    ) {
                        Icon(
                            imageVector = if (state.isSoundMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Sound",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleHaptics() },
                        modifier = Modifier.testTag("game_haptics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Toggle Haptics",
                            tint = if (state.isHapticsEnabled) Color(0xFFFF4081) else Color(0x66FFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.startLevel(state.currentLevel, state.gameMode) },
                        modifier = Modifier.testTag("game_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Level",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Game Header (Scores, Star Thresholds, Moves/Timer, Objectives)
            GameHeaderView(
                levelNumber = state.currentLevel,
                gameMode = state.gameMode,
                score = state.score,
                starThresholds = levelConfig.starScores,
                starsEarned = state.starsEarned,
                movesRemaining = state.movesRemaining,
                timeRemainingSec = state.timeRemainingSec,
                targetScore = levelConfig.targetScore,
                jelliesRemaining = state.jelliesRemaining,
                candyProgress = state.candyProgress
            )

            // Board Container with Particle Overlay and Combo Announcer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Game Board Canvas (Keyed to boardVersion so changes recompose efficiently)
                GameBoardView(
                    board = viewModel.board,
                    selectedTile = selectedTile,
                    hintMove = hintMove,
                    onSwipe = { r, c, dir -> viewModel.onSwipe(r, c, dir) },
                    onTileTap = { r, c -> viewModel.onTileTap(r, c) }
                )

                // Particle System View layer rendered on top of board
                ParticleSystemView(particles = particles)

                // Juicy Combo Announcer Banner
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.announcerText != null,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    state.announcerText?.let { text ->
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFE91E63),
                            shadowElevation = 16.dp,
                            modifier = Modifier
                                .border(3.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp))
                                .testTag("announcer_banner")
                        ) {
                            Text(
                                text = text,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            // Power-up Toolbelt at bottom
            BoosterBarView(
                activeBooster = state.activeBooster,
                hammerCount = state.hammerCount,
                freeSwitchCount = state.freeSwitchCount,
                colorBombCount = state.colorBombCount,
                onSelectBooster = { booster -> viewModel.selectBooster(booster) },
                onShuffle = { viewModel.shuffleManual() }
            )

            Spacer(modifier = Modifier.height(6.dp))
        }

        // Victory Dialog
        if (state.gameStatus == GameStatus.WON) {
            WinDialog(
                score = state.score,
                starsEarned = state.starsEarned,
                levelNumber = state.currentLevel,
                hasNextLevel = state.currentLevel < 15,
                onNextLevel = { viewModel.startLevel(state.currentLevel + 1, state.gameMode) },
                onReplay = { viewModel.startLevel(state.currentLevel, state.gameMode) },
                onExit = onBackToLevels
            )
        }

        // Game Over Dialog
        if (state.gameStatus == GameStatus.LOST) {
            GameOverDialog(
                score = state.score,
                targetScore = levelConfig.targetScore,
                onRetry = { viewModel.startLevel(state.currentLevel, state.gameMode) },
                onExit = onBackToLevels
            )
        }
    }
}
