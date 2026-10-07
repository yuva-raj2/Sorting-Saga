package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.CandyViewModel
import com.example.model.GameMode
import com.example.model.LevelConfig

@Composable
fun LevelSelectScreen(
    viewModel: CandyViewModel,
    onStartGame: (Int, GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showHowToPlay by remember { mutableStateOf(false) }
    val maxUnlocked = viewModel.getMaxUnlockedLevel()
    val isMuted = viewModel.soundManager.isMuted

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF26053D), Color(0xFF450D69), Color(0xFF1B032D))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showHowToPlay = true },
                    modifier = Modifier.testTag("help_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "How to play",
                        tint = Color.White
                    )
                }

                Text(
                    text = "CANDY BURST",
                    color = Color(0xFFFFD54F),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                IconButton(
                    onClick = { viewModel.toggleSound() },
                    modifier = Modifier.testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Sound Toggle",
                        tint = Color.White
                    )
                }
            }

            // Mode Selector Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFFF4081)
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("game_mode_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "CAMPAIGN",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "ARCADE",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "ZEN",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Campaign Levels Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(LevelConfig.levels) { level ->
                            val isUnlocked = level.levelNumber <= maxUnlocked
                            val stars = viewModel.getLevelStars(level.levelNumber)
                            val highScore = viewModel.getLevelHighScore(level.levelNumber)

                            LevelCard(
                                level = level,
                                isUnlocked = isUnlocked,
                                stars = stars,
                                highScore = highScore,
                                onClick = {
                                    if (isUnlocked) {
                                        onStartGame(level.levelNumber, GameMode.CAMPAIGN)
                                    }
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // Arcade Mode Hero Card
                    ArcadeModeCard(
                        onPlayArcade = { onStartGame(1, GameMode.ARCADE) }
                    )
                }
                2 -> {
                    // Zen Mode Hero Card
                    ZenModeCard(
                        onPlayZen = { onStartGame(1, GameMode.ZEN) }
                    )
                }
            }
        }

        // How to play dialog
        if (showHowToPlay) {
            HowToPlayDialog(onDismiss = { showHowToPlay = false })
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelConfig,
    isUnlocked: Boolean,
    stars: Int,
    highScore: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF5E138E) else Color(0xFF280B3E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 6.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 2.dp,
                color = if (isUnlocked) Color(0xFFFFD54F) else Color(0x33FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_card_${level.levelNumber}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isUnlocked) {
                Text(
                    text = "${level.levelNumber}",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Stars row
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..3) {
                        val earned = stars >= i
                        Icon(
                            imageVector = if (earned) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (earned) Color(0xFFFFD600) else Color(0x55FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (highScore > 0) {
                    Text(
                        text = "Best: $highScore",
                        color = Color(0xFFFFE082),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x33000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0x66FFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Lvl ${level.levelNumber}",
                    color = Color(0x66FFFFFF),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ArcadeModeCard(onPlayArcade: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF4A1075),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.5.dp, Color(0xFFFF4081), RoundedCornerShape(28.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "⚡ ARCADE FRENZY",
                    color = Color(0xFFFFD54F),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Race against the 60-second timer! Pull off massive cascade combos to earn bonus score before time expires.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = onPlayArcade,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_arcade_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Text(
                        "PLAY ARCADE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZenModeCard(onPlayZen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF233B6E),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.5.dp, Color(0xFF00E5FF), RoundedCornerShape(28.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🧘 ZEN RELAX",
                    color = Color(0xFF00E5FF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "No move limits. No timers. Just endless, calming candy popping and satisfying cascade chains.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = onPlayZen,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_zen_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Text(
                        "PLAY ZEN",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF320857),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp))
                .testTag("how_to_play_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "HOW TO PLAY",
                    color = Color(0xFFFFD54F),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RuleRow(title = "Swipe to Match", desc = "Drag any candy up, down, left, or right to swap with neighbor.")
                    RuleRow(title = "Match 4: Striped Candy", desc = "Creates a Striped Candy that clears an entire row or column!")
                    RuleRow(title = "T or L: Wrapped Candy", desc = "Creates a Wrapped Candy with explosive 3x3 blast radius!")
                    RuleRow(title = "Match 5: Color Bomb", desc = "Creates a disco Color Bomb. Swap with any candy to clear that entire color!")
                    RuleRow(title = "Clear Jellies", desc = "Make matches on top of frosted jelly tiles to remove them.")
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GOT IT!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RuleRow(title: String, desc: String) {
    Column {
        Text(
            text = "🍬 $title",
            color = Color(0xFFFF80AB),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
