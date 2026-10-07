package com.example.model

enum class GameMode {
    CAMPAIGN,
    ARCADE,
    ZEN
}

enum class BoosterType {
    NONE,
    LOLLIPOP_HAMMER, // Tap any candy to smash it
    FREE_SWITCH,     // Swap any 2 adjacent or non-adjacent candies without losing a move
    COLOR_BOMB       // Drop a rainbow color bomb onto the board
}
