package com.example.game

import com.example.model.BoardTile
import com.example.model.Candy
import com.example.model.CandyColor
import com.example.model.SpecialType

data class MatchGroup(
    val tiles: Set<Pair<Int, Int>>,
    val color: CandyColor,
    val specialToCreate: SpecialType = SpecialType.NONE,
    val specialSpawnPos: Pair<Int, Int>? = null
)

data class ClearResult(
    val clearedTiles: Set<Pair<Int, Int>>,
    val newSpecialCandies: Map<Pair<Int, Int>, Candy>,
    val clearedColorCounts: Map<CandyColor, Int>,
    val activatedSpecials: List<SpecialType>,
    val jelliesCleared: Int
)

class CandyBoardEngine {
    private var nextCandyId = 1L

    fun generateCandy(
        color: CandyColor = CandyColor.random(),
        special: SpecialType = SpecialType.NONE
    ): Candy {
        return Candy(
            id = nextCandyId++,
            color = color,
            special = special
        )
    }

    fun createInitialBoard(
        rows: Int = 8,
        cols: Int = 8,
        jellyMap: Set<Pair<Int, Int>> = emptySet()
    ): Array<Array<BoardTile>> {
        val board = Array(rows) { r ->
            Array(cols) { c ->
                BoardTile(
                    row = r,
                    col = c,
                    hasJelly = jellyMap.contains(r to c)
                )
            }
        }

        var attempts = 0
        do {
            populateWithoutMatches(board)
            attempts++
        } while (!hasValidMoves(board) && attempts < 50)

        return board
    }

