package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CandyColor
import com.example.model.GameMode

@Composable
fun GameHeaderView(
    levelNumber: Int,
    gameMode: GameMode,
    score: Int,
    starThresholds: Triple<Int, Int, Int>,
    starsEarned: Int,
    movesRemaining: Int,
    timeRemainingSec: Int,
    targetScore: Int,
    jelliesRemaining: Int,
    candyProgress: Map<CandyColor, Pair<Int, Int>>, // current to required
    modifier: Modifier = Modifier
) {
    val maxStarScore = starThresholds.third.toFloat().coerceAtLeast(1f)
    val progressRatio by animateFloatAsState(
        targetValue = (score / maxStarScore).coerceIn(0f, 1f),
        label = "score_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top stats bar: Moves / Timer Badge and Target Goal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Moves / Time remaining pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF4A148C),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(20.dp))
                    .testTag("moves_counter_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (gameMode == GameMode.ARCADE) "TIME" else "MOVES",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (gameMode == GameMode.ARCADE) "${timeRemainingSec}s" else if (gameMode == GameMode.ZEN) "∞" else "$movesRemaining",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }
            }

            // Level Objective Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1A0826),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .border(1.5.dp, Color(0xFFBA68C8), RoundedCornerShape(20.dp))
                    .testTag("objective_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (jelliesRemaining > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF4081))
                        )
                        Text(
                            text = "Jellies: $jelliesRemaining",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    } else if (candyProgress.isNotEmpty()) {
                        candyProgress.forEach { (color, progress) ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(color.primaryColor)
                            )
                            Text(
                                text = "${progress.first}/${progress.second}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Target: $targetScore",
                            color = Color(0xFFFFE082),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Score Bar with 3-Star progression
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0x60000000),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCORE: $score",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )

                    // 3 Star Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..3) {
                            val earned = starsEarned >= i
                            Icon(
                                imageVector = if (earned) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Star $i",
                                tint = if (earned) Color(0xFFFFD600) else Color(0x66FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Animated Gradient Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x40FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressRatio)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF4081), Color(0xFFFFD54F), Color(0xFF00E676))
                                )
                            )
                    )
                }
            }
        }
    }
}
