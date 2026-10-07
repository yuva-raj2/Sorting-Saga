package com.example.model

import androidx.compose.ui.graphics.Color

enum class CandyColor(
    val displayName: String,
    val primaryColor: Color,
    val darkColor: Color,
    val lightColor: Color,
    val accentColor: Color
) {
    RED(
        displayName = "Strawberry Heart",
        primaryColor = Color(0xFFFF1744),
        darkColor = Color(0xFFC4001D),
        lightColor = Color(0xFFFF8A80),
        accentColor = Color(0xFFFF5252)
    ),
    ORANGE(
        displayName = "Orange Lozenge",
        primaryColor = Color(0xFFFF6D00),
        darkColor = Color(0xFFC43E00),
        lightColor = Color(0xFFFFD180),
        accentColor = Color(0xFFFF9100)
    ),
    YELLOW(
        displayName = "Lemon Star",
        primaryColor = Color(0xFFFFD600),
        darkColor = Color(0xFFC79A00),
        lightColor = Color(0xFFFFFF8D),
        accentColor = Color(0xFFFFEA00)
    ),
    GREEN(
        displayName = "Mint Gumdrop",
        primaryColor = Color(0xFF00E676),
        darkColor = Color(0xFF00B248),
        lightColor = Color(0xFFB9F6CA),
        accentColor = Color(0xFF69F0AE)
    ),
    BLUE(
        displayName = "Blueberry Pop",
        primaryColor = Color(0xFF00B0FF),
        darkColor = Color(0xFF0081CB),
        lightColor = Color(0xFF80D8FF),
        accentColor = Color(0xFF40C4FF)
    ),
    PURPLE(
        displayName = "Grape Jewel",
        primaryColor = Color(0xFFAA00FF),
        darkColor = Color(0xFF7200CA),
        lightColor = Color(0xFFEA80FC),
        accentColor = Color(0xFFE040FB)
    );

    companion object {
        fun random(available: List<CandyColor> = entries): CandyColor {
            return available.random()
        }
    }
}

enum class SpecialType {
    NONE,
    STRIPED_HORIZONTAL,
    STRIPED_VERTICAL,
    WRAPPED,
    COLOR_BOMB
}

data class Candy(
    val id: Long,
    val color: CandyColor,
    val special: SpecialType = SpecialType.NONE
) {
    val isSpecial: Boolean get() = special != SpecialType.NONE
}
