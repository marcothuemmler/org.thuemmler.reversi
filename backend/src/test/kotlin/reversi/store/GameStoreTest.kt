import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game
import reversi.store.GameStore
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameStoreTest {

    private lateinit var store: GameStore

    @BeforeEach
    fun setup() {
        store = GameStore()
    }

    @Test
    fun `save and get game`() {
        val game = Game(
            id = "game1",
            board = Board.new(CellState.EMPTY),
            playerTypes = mapOf(),
            currentPlayer = CellState.BLACK
        )
        store.save(game)
        assertEquals(game, store.getGame("game1"))
    }

    @Test
    fun `remove game`() {
        val game = Game(
            id = "game2",
            board = Board.new(CellState.EMPTY),
            playerTypes = mapOf(),
            currentPlayer = CellState.BLACK
        )
        store.save(game)
        store.removeGame("game2")
        assertNull(store.getGame("game2"))
    }

    @Test
    fun `list games returns all saved games`() {
        val game1 =
            Game(id = "g1", board = Board.new(CellState.EMPTY), playerTypes = mapOf(), currentPlayer = CellState.BLACK)
        val game2 =
            Game(id = "g2", board = Board.new(CellState.EMPTY), playerTypes = mapOf(), currentPlayer = CellState.BLACK)
        store.save(game1)
        store.save(game2)
        val games = store.listGames()
        assertEquals(2, games.size)
        assert(games.containsAll(listOf(game1, game2)))
    }
}
