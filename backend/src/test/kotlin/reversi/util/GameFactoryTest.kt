package reversi.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import reversi.controller.dto.MoveRequest
import reversi.controller.dto.NewGameRequest
import reversi.model.*
import java.util.*

class GameFactoryTest {

    @Test
    fun `should create a new game with provided board and valid moves`() {
        val board = BoardFactory.createStartingBoard()
        val moves: MoveList = listOf(Pair(2, 3), Pair(4, 5))
        val request = NewGameRequest(
            id = "game-1",
            difficulty = Difficulty.MEDIUM,
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
            currentPlayer = CellState.BLACK
        )

        val game: Game = GameFactory.createNewGame(request, board, moves)

        assertEquals("game-1", game.id)
        assertEquals(board, game.board)
        assertEquals(mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI), game.playerTypes)
        assertEquals(CellState.BLACK, game.currentPlayer)
        assertEquals(moves.size, game.validMoves.size)
        assertTrue(game.validMoves.contains(MoveRequest(2, 3)))
        assertTrue(game.validMoves.contains(MoveRequest(4, 5)))
    }

    @Test
    fun `should create a new game with default board if board is null`() {
        val request = NewGameRequest(
            id = "game-2",
            difficulty = Difficulty.MEDIUM,
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
            currentPlayer = CellState.BLACK
        )

        val game: Game = GameFactory.createNewGame(request, null)

        assertNotNull(game.board)
        assertEquals("game-2", game.id)
        assertEquals(mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI), game.playerTypes)
        assertEquals(CellState.BLACK, game.currentPlayer)
        assertTrue(game.validMoves.isEmpty())
    }

    @Test
    fun `should generate random UUID if id is null`() {
        val request = NewGameRequest(
            id = null,
            difficulty = Difficulty.MEDIUM,
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
            currentPlayer = CellState.BLACK
        )

        val game: Game = GameFactory.createNewGame(request, null)

        assertNotNull(game.id)
        assertDoesNotThrow { UUID.fromString(game.id) }
    }

    @Test
    fun `should map MoveList to MoveRequest correctly`() {
        val moves: MoveList = listOf(Pair(0, 0), Pair(7, 7))
        val request = NewGameRequest(
            id = "game-3",
            difficulty = Difficulty.MEDIUM,
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
            currentPlayer = CellState.BLACK
        )

        val game: Game = GameFactory.createNewGame(request, null, moves)

        assertEquals(2, game.validMoves.size)
        assertEquals(MoveRequest(0, 0), game.validMoves[0])
        assertEquals(MoveRequest(7, 7), game.validMoves[1])
    }

    @Test
    fun `should handle empty validMoves`() {
        val request = NewGameRequest(
            id = "game-4",
            difficulty = Difficulty.MEDIUM,
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
            currentPlayer = CellState.BLACK
        )

        val game: Game = GameFactory.createNewGame(request, null, emptyList())

        assertTrue(game.validMoves.isEmpty())
    }
}
