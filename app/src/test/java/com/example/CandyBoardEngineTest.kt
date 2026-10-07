package com.example

import com.example.game.CandyBoardEngine
import com.example.model.CandyColor
import com.example.model.SpecialType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CandyBoardEngineTest {

    private val engine = CandyBoardEngine()

    @Test
    fun initialBoardHasNoMatchesAndHasValidMoves() {
        val board = engine.createInitialBoard(8, 8)
        val matches = engine.findMatches(board)
        assertTrue("Initial board should not start with matches", matches.isEmpty())
        assertTrue("Initial board must have at least one valid move", engine.hasValidMoves(board))
    }

    @Test
    fun matchFourCreatesStripedCandy() {
        val board = Array(8) { r -> Array(8) { c -> com.example.model.BoardTile(r, c) } }
        for (c in 0..3) {
            board[0][c].candy = engine.generateCandy(CandyColor.RED)
        }
        val matches = engine.findMatches(board)
        val fourMatch = matches.find { it.tiles.size == 4 }
        assertNotNull(fourMatch)
        assertEquals(SpecialType.STRIPED_VERTICAL, fourMatch?.specialToCreate)
    }

    @Test
    fun matchFiveCreatesColorBomb() {
        val board = Array(8) { r -> Array(8) { c -> com.example.model.BoardTile(r, c) } }
        for (c in 0..4) {
            board[1][c].candy = engine.generateCandy(CandyColor.BLUE)
        }
        val matches = engine.findMatches(board)
        val fiveMatch = matches.find { it.tiles.size == 5 }
        assertNotNull(fiveMatch)
        assertEquals(SpecialType.COLOR_BOMB, fiveMatch?.specialToCreate)
    }
}