    private fun populateWithoutMatches(board: Array<Array<BoardTile>>) {
        val rows = board.size
        val cols = board[0].size

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val forbiddenColors = mutableSetOf<CandyColor>()
                if (c >= 2) {
                    val c1 = board[r][c - 1].candy?.color
                    val c2 = board[r][c - 2].candy?.color
                    if (c1 != null && c1 == c2) {
                        forbiddenColors.add(c1)
                    }
                }
                if (r >= 2) {
                    val r1 = board[r - 1][c].candy?.color
                    val r2 = board[r - 2][c].candy?.color
                    if (r1 != null && r1 == r2) {
                        forbiddenColors.add(r1)
                    }
                }

                val availableColors = CandyColor.entries.filter { it !in forbiddenColors }
                val chosenColor = if (availableColors.isNotEmpty()) {
                    availableColors.random()
                } else {
                    CandyColor.entries.random()
                }
                board[r][c].candy = generateCandy(chosenColor)
            }
        }
    }

    fun canSwap(
        r1: Int, c1: Int,
        r2: Int, c2: Int,
        board: Array<Array<BoardTile>>
    ): Boolean {
        // Must be adjacent
        val dr = kotlin.math.abs(r1 - r2)
        val dc = kotlin.math.abs(c1 - c2)
        if (dr + dc != 1) return false

        val candy1 = board[r1][c1].candy ?: return false
        val candy2 = board[r2][c2].candy ?: return false

        // Color bomb swapped with any candy is always valid!
        if (candy1.special == SpecialType.COLOR_BOMB || candy2.special == SpecialType.COLOR_BOMB) {
            return true
        }

        // Two special candies swapped together is always valid combo!
        if (candy1.isSpecial && candy2.isSpecial) {
            return true
        }

        // Virtual swap to test match
        board[r1][c1].candy = candy2
        board[r2][c2].candy = candy1
        val matches = findMatches(board)
        // Swap back
        board[r1][c1].candy = candy1
        board[r2][c2].candy = candy2

        return matches.isNotEmpty()
    }

    fun findMatches(
        board: Array<Array<BoardTile>>,
        lastSwappedPos: Pair<Int, Int>? = null
    ): List<MatchGroup> {
        val rows = board.size
        val cols = board[0].size

        val horizontalRuns = mutableListOf<List<Pair<Int, Int>>>()
        val verticalRuns = mutableListOf<List<Pair<Int, Int>>>()

        // Scan rows for horizontal runs
        for (r in 0 until rows) {
            var runStart = 0
            while (runStart < cols) {
                val startCandy = board[r][runStart].candy
                if (startCandy == null || startCandy.special == SpecialType.COLOR_BOMB) {
                    runStart++
                    continue
                }
                var runEnd = runStart + 1
                while (runEnd < cols) {
                    val nextCandy = board[r][runEnd].candy
                    if (nextCandy != null && nextCandy.color == startCandy.color && nextCandy.special != SpecialType.COLOR_BOMB) {
                        runEnd++
                    } else {
                        break
                    }
                }
                if (runEnd - runStart >= 3) {
                    val run = (runStart until runEnd).map { c -> r to c }
                    horizontalRuns.add(run)
                }
                runStart = runEnd
            }
        }

        // Scan columns for vertical runs
        for (c in 0 until cols) {
            var runStart = 0
            while (runStart < rows) {
                val startCandy = board[runStart][c].candy
                if (startCandy == null || startCandy.special == SpecialType.COLOR_BOMB) {
                    runStart++
                    continue
                }
                var runEnd = runStart + 1
                while (runEnd < rows) {
                    val nextCandy = board[runEnd][c].candy
                    if (nextCandy != null && nextCandy.color == startCandy.color && nextCandy.special != SpecialType.COLOR_BOMB) {
                        runEnd++
                    } else {
                        break
                    }
                }
                if (runEnd - runStart >= 3) {
                    val run = (runStart until runEnd).map { r -> r to c }
                    verticalRuns.add(run)
                }
                runStart = runEnd
            }
        }

        val matchGroups = mutableListOf<MatchGroup>()
        val processedHorizontal = BooleanArray(horizontalRuns.size)
        val processedVertical = BooleanArray(verticalRuns.size)

        // Check for T or L intersections (Wrapped candy)
        for (i in horizontalRuns.indices) {
            val hRun = horizontalRuns[i]
            val hColor = board[hRun[0].first][hRun[0].second].candy?.color ?: continue
            for (j in verticalRuns.indices) {
                if (processedVertical[j]) continue
                val vRun = verticalRuns[j]
                val vColor = board[vRun[0].first][vRun[0].second].candy?.color ?: continue
                if (hColor == vColor) {
                    val intersection = hRun.intersect(vRun.toSet())
                    if (intersection.isNotEmpty()) {
                        processedHorizontal[i] = true
                        processedVertical[j] = true
                        val combined = (hRun + vRun).toSet()
                        val spawnPos = intersection.first()
                        matchGroups.add(
                            MatchGroup(
                                tiles = combined,
                                color = hColor,
                                specialToCreate = SpecialType.WRAPPED,
                                specialSpawnPos = spawnPos
                            )
                        )
                    }
                }
            }
        }

        // Process remaining horizontal runs
        for (i in horizontalRuns.indices) {
            if (processedHorizontal[i]) continue
            val run = horizontalRuns[i]
            val color = board[run[0].first][run[0].second].candy?.color ?: continue
            val count = run.size
            val spawnPos = if (lastSwappedPos != null && run.contains(lastSwappedPos)) {
                lastSwappedPos
            } else {
                run[count / 2]
            }

            val special = when {
                count >= 5 -> SpecialType.COLOR_BOMB
                count == 4 -> SpecialType.STRIPED_VERTICAL // vertical stripe clears column
                else -> SpecialType.NONE
            }
            matchGroups.add(
                MatchGroup(
                    tiles = run.toSet(),
                    color = color,
                    specialToCreate = special,
                    specialSpawnPos = if (special != SpecialType.NONE) spawnPos else null
                )
            )
        }

        // Process remaining vertical runs
        for (j in verticalRuns.indices) {
            if (processedVertical[j]) continue
            val run = verticalRuns[j]
            val color = board[run[0].first][run[0].second].candy?.color ?: continue
            val count = run.size
            val spawnPos = if (lastSwappedPos != null && run.contains(lastSwappedPos)) {
                lastSwappedPos
            } else {
                run[count / 2]
            }

            val special = when {
                count >= 5 -> SpecialType.COLOR_BOMB
                count == 4 -> SpecialType.STRIPED_HORIZONTAL // horizontal stripe clears row
                else -> SpecialType.NONE
            }
            matchGroups.add(
                MatchGroup(
                    tiles = run.toSet(),
                    color = color,
                    specialToCreate = special,
                    specialSpawnPos = if (special != SpecialType.NONE) spawnPos else null
                )
            )
        }

        return matchGroups
    }

    fun resolveMatches(
        board: Array<Array<BoardTile>>,
        matches: List<MatchGroup>
    ): ClearResult {
        val rows = board.size
        val cols = board[0].size
        val allTilesToClear = mutableSetOf<Pair<Int, Int>>()
        val newSpecials = mutableMapOf<Pair<Int, Int>, Candy>()
        val clearedColors = mutableMapOf<CandyColor, Int>()
        val activatedSpecials = mutableListOf<SpecialType>()

        for (group in matches) {
            allTilesToClear.addAll(group.tiles)
            if (group.specialToCreate != SpecialType.NONE && group.specialSpawnPos != null) {
                val newCandy = generateCandy(group.color, group.specialToCreate)
                newSpecials[group.specialSpawnPos] = newCandy
            }
        }

        // Expand any special candy activations within the cleared tiles (cascade chain reaction)
        val queue = ArrayDeque(allTilesToClear.toList())
        val processed = mutableSetOf<Pair<Int, Int>>()

        while (queue.isNotEmpty()) {
            val pos = queue.removeFirst()
            if (processed.contains(pos)) continue
            processed.add(pos)

            val tile = board[pos.first][pos.second]
            val candy = tile.candy ?: continue

            when (candy.special) {
                SpecialType.STRIPED_HORIZONTAL -> {
                    activatedSpecials.add(SpecialType.STRIPED_HORIZONTAL)
                    // Clears whole row
                    for (c in 0 until cols) {
                        val p = pos.first to c
                        if (allTilesToClear.add(p)) queue.add(p)
                    }
                }
                SpecialType.STRIPED_VERTICAL -> {
                    activatedSpecials.add(SpecialType.STRIPED_VERTICAL)
                    // Clears whole col
                    for (r in 0 until rows) {
                        val p = r to pos.second
                        if (allTilesToClear.add(p)) queue.add(p)
                    }
                }
                SpecialType.WRAPPED -> {
                    activatedSpecials.add(SpecialType.WRAPPED)
                    // Clears 3x3
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = pos.first + dr
                            val nc = pos.second + dc
                            if (nr in 0 until rows && nc in 0 until cols) {
                                val p = nr to nc
                                if (allTilesToClear.add(p)) queue.add(p)
                            }
                        }
                    }
                }
                SpecialType.COLOR_BOMB -> {
                    activatedSpecials.add(SpecialType.COLOR_BOMB)
                    // Clears a random color or most common on board
                    val targetColor = candy.color
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            if (board[r][c].candy?.color == targetColor) {
                                val p = r to c
                                if (allTilesToClear.add(p)) queue.add(p)
                            }
                        }
                    }
                }
                SpecialType.NONE -> {}
            }
        }

        // Calculate jellies cleared and candy color counts
        var jelliesCleared = 0
        for (pos in allTilesToClear) {
            val tile = board[pos.first][pos.second]
            if (tile.hasJelly) {
                tile.hasJelly = false
                jelliesCleared++
            }
            val candy = tile.candy
            if (candy != null) {
                clearedColors[candy.color] = (clearedColors[candy.color] ?: 0) + 1
            }
            // Clear tile
            tile.candy = null
        }

        // Place newly created special candies
        for ((pos, candy) in newSpecials) {
            board[pos.first][pos.second].candy = candy
            allTilesToClear.remove(pos)
        }

        return ClearResult(
            clearedTiles = allTilesToClear,
            newSpecialCandies = newSpecials,
            clearedColorCounts = clearedColors,
            activatedSpecials = activatedSpecials,
            jelliesCleared = jelliesCleared
        )
    }

    /**
     * Handles direct combo swaps (e.g. Color Bomb + Candy, or Striped + Striped).
     */
    fun resolveDirectSpecialSwap(
        r1: Int, c1: Int,
        r2: Int, c2: Int,
        board: Array<Array<BoardTile>>
    ): ClearResult? {
        val candy1 = board[r1][c1].candy ?: return null
        val candy2 = board[r2][c2].candy ?: return null
        val rows = board.size
        val cols = board[0].size

        val allTilesToClear = mutableSetOf<Pair<Int, Int>>()
        val activatedSpecials = mutableListOf<SpecialType>()
        val clearedColors = mutableMapOf<CandyColor, Int>()
        var jelliesCleared = 0

        // Case 1: Two Color Bombs swapped -> Sugar Crush whole board!
        if (candy1.special == SpecialType.COLOR_BOMB && candy2.special == SpecialType.COLOR_BOMB) {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    allTilesToClear.add(r to c)
                }
            }
            activatedSpecials.add(SpecialType.COLOR_BOMB)
        }
        // Case 2: One Color Bomb swapped with normal candy -> clear all candies of that color
        else if (candy1.special == SpecialType.COLOR_BOMB || candy2.special == SpecialType.COLOR_BOMB) {
            val normalCandy = if (candy1.special == SpecialType.COLOR_BOMB) candy2 else candy1
            val bombPos = if (candy1.special == SpecialType.COLOR_BOMB) r1 to c1 else r2 to c2
            allTilesToClear.add(bombPos)

            val targetColor = normalCandy.color
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val tile = board[r][c]
                    if (tile.candy?.color == targetColor) {
                        allTilesToClear.add(r to c)
                    }
                }
            }
            activatedSpecials.add(SpecialType.COLOR_BOMB)
        }
        // Case 3: Striped + Striped -> Cross blast (both row and col)
        else if (
            (candy1.special == SpecialType.STRIPED_HORIZONTAL || candy1.special == SpecialType.STRIPED_VERTICAL) &&
            (candy2.special == SpecialType.STRIPED_HORIZONTAL || candy2.special == SpecialType.STRIPED_VERTICAL)
        ) {
            for (c in 0 until cols) allTilesToClear.add(r2 to c)
            for (r in 0 until rows) allTilesToClear.add(r to c2)
            activatedSpecials.add(SpecialType.STRIPED_HORIZONTAL)
            activatedSpecials.add(SpecialType.STRIPED_VERTICAL)
        }
        // Case 4: Striped + Wrapped -> Giant 3-row & 3-col cross blast!
        else if (
            (candy1.special == SpecialType.WRAPPED && (candy2.special == SpecialType.STRIPED_HORIZONTAL || candy2.special == SpecialType.STRIPED_VERTICAL)) ||
            (candy2.special == SpecialType.WRAPPED && (candy1.special == SpecialType.STRIPED_HORIZONTAL || candy1.special == SpecialType.STRIPED_VERTICAL))
        ) {
            for (dr in -1..1) {
                val r = (r2 + dr).coerceIn(0, rows - 1)
                for (c in 0 until cols) allTilesToClear.add(r to c)
            }
            for (dc in -1..1) {
                val c = (c2 + dc).coerceIn(0, cols - 1)
                for (r in 0 until rows) allTilesToClear.add(r to c)
            }
            activatedSpecials.add(SpecialType.WRAPPED)
        } else {
            return null
        }

        // Clear tiles
        for (pos in allTilesToClear) {
            val tile = board[pos.first][pos.second]
            if (tile.hasJelly) {
                tile.hasJelly = false
                jelliesCleared++
            }
            val c = tile.candy
            if (c != null) {
                clearedColors[c.color] = (clearedColors[c.color] ?: 0) + 1
            }
            tile.candy = null
        }

        return ClearResult(
            clearedTiles = allTilesToClear,
            newSpecialCandies = emptyMap(),
            clearedColorCounts = clearedColors,
            activatedSpecials = activatedSpecials,
            jelliesCleared = jelliesCleared
        )
    }

    /**
     * Applies gravity column by column.
     * Candies drop down into null spaces, and new candies fall from above.
     */
    fun applyGravity(board: Array<Array<BoardTile>>): Boolean {
        val rows = board.size
        val cols = board[0].size
        var changed = false

        for (c in 0 until cols) {
            var writeRow = rows - 1
            for (r in rows - 1 downTo 0) {
                val currentCandy = board[r][c].candy
                if (currentCandy != null) {
                    if (writeRow != r) {
                        board[writeRow][c].candy = currentCandy
                        board[r][c].candy = null
                        changed = true
                    }
                    writeRow--
                }
            }

            // Fill empty rows at top with new candies
            for (r in writeRow downTo 0) {
                board[r][c].candy = generateCandy()
                changed = true
            }
        }

        return changed
    }

    fun hasValidMoves(board: Array<Array<BoardTile>>): Boolean {
        val rows = board.size
        val cols = board[0].size

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Test right neighbor
                if (c + 1 < cols && canSwap(r, c, r, c + 1, board)) {
                    return true
                }
                // Test down neighbor
                if (r + 1 < rows && canSwap(r, c, r + 1, c, board)) {
                    return true
                }
            }
        }
        return false
    }

    fun findHintMove(board: Array<Array<BoardTile>>): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
        val rows = board.size
        val cols = board[0].size

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (c + 1 < cols && canSwap(r, c, r, c + 1, board)) {
                    return (r to c) to (r to c + 1)
                }
                if (r + 1 < rows && canSwap(r, c, r + 1, c, board)) {
                    return (r to c) to (r + 1 to c)
                }
            }
        }
        return null
    }

    fun shuffleBoard(board: Array<Array<BoardTile>>) {
        val rows = board.size
        val cols = board[0].size
        val candies = mutableListOf<Candy>()

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                board[r][c].candy?.let { candies.add(it) }
            }
        }

        var attempts = 0
        do {
            candies.shuffle()
            var idx = 0
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    board[r][c].candy = candies[idx++]
                }
            }
            attempts++
        } while ((!hasValidMoves(board) || findMatches(board).isNotEmpty()) && attempts < 50)
    }
}
