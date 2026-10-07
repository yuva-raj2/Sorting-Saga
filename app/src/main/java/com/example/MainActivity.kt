package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.game.CandyViewModel
import com.example.model.GameMode
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    LEVEL_SELECT,
    GAME
}

class MainActivity : ComponentActivity() {
    private val candyViewModel: CandyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1B032D)
                ) {
                    var currentScreen by remember { mutableStateOf(ScreenState.LEVEL_SELECT) }

                    when (currentScreen) {
                        ScreenState.LEVEL_SELECT -> {
                            LevelSelectScreen(
                                viewModel = candyViewModel,
                                onStartGame = { level, mode ->
                                    candyViewModel.startLevel(level, mode)
                                    currentScreen = ScreenState.GAME
                                }
                            )
                        }
                        ScreenState.GAME -> {
                            GameScreen(
                                viewModel = candyViewModel,
                                onBackToLevels = {
                                    currentScreen = ScreenState.LEVEL_SELECT
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
