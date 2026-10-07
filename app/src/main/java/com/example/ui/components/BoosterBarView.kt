package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoosterType

@Composable
fun BoosterBarView(
    activeBooster: BoosterType,
    hammerCount: Int,
    freeSwitchCount: Int,
    colorBombCount: Int,
    onSelectBooster: (BoosterType) -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0x801F0633),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.5.dp, Color(0x40FFFFFF), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoosterButton(
                icon = Icons.Default.Build,
                label = "Hammer",
                count = hammerCount,
                isActive = activeBooster == BoosterType.LOLLIPOP_HAMMER,
                tag = "booster_hammer",
                onClick = {
                    onSelectBooster(
                        if (activeBooster == BoosterType.LOLLIPOP_HAMMER) BoosterType.NONE
                        else BoosterType.LOLLIPOP_HAMMER
                    )
                }
            )

            BoosterButton(
                icon = Icons.Default.ChangeCircle,
                label = "Switch",
                count = freeSwitchCount,
                isActive = activeBooster == BoosterType.FREE_SWITCH,
                tag = "booster_switch",
                onClick = {
                    onSelectBooster(
                        if (activeBooster == BoosterType.FREE_SWITCH) BoosterType.NONE
                        else BoosterType.FREE_SWITCH
                    )
                }
            )

            BoosterButton(
                icon = Icons.Default.AutoAwesome,
                label = "Color Bomb",
                count = colorBombCount,
                isActive = activeBooster == BoosterType.COLOR_BOMB,
                tag = "booster_color_bomb",
                onClick = {
                    onSelectBooster(
                        if (activeBooster == BoosterType.COLOR_BOMB) BoosterType.NONE
                        else BoosterType.COLOR_BOMB
                    )
                }
            )

            BoosterButton(
                icon = Icons.Default.Refresh,
                label = "Shuffle",
                count = -1, // always available
                isActive = false,
                tag = "booster_shuffle",
                onClick = onShuffle
            )
        }
    }
}

@Composable
private fun BoosterButton(
    icon: ImageVector,
    label: String,
    count: Int,
    isActive: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) Color(0xFFFFD54F) else Color(0x33FFFFFF)
    val iconColor = if (isActive) Color(0xFF21004A) else Color.White
    val borderColor = if (isActive) Color.White else Color(0x30FFFFFF)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )

            if (count >= 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF1744)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = label,
            color = if (isActive) Color(0xFFFFD54F) else Color(0xCCFFFFFF),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
