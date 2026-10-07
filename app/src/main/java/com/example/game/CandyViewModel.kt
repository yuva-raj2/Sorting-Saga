package com.example.game

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticsManager
import com.example.audio.SoundManager
import com.example.model.BoardTile
import com.example.model.BoosterType
import com.example.model.Candy
import com.example.model.CandyColor
import com.example.model.GameMode
import com.example.model.LevelConfig
import com.example.model.Particle
import com.example.model.ParticleType
import com.example.model.SpecialType
import com.example.model.SwipeDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class GameStatus {
    PLAYING,
    ANIMATING,
    WON,
    LOST,
    PAUSED
}

data class GameUiState(
    val currentLevel: Int = 1,
    val gameMode: GameMode = GameMode.CAMPAIGN,
    val score: Int = 0,
    val movesRemaining: Int = 20,
    val timeRemainingSec: Int = 60,
    val jelliesRemaining: Int = 0,
    val candyProgress: Map<CandyColor, Pair<Int, Int>> = emptyMap(),
    val starsEarned: Int = 0,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val activeBooster: BoosterType = BoosterType.NONE,
    val hammerCount: Int = 3,
    val freeSwitchCount: Int = 2,
    val colorBombCount: Int = 2,
    val announcerText: String? = null,
    val isSoundMuted: Boolean = false,
    val isHapticsEnabled: Boolean = true
)

class CandyViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = CandyBoardEngine()
    val soundManager = SoundManager()
    val hapticsManager = HapticsManager(application)
    private val prefs = application.getSharedPreferences("candy_burst_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var _board = engine.createInitialBoard()
    val board: Array<Array<BoardTile>> get() = _board

    private val _boardVersion = MutableStateFlow(0)
    val boardVersion: StateFlow<Int> = _boardVersion.asStateFlow()

    private val _selectedTile = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedTile: StateFlow<Pair<Int, Int>?> = _selectedTile.asStateFlow()

    private val _hintMove = MutableStateFlow<Pair<Pair<Int, Int>, Pair<Int, Int>>?>(null)
    val hintMove: StateFlow<Pair<Pair<Int, Int>, Pair<Int, Int>>?> = _hintMove.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private var currentConfig = LevelConfig.getLevel(1)
    private var particleIdGen = 1L
    private var arcadeTimerJob: Job? = null
    private var hintJob: Job? = null
    private var lastMoveTime = System.currentTimeMillis()

    init {
        startParticleTicker()
        startLevel(1, GameMode.CAMPAIGN)
    }

    fun startLevel(levelNumber: Int, mode: GameMode = GameMode.CAMPAIGN) {
        currentConfig = LevelConfig.getLevel(levelNumber)
        _board = engine.createInitialBoard(
            rows = currentConfig.rows,
            cols = currentConfig.cols,
            jellyMap = if (mode == GameMode.CAMPAIGN) currentConfig.jellyMap else emptySet()
        )
        _boardVersion.value++

        val initialProgress = currentConfig.candyRequirements.mapValues { 0 to it.value }

        _uiState.value = _uiState.value.copy(
            currentLevel = levelNumber,
            gameMode = mode,
            score = 0,
            movesRemaining = if (mode == GameMode.ZEN) 999 else currentConfig.maxMoves,
            timeRemainingSec = 60,
            jelliesRemaining = if (mode == GameMode.CAMPAIGN) currentConfig.targetJellies else 0,
            candyProgress = initialProgress,
            starsEarned = 0,
            gameStatus = GameStatus.PLAYING,
            activeBooster = BoosterType.NONE,
            announcerText = null
        )
        _selectedTile.value = null
        _hintMove.value = null
        lastMoveTime = System.currentTimeMillis()
        restartHintTimer()

        if (mode == GameMode.ARCADE) {
            startArcadeTimer()
        } else {
            arcadeTimerJob?.cancel()
        }
    }

    private fun startArcadeTimer() {
        arcadeTimerJob?.cancel()
        arcadeTimerJob = viewModelScope.launch {
            while (isActive && _uiState.value.timeRemainingSec > 0 && _uiState.value.gameStatus == GameStatus.PLAYING) {
                delay(1000)
                val newTime = _uiState.value.timeRemainingSec - 1
                _uiState.value = _uiState.value.copy(timeRemainingSec = newTime)
                if (newTime <= 0) {
                    onGameOver()
                }
            }
        }
    }

    private fun restartHintTimer() {
        hintJob?.cancel()
        _hintMove.value = null
        hintJob = viewModelScope.launch {
            delay(4000) // Show hint after 4s idle
            if (_uiState.value.gameStatus == GameStatus.PLAYING) {
                _hintMove.value = engine.findHintMove(_board)
            }
        }
    }

    fun onSwipe(r: Int, c: Int, direction: SwipeDirection) {
        if (_uiState.value.gameStatus != GameStatus.PLAYING) return
        val targetPos = when (direction) {
            SwipeDirection.UP -> (r - 1) to c
            SwipeDirection.DOWN -> (r + 1) to c
            SwipeDirection.LEFT -> r to (c - 1)
            SwipeDirection.RIGHT -> r to (c + 1)
        }

        if (targetPos.first !in 0 until _board.size || targetPos.second !in 0 until _board[0].size) {
            return
        }

        performSwap(r, c, targetPos.first, targetPos.second)
    }

    fun onTileTap(r: Int, c: Int) {
        if (_uiState.value.gameStatus != GameStatus.PLAYING) return

        // Handle booster selection
        when (_uiState.value.activeBooster) {
            BoosterType.LOLLIPOP_HAMMER -> {
                useHammer(r, c)
                return
            }
            BoosterType.FREE_SWITCH -> {
                handleFreeSwitchTap(r, c)
                return
            }
            BoosterType.COLOR_BOMB -> {
                useColorBombBooster(r, c)
                return
            }
            BoosterType.NONE -> {}
        }

        val current = _selectedTile.value
        if (current == null) {
            _selectedTile.value = r to c
            soundManager.playSwap()
            hapticsManager.tap()
        } else if (current.first == r && current.second == c) {
            _selectedTile.value = null // Deselect
        } else {
            val dr = kotlin.math.abs(current.first - r)
            val dc = kotlin.math.abs(current.second - c)
            if (dr + dc == 1) {
                // Adjacent! Perform swap
                performSwap(current.first, current.second, r, c)
                _selectedTile.value = null
            } else {
                // Select new tile
                _selectedTile.value = r to c
                soundManager.playSwap()
                hapticsManager.tap()
            }
        }
    }

    private fun performSwap(r1: Int, c1: Int, r2: Int, c2: Int) {
        _hintMove.value = null
        restartHintTimer()

        val canSwap = engine.canSwap(r1, c1, r2, c2, _board)
        if (!canSwap) {
            soundManager.playInvalid()
            hapticsManager.tap()
            // Quick visual swap & revert
            viewModelScope.launch {
                val candy1 = _board[r1][c1].candy
                val candy2 = _board[r2][c2].candy
                _board[r1][c1].candy = candy2
                _board[r2][c2].candy = candy1
                _boardVersion.value++
                delay(150)
                _board[r1][c1].candy = candy1
                _board[r2][c2].candy = candy2
                _boardVersion.value++
            }
            return
        }

        // Valid swap!
        soundManager.playSwap()
        hapticsManager.tap()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(gameStatus = GameStatus.ANIMATING)

            // Swap candies on board
            val candy1 = _board[r1][c1].candy
            val candy2 = _board[r2][c2].candy
            _board[r1][c1].candy = candy2
            _board[r2][c2].candy = candy1
            _boardVersion.value++

            // Deduct move
            if (_uiState.value.gameMode != GameMode.ZEN) {
                val newMoves = _uiState.value.movesRemaining - 1
                _uiState.value = _uiState.value.copy(movesRemaining = newMoves)
            }

            delay(120)

            // Check if this was a direct special combo swap
            val directComboResult = engine.resolveDirectSpecialSwap(r1, c1, r2, c2, _board)
            if (directComboResult != null) {
                soundManager.playExplosion()
                hapticsManager.specialBlast()
                handleClearResult(directComboResult, comboMultiplier = 1)
                _boardVersion.value++
                delay(250)
                runGravityAndCascadeLoop(initialCombo = 2)
            } else {
                // Regular match & cascade loop
                runCascadeCycle(lastSwappedPos = r2 to c2)
            }
        }
    }

    private suspend fun runCascadeCycle(lastSwappedPos: Pair<Int, Int>? = null) {
        var combo = 1
        var matches = engine.findMatches(_board, lastSwappedPos)

        while (matches.isNotEmpty()) {
            soundManager.playMatch(combo)
            hapticsManager.match()

            if (combo >= 2) {
                val announcer = when (combo) {
                    2 -> "TASTY!"
                    3 -> "SWEET!"
                    4 -> "DELICIOUS!"
                    else -> "SUGAR CRUSH!"
                }
                triggerAnnouncer(announcer)
            }

            val clearResult = engine.resolveMatches(_board, matches)
            handleClearResult(clearResult, comboMultiplier = combo)
            _boardVersion.value++
            delay(280)

            // Drop down candies
            val hadFalls = engine.applyGravity(_board)
            if (hadFalls) {
                _boardVersion.value++
                delay(220)
            }

            combo++
            matches = engine.findMatches(_board)
        }

        finishMoveEvaluation()
    }

    private suspend fun runGravityAndCascadeLoop(initialCombo: Int) {
        val hadFalls = engine.applyGravity(_board)
        if (hadFalls) {
            _boardVersion.value++
            delay(220)
        }

        var combo = initialCombo
        var matches = engine.findMatches(_board)
        while (matches.isNotEmpty()) {
            soundManager.playMatch(combo)
            hapticsManager.match()

            val clearResult = engine.resolveMatches(_board, matches)
            handleClearResult(clearResult, comboMultiplier = combo)
            _boardVersion.value++
            delay(280)

            engine.applyGravity(_board)
            _boardVersion.value++
            delay(220)

            combo++
            matches = engine.findMatches(_board)
        }

        finishMoveEvaluation()
    }

    private fun handleClearResult(result: ClearResult, comboMultiplier: Int) {
        val clearedCount = result.clearedTiles.size
        val points = clearedCount * 60 * comboMultiplier
        val newScore = _uiState.value.score + points

        // Calculate stars
        val stars = when {
            newScore >= currentConfig.starScores.third -> 3
            newScore >= currentConfig.starScores.second -> 2
            newScore >= currentConfig.starScores.first -> 1
            else -> 0
        }

        // Update jellies
        val newJellies = (_uiState.value.jelliesRemaining - result.jelliesCleared).coerceAtLeast(0)

        // Update candy progress
        val updatedProgress = _uiState.value.candyProgress.toMutableMap()
        result.clearedColorCounts.forEach { (color, count) ->
            updatedProgress[color]?.let { (curr, req) ->
                updatedProgress[color] = (curr + count).coerceAtMost(req) to req
            }
        }

        _uiState.value = _uiState.value.copy(
            score = newScore,
            starsEarned = stars,
            jelliesRemaining = newJellies,
            candyProgress = updatedProgress
        )

        // Spawn visual particle explosions for cleared tiles
        spawnTileBurstParticles(result.clearedTiles, points, result.activatedSpecials)
    }

    private fun spawnTileBurstParticles(
        tiles: Set<Pair<Int, Int>>,
        score: Int,
        activatedSpecials: List<SpecialType>
    ) {
        val newParticles = mutableListOf<Particle>()
        val cols = _board[0].size
        val rows = _board.size

        // Reference canvas size assumption for particle coordinates (normalized ~400dp width)
        val screenW = 800f
        val cellW = screenW / cols
        val cellH = cellW

        tiles.forEach { (r, c) ->
            val cx = c * cellW + cellW / 2
            val cy = r * cellH + cellH / 2
            val color = CandyColor.random().primaryColor

            // Exploding shards
            for (i in 0 until 8) {
                val angle = (Math.random() * 2 * PI).toFloat()
                val speed = (180f + Math.random() * 240f).toFloat()
                newParticles.add(
                    Particle(
                        id = particleIdGen++,
                        x = cx,
                        y = cy,
                        vx = (cos(angle) * speed),
                        vy = (sin(angle) * speed),
                        color = color,
                        size = (12f + Math.random() * 10f).toFloat(),
                        life = 0.6f,
                        maxLife = 0.6f,
                        type = ParticleType.SHARD,
                        vRot = ((Math.random() - 0.5) * 500).toFloat()
                    )
                )
            }

            // Sparkle stars
            for (i in 0 until 4) {
                newParticles.add(
                    Particle(
                        id = particleIdGen++,
                        x = cx + ((Math.random() - 0.5) * 20).toFloat(),
                        y = cy + ((Math.random() - 0.5) * 20).toFloat(),
                        vx = ((Math.random() - 0.5) * 60).toFloat(),
                        vy = ((Math.random() - 0.5) * 60).toFloat(),
                        color = Color(0xFFFFEB3B),
                        size = 14f,
                        life = 0.5f,
                        maxLife = 0.5f,
                        type = ParticleType.STAR
                    )
                )
            }
        }

        // Special effect particles
        activatedSpecials.forEach { special ->
            when (special) {
                SpecialType.STRIPED_HORIZONTAL -> {
                    tiles.firstOrNull()?.let { (r, _) ->
                        newParticles.add(
                            Particle(
                                id = particleIdGen++,
                                x = screenW / 2,
                                y = r * cellH + cellH / 2,
                                vx = 0f,
                                vy = 0f,
                                color = Color(0xFFFFD54F),
                                size = cellH * 0.8f,
                                life = 0.4f,
                                maxLife = 0.4f,
                                type = ParticleType.LASER_BEAM_H,
                                length = 200f
                            )
                        )
                    }
                }
                SpecialType.STRIPED_VERTICAL -> {
                    tiles.firstOrNull()?.let { (_, c) ->
                        newParticles.add(
                            Particle(
                                id = particleIdGen++,
                                x = c * cellW + cellW / 2,
                                y = screenW / 2,
                                vx = 0f,
                                vy = 0f,
                                color = Color(0xFFFFD54F),
                                size = cellW * 0.8f,
                                life = 0.4f,
                                maxLife = 0.4f,
                                type = ParticleType.LASER_BEAM_V,
                                length = 200f
                            )
                        )
                    }
                }
                SpecialType.WRAPPED, SpecialType.COLOR_BOMB -> {
                    tiles.firstOrNull()?.let { (r, c) ->
                        newParticles.add(
                            Particle(
                                id = particleIdGen++,
                                x = c * cellW + cellW / 2,
                                y = r * cellH + cellH / 2,
                                vx = 0f,
                                vy = 0f,
                                color = Color(0xFFFF4081),
                                size = 20f,
                                life = 0.5f,
                                maxLife = 0.5f,
                                type = ParticleType.RING_SHOCKWAVE
                            )
                        )
                    }
                }
                SpecialType.NONE -> {}
            }
        }

        // Floating score popup
        tiles.firstOrNull()?.let { (r, c) ->
            newParticles.add(
                Particle(
                    id = particleIdGen++,
                    x = c * cellW + cellW / 2,
                    y = r * cellH + cellH / 2,
                    vx = 0f,
                    vy = -60f,
                    color = Color(0xFFFFEB3B),
                    size = 32f,
                    life = 0.8f,
                    maxLife = 0.8f,
                    type = ParticleType.FLOATING_TEXT,
                    text = "+$score"
                )
            )
        }

        _particles.value = _particles.value + newParticles
    }

    private fun finishMoveEvaluation() {
        val state = _uiState.value

        // Check Win Condition
        val scoreMet = state.score >= currentConfig.targetScore
        val jelliesMet = state.jelliesRemaining == 0
        val candiesMet = state.candyProgress.all { it.value.first >= it.value.second }

        if (scoreMet && jelliesMet && candiesMet) {
            soundManager.playWin()
            saveLevelProgress(state.currentLevel, state.starsEarned, state.score)
            _uiState.value = state.copy(gameStatus = GameStatus.WON)
            triggerConfetti()
            return
        }

        // Check Loss Condition
        if (state.gameMode != GameMode.ZEN && state.movesRemaining <= 0) {
            onGameOver()
            return
        }

        // Check if there are valid moves left; if not, shuffle board
        if (!engine.hasValidMoves(_board)) {
            triggerAnnouncer("SHUFFLING!")
            soundManager.playColorBomb()
            engine.shuffleBoard(_board)
            _boardVersion.value++
        }

        _uiState.value = _uiState.value.copy(gameStatus = GameStatus.PLAYING)
        restartHintTimer()
    }

    private fun onGameOver() {
        soundManager.playInvalid()
        _uiState.value = _uiState.value.copy(gameStatus = GameStatus.LOST)
    }

    private fun triggerAnnouncer(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(announcerText = text)
            delay(1200)
            _uiState.value = _uiState.value.copy(announcerText = null)
        }
    }

    private fun triggerConfetti() {
        val confetti = mutableListOf<Particle>()
        val colors = listOf(Color(0xFFFF1744), Color(0xFFFFD600), Color(0xFF00E676), Color(0xFF00B0FF), Color(0xFFAA00FF))
        for (i in 0 until 60) {
            confetti.add(
                Particle(
                    id = particleIdGen++,
                    x = (Math.random() * 800).toFloat(),
                    y = -20f,
                    vx = ((Math.random() - 0.5) * 150).toFloat(),
                    vy = (100f + Math.random() * 200).toFloat(),
                    color = colors.random(),
                    size = (10f + Math.random() * 8).toFloat(),
                    life = 2.5f,
                    maxLife = 2.5f,
                    type = ParticleType.CONFETTI,
                    vRot = ((Math.random() - 0.5) * 360).toFloat()
                )
            )
        }
        _particles.value = _particles.value + confetti
    }

    // Boosters
    fun selectBooster(booster: BoosterType) {
        _uiState.value = _uiState.value.copy(activeBooster = booster)
        _selectedTile.value = null
    }

    private fun useHammer(r: Int, c: Int) {
        if (_uiState.value.hammerCount <= 0) return
        val tile = _board[r][c]
        if (tile.candy != null) {
            tile.candy = null
            soundManager.playExplosion()
            hapticsManager.specialBlast()
            _uiState.value = _uiState.value.copy(
                hammerCount = _uiState.value.hammerCount - 1,
                activeBooster = BoosterType.NONE
            )
            _boardVersion.value++
            viewModelScope.launch {
                runGravityAndCascadeLoop(initialCombo = 1)
            }
        }
    }

    private var switchFirstTile: Pair<Int, Int>? = null
    private fun handleFreeSwitchTap(r: Int, c: Int) {
        if (_uiState.value.freeSwitchCount <= 0) return
        val first = switchFirstTile
        if (first == null) {
            switchFirstTile = r to c
            _selectedTile.value = r to c
            soundManager.playSwap()
        } else {
            val candy1 = _board[first.first][first.second].candy
            val candy2 = _board[r][c].candy
            _board[first.first][first.second].candy = candy2
            _board[r][c].candy = candy1
            switchFirstTile = null
            _selectedTile.value = null
            soundManager.playSwap()
            hapticsManager.tap()
            _uiState.value = _uiState.value.copy(
                freeSwitchCount = _uiState.value.freeSwitchCount - 1,
                activeBooster = BoosterType.NONE
            )
            _boardVersion.value++
            viewModelScope.launch {
                runCascadeCycle()
            }
        }
    }

    private fun useColorBombBooster(r: Int, c: Int) {
        if (_uiState.value.colorBombCount <= 0) return
        _board[r][c].candy = engine.generateCandy(CandyColor.RED, SpecialType.COLOR_BOMB)
        soundManager.playColorBomb()
        hapticsManager.specialBlast()
        _uiState.value = _uiState.value.copy(
            colorBombCount = _uiState.value.colorBombCount - 1,
            activeBooster = BoosterType.NONE
        )
        _boardVersion.value++
    }

    fun shuffleManual() {
        soundManager.playColorBomb()
        hapticsManager.tap()
        engine.shuffleBoard(_board)
        _boardVersion.value++
        triggerAnnouncer("SHUFFLED!")
    }

    fun toggleSound() {
        soundManager.isMuted = !soundManager.isMuted
        _uiState.value = _uiState.value.copy(isSoundMuted = soundManager.isMuted)
    }

    fun toggleHaptics() {
        hapticsManager.isHapticsEnabled = !hapticsManager.isHapticsEnabled
        _uiState.value = _uiState.value.copy(isHapticsEnabled = hapticsManager.isHapticsEnabled)
    }

    private fun startParticleTicker() {
        viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                delay(16) // ~60fps
                val now = System.currentTimeMillis()
                val dt = (now - lastTime) / 1000f
                lastTime = now

                val current = _particles.value
                if (current.isNotEmpty()) {
                    val remaining = current.filter { it.update(dt) }
                    _particles.value = remaining
                }
            }
        }
    }

    fun getLevelStars(level: Int): Int {
        return prefs.getInt("level_${level}_stars", 0)
    }

    fun getLevelHighScore(level: Int): Int {
        return prefs.getInt("level_${level}_score", 0)
    }

    fun getMaxUnlockedLevel(): Int {
        return prefs.getInt("max_unlocked_level", 1)
    }

    private fun saveLevelProgress(level: Int, stars: Int, score: Int) {
        val oldStars = getLevelStars(level)
        val oldScore = getLevelHighScore(level)
        val maxUnlocked = getMaxUnlockedLevel()

        val editor = prefs.edit()
        if (stars > oldStars) editor.putInt("level_${level}_stars", stars)
        if (score > oldScore) editor.putInt("level_${level}_score", score)
        if (level >= maxUnlocked && level < 15) {
            editor.putInt("max_unlocked_level", level + 1)
        }
        editor.apply()
    }
}
