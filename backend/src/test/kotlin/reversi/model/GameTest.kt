import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game
import kotlin.test.assertEquals

class GameTest {

    @Test
    fun `default constructor sets expected defaults`() {
        val game = Game(board = Board.new(8, CellState.EMPTY))
        assertEquals(CellState.BLACK, game.currentPlayer)
        assertEquals(false, game.isFinished)
        assertEquals(2, game.playerTypes.size)
    }

    @Test
    fun `serialization round trip`() {
        val game = Game(board = Board.new(2, CellState.EMPTY))
        val json = Json.encodeToString(game)
        val decoded = Json.decodeFromString<Game>(json)
        assertEquals(game, decoded)
    }
}
